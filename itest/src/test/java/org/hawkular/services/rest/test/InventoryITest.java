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
package org.hawkular.services.rest.test;

import org.jboss.arquillian.container.test.api.RunAsClient;
import org.testng.annotations.Test;

/**
 * Inventory integration tests.
 *
 * @author <a href="https://github.com/ppalaga">Peter Palaga</a>
 */
// TODO/FIXME: remove test?
public class InventoryITest extends AbstractTestBase {
    public static final String GROUP = "InventoryITest";

    @Test(groups = { GROUP })
    @RunAsClient
    public void inventoryUp() throws Throwable {

    }
//    private static final Logger log = Logger.getLogger(InventoryITest.class);
//    private static final String testResourceTypeId = "testResourceType";
//    private static final String testResourceId = "testResource";
//    private static final String testEnvironmentId = "testEnvironment";
//    public static final String inventoryPath = "/hawkular/inventory";
//    public static final String traversalPath = "/hawkular/inventory/traversal";
//
//    @Test(groups = { GROUP })
//    @RunAsClient
//    public void inventoryUp() throws Throwable {
//
//        final String path = "/hawkular/inventory/status";
//        testClient.newRequest()
//                .path(path).get()
//                .assertWithRetries(testResponse -> {
//                    testResponse
//                            .assertCode(200)
//                            .assertJson(inventoryStatus -> {
//                        log.tracef("Got inventory status [%s]", inventoryStatus);
//                        Assert.assertTrue(inventoryStatus.get("Initialized").asBoolean(),
//                                String.format(
//                                        "[%s] should have returned a state with Initialized == true, while it retruened [%s]",
//                                        testResponse.getRequest(), inventoryStatus));
//                    });
//                } , Retry.times(50).delay(250));
//
//    }
//
//    @Test(groups = { GROUP }, dependsOnMethods = { "inventoryUp" })
//    @RunAsClient
//    public void postGetDelete() throws Throwable {
//        /* ensure our env not there already */
//        final String environmentPath = inventoryPath + "/entity/e;" + testEnvironmentId;
//        testClient.newRequest().path(environmentPath).get()
//                .assertCode(404);
//
//        /* create our test environment */
//        String postPath = inventoryPath + "/entity/environment";
//        final Object env = Environment.Blueprint.builder().withId(testEnvironmentId).build();
//        testClient.newRequest().path(postPath).postObject(env).assertCode(201);
//
//        /* check that our env is there */
//        testClient.newRequest()
//                .path(environmentPath).get()
//                .assertCode(200)
//                .assertJson(foundEnv -> Assert.assertEquals(foundEnv.get("id").asText(), testEnvironmentId,
//                        String.format("GET [%s] returned an unexpected object", environmentPath)));
//
//        /* ensure our test resource type not there already */
//        final String resourceTypePath = inventoryPath + "/entity/rt;" + testResourceTypeId;
//        testClient.newRequest().path(resourceTypePath).get()
//                .assertCode(404);
//
//        /* create our test resource type */
//        postPath = inventoryPath + "/entity/resourceType";
//        final Object resType = ResourceType.Blueprint.builder().withId(testResourceTypeId).build();
//        testClient.newRequest().path(postPath).postObject(resType).assertCode(201);
//
//        /* check that our test resource type is there */
//        testClient.newRequest()
//                .path(resourceTypePath).get()
//                .assertCode(200)
//                .assertJson(json -> Assert.assertEquals(json.get("id").asText(), testResourceTypeId,
//                        String.format("GET [%s] returned an unexpected object", resourceTypePath)));
//
//        /* ensure no resources there already */
//        final String resourcesPath = traversalPath + "/type=e/type=r";
//        final String resourcePath = inventoryPath + "/entity/e;" + testEnvironmentId + "/r;" + testResourceId;
//        testClient.newRequest().path(resourcePath).get()
//                .assertCode(404);
//
//        /* create our test resource */
//        postPath = inventoryPath + "/entity/e;" + testEnvironmentId + "/resource";
//        Object resource = Resource.Blueprint.builder()//
//                .withId(testResourceId)//
//                .withResourceTypePath("../" + testResourceTypeId)//
//                .build();
//        testClient.newRequest().path(postPath).postObject(resource).assertCode(201);
//
//        /* check that our test resource is there */
//        testClient.newRequest()
//                .path(resourcesPath).get()
//                .assertCode(200)
//                .assertJson(foundResources -> {
//                    Assert.assertTrue(foundResources.isArray(),
//                            String.format("GET [%s] should return an array", resourcesPath));
//                    Assert.assertEquals(foundResources.size(), 1,
//                            String.format("GET [%s] returned an array of unexpected size", resourcesPath));
//                    JsonNode firstResource = foundResources.get(0);
//                    Assert.assertEquals(firstResource.get("id").asText(), testResourceId,
//                            String.format("GET [%s] returned an array with unexpected first element", resourcesPath));
//                });
//
//        testClient.newRequest()
//                .path(resourcePath).get()
//                .assertCode(200)
//                .assertJson(foundResource -> Assert.assertEquals(foundResource.get("id").asText(), testResourceId,
//                        String.format("GET [%s] returned an unexpected object", resourcePath)));
//
//        /* cleanup */
//        testClient.newRequest().path(resourcePath).delete().assertCode(204);
//        testClient.newRequest().path(resourcePath).get().assertCode(404);
//
//        testClient.newRequest().path(resourceTypePath).delete().assertCode(204);
//        testClient.newRequest().path(resourceTypePath).get().assertCode(404);
//
//        testClient.newRequest().path(environmentPath).delete().assertCode(204);
//        testClient.newRequest().path(environmentPath).get().assertCode(404);
//    }
}
