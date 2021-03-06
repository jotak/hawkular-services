/*
 * Copyright 2016-2017 Red Hat, Inc. and/or its affiliates
 * and other contributors as indicated by the @author tags.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.hawkular.listener.cache;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Base64;
import java.util.Base64.Decoder;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import org.hawkular.inventory.api.model.ExtendedInventoryStructure;
import org.hawkular.inventory.api.model.Metric;
import org.hawkular.inventory.api.model.MetricType;
import org.hawkular.inventory.json.InventoryJacksonConfig;
import org.hawkular.inventory.paths.RelativePath;
import org.hawkular.listener.exception.InvalidInventoryChunksException;
import org.hawkular.metrics.core.service.MetricsService;
import org.hawkular.metrics.core.service.Order;
import org.hawkular.metrics.model.DataPoint;
import org.hawkular.metrics.model.Tenant;
import org.jboss.logging.Logger;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Throwables;

import rx.Observable;

/**
 * Helper methods to read inventory data as produced by the agents
 * @author Joel Takvorian
 */
public final class InventoryHelper {

    private static final Logger LOG = Logger.getLogger(InventoryHelper.class);
    private static final ObjectMapper MAPPER = new ObjectMapper(new JsonFactory());

    static {
        InventoryJacksonConfig.configure(MAPPER);
    }

    private InventoryHelper() {
    }

    /**
     * Get the list of tenants that have the given feed id
     */
    static Observable<Tenant> listTenantsForFeed(MetricsService metricsService, String feedId) {
        return metricsService.getTenants()
                .flatMap(tenant -> metricsService.findMetricsWithFilters(
                        tenant.getId(),
                        org.hawkular.metrics.model.MetricType.STRING,
                        "module:inventory,feed:" + feedId)
                        .isEmpty().filter(isEmpty -> !isEmpty).map(any -> tenant));
    }

    /**
     * Get the list of all metric types for given tenant and feed
     */
    static Observable<MetricType.Blueprint> listMetricTypes(MetricsService metricsService,
                                                            String tenantId,
                                                            String feedId) {
        return listMetricTypes(metricsService, tenantId, feedId, System.currentTimeMillis());
    }

    /**
     * Get the list of all metric types for given tenant and feed, with enforced current time
     */
    static Observable<MetricType.Blueprint> listMetricTypes(MetricsService metricsService,
                                                            String tenantId,
                                                            String feedId,
                                                            long currentTime) {
        String tags = "module:inventory,feed:" + feedId + ",type:mt";
        return metricsService.findMetricsWithFilters(tenantId, org.hawkular.metrics.model.MetricType.STRING, tags)
                .flatMap(metric -> {
                    return metricsService.findStringData(metric.getMetricId(), 0, currentTime,
                            false, 0, Order.DESC)
                            .toList()
                            .map(dataPoints -> {
                                try {
                                    return rebuildFromChunks(dataPoints);
                                } catch (InvalidInventoryChunksException e) {
                                    e.addContext("metricId", metric.getId());
                                    throw Throwables.propagate(e);
                                }
                            })
                            .map(inv -> inv.getStructure().get(RelativePath.fromString("")))
                            .filter(bp -> bp instanceof MetricType.Blueprint)
                            .map(bp -> (MetricType.Blueprint) bp);
                });
    }

    /**
     * Get the list of metrics for given tenant, feed and metric type
     */
    static Observable<Metric.Blueprint> listMetricsForType(MetricsService metricsService,
                                                           String tenantId,
                                                           String feedId,
                                                           MetricType.Blueprint metricType) {
        return listMetricsForType(metricsService, tenantId, feedId, metricType, System.currentTimeMillis());
    }

    /**
     * Get the list of metrics for given tenant, feed and metric type
     */
    static Observable<Metric.Blueprint> listMetricsForType(MetricsService metricsService,
                                                           String tenantId,
                                                           String feedId,
                                                           MetricType.Blueprint metricType,
                                                           long currentTime) {
        String escapedForRegex = Pattern.quote("|" + metricType.getId() + "|");
        String tags = "module:inventory,feed:" + feedId + ",type:r,mtypes:.*" + escapedForRegex + ".*";
        return metricsService.findMetricsWithFilters(tenantId, org.hawkular.metrics.model.MetricType.STRING, tags)
                .flatMap(metric -> {
                    return metricsService.findStringData(metric.getMetricId(), 0, currentTime,
                            false, 0, Order.DESC)
                            .toList()
                            .map(dataPoints -> {
                                try {
                                    return rebuildFromChunks(dataPoints);
                                } catch (InvalidInventoryChunksException e) {
                                    e.addContext("metricType", metricType.getId());
                                    e.addContext("metricId", metric.getId());
                                    throw Throwables.propagate(e);
                                }
                            })
                            .map(inv -> extractMetricsForType(inv, metricType.getId()))
                            .flatMap(Observable::from);
                });
    }

    private static List<Metric.Blueprint> extractMetricsForType(ExtendedInventoryStructure inv, String metricTypeId) {
        return inv.getMetricTypesIndex().getOrDefault(metricTypeId, Collections.emptyList()).stream()
                .map(relPath -> inv.getStructure().get(RelativePath.fromString(relPath)))
                .filter(bp -> bp instanceof Metric.Blueprint)
                .map(bp -> (Metric.Blueprint)bp)
                .collect(Collectors.toList());

    }

    @VisibleForTesting
    static ExtendedInventoryStructure rebuildFromChunks(List<DataPoint<String>> datapoints)
            throws InvalidInventoryChunksException {
        if (datapoints.isEmpty()) {
            throw new InvalidInventoryChunksException("Missing inventory: no datapoint found. Did they expire?");
        }
        DataPoint<String> masterNode = datapoints.get(0);
        Decoder decoder = Base64.getDecoder();
        final byte[] all;
        if (masterNode.getTags().containsKey("chunks")) {
            int nbChunks = Integer.parseInt(masterNode.getTags().get("chunks"));
            int totalSize = Integer.parseInt(masterNode.getTags().get("size"));
            byte[] master = decoder.decode(masterNode.getValue().getBytes());
            if (master.length == 0) {
                throw new InvalidInventoryChunksException("Missing inventory: master datapoint exists but is empty");
            }
            if (nbChunks > datapoints.size()) {
                // Sanity check: missing chunks?
                throw new InvalidInventoryChunksException("Inventory sanity check failure: " + nbChunks
                        + " chunks expected, only " + datapoints.size() + " are available");
            }
            all = new byte[totalSize];
            int pos = 0;
            System.arraycopy(master, 0, all, pos, master.length);
            pos += master.length;
            for (int i = 1; i < nbChunks; i++) {
                DataPoint<String> slaveNode = datapoints.get(i);
                // Perform sanity check using timestamps; they should all be contiguous, in decreasing order
                if (slaveNode.getTimestamp() != masterNode.getTimestamp() - i) {
                    throw new InvalidInventoryChunksException("Inventory sanity check failure: chunk n°" + i
                            + " timestamp is " + slaveNode.getTimestamp() + ", expecting "
                            + (masterNode.getTimestamp() - i));
                }
                byte[] slave = decoder.decode(slaveNode.getValue().getBytes());
                System.arraycopy(slave, 0, all, pos, slave.length);
                pos += slave.length;
            }
        } else {
            // Not chunked
            all = decoder.decode(masterNode.getValue().getBytes());
        }
        try {
            String decompressed = decompress(all);
            return MAPPER.readValue(decompressed, ExtendedInventoryStructure.class);
        } catch (IOException e) {
            throw new InvalidInventoryChunksException("Could not read assembled chunks", e);
        }
    }

    private static String decompress(byte[] gzipped) throws IOException {
        if ((gzipped == null) || (gzipped.length == 0)) {
            return "";
        }
        StringBuilder outStr = new StringBuilder();
        GZIPInputStream gis = new GZIPInputStream(new ByteArrayInputStream(gzipped));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(gis, "UTF-8"));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            outStr.append(line);
        }
        return outStr.toString();
    }
}
