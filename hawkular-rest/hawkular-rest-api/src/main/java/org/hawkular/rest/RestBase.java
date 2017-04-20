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
package org.hawkular.rest;

import javax.inject.Inject;

//import org.hawkular.inventory.rest.security.TenantId;
import org.jboss.resteasy.annotations.GZIP;

/**
 * @author Jirka Kremser
 * @since 0.0.1
 */
@GZIP
// FIXME: import @TenantId here?
public class RestBase {

    @Inject /*@TenantId*/
    // using the @AllPermissive annotation the tenant will be always the same
    private String tenantId;

    protected String getTenantId() {
        return this.tenantId;
    }
}
