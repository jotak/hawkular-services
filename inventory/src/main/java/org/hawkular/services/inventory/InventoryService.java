/*
 * Copyright 2017 Red Hat, Inc. and/or its affiliates
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
package org.hawkular.services.inventory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.hawkular.services.inventory.model.Metric;
import org.hawkular.services.inventory.model.Resource;
import org.hawkular.services.inventory.model.ResourceType;

/**
 * @author Joel Takvorian
 */
public class InventoryService {
    // Temp (ispn mock: stores)
    private Collection<Resource> allResources = new ArrayList<>();
    private Collection<Metric> allMetrics = new ArrayList<>();
    private Collection<ResourceType> allResourceTypes = new ArrayList<>();

    // Temp (ispn mock: indexes)
    private Map<String, Resource> resourcesById = new HashMap<>();
    private Map<String, List<Resource>> resourcesByRoot = new HashMap<>();
    private Map<String, List<Resource>> resourcesByType = new HashMap<>();
    private Map<String, Metric> metricsById = new HashMap<>();
    private Map<String, ResourceType> resourceTypesById = new HashMap<>();

    public void addResource(Resource r) {
        allResources.add(r);
    }

    public void addMetric(Metric m) {
        allMetrics.add(m);
    }

    public void addResourceType(ResourceType rt) {
        allResourceTypes.add(rt);
    }

    public void updateIndexes() {
        resourcesById = allResources.stream().collect(Collectors.toMap(Resource::getId, Function.identity()));
        resourcesByRoot = allResources.stream().collect(
                Collectors.groupingBy(Resource::getRootId, Collectors.mapping(Function.identity(), Collectors.toList())));
        resourcesByType = allResources.stream().collect(
                Collectors.groupingBy(Resource::getTypeId, Collectors.mapping(Function.identity(), Collectors.toList())));
        metricsById = allMetrics.stream().collect(Collectors.toMap(Metric::getId, Function.identity()));
        resourceTypesById = allResourceTypes.stream().collect(Collectors.toMap(ResourceType::getId, Function.identity()));
    }

    public Optional<Resource> findResourceById(String id) {
        return Optional.ofNullable(resourcesById.get(id));
    }

    public Collection<Resource> getAllTopResources() {
        return resourcesByRoot.get("");
    }

    public Collection<ResourceType> getAllResourceTypes() {
        return allResourceTypes;
    }

    public Collection<Resource> getResourcesByType(String typeId) {
        return resourcesByType.get(typeId);
    }

    public Optional<Collection<Resource>> getChildResources(String id) {
        return findResourceById(id)
                .map(r -> r.getChildren(resourcesById::get));
    }

    public Optional<Collection<Metric>> getResourceMetrics(String id) {
        return findResourceById(id)
                .map(r -> r.getMetrics(metricsById::get));
    }

    public Optional<ResourceType> getResourceType(String typeId) {
        return Optional.ofNullable(resourceTypesById.get(typeId));
    }
}
