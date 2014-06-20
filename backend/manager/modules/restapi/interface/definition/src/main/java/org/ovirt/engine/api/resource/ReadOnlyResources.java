/*
* Copyright (c) 2013 Red Hat, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
*           http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/

package org.ovirt.engine.api.resource;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;

import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.BaseResources;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface ReadOnlyResources<D extends BaseResource, C extends BaseResources> {

    @GET
    public C list();

    /**
     * Sub-resource locator method, returns individual DeviceResource on which the
     * remainder of the URI is dispatched.
     *
     * @param id  the Device ID
     * @return    matching subresource if found
     */
    @Path("{id}")
    public ReadOnlyResource<D> getDeviceSubResource(@PathParam("id") String id);
}
