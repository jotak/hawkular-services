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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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

    public Optional<Resource> getResourceById(String id) {
        return Optional.ofNullable(resourcesById.get(id));
    }

    public void loadSubtree(Resource parent) {
        if (parent.getRootId().equals("")) {
            // Optimisation, make sure eveything gets in cache; can be removed safely
            resourcesByRoot.get(parent.getId());
        }
        loadSubtree(parent, new HashSet<>());
    }

    private void loadSubtree(Resource parent, Set<String> loaded) {
        if (loaded.contains(parent.getId())) {
            throw new IllegalStateException("Cycle detected in the tree with id " + parent.getId()
                    + "; aborting operation. The inventory is invalid.");
        }
        loaded.add(parent.getId());
        parent.getChildren(resourcesById::get).forEach(child -> loadSubtree(child, loaded));
    }

    public Collection<Resource> getAllTopResources() {
        return resourcesByRoot.getOrDefault("", Collections.emptyList());
    }

    public Collection<ResourceType> getAllResourceTypes() {
        return allResourceTypes;
    }

    public Collection<Resource> getResourcesByType(String typeId) {
        return resourcesByType.getOrDefault(typeId, Collections.emptyList());
    }

    public Optional<Collection<Metric>> getResourceMetrics(String id) {
        return getResourceById(id)
                .map(r -> r.getMetrics(metricsById::get));
    }

    public Optional<ResourceType> getResourceType(String typeId) {
        return Optional.ofNullable(resourceTypesById.get(typeId));
    }
}
