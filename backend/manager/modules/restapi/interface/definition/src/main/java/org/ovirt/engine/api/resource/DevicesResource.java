/*
* Copyright (c) 2010 Red Hat, Inc.
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

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.POST;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.BaseDevice;
import org.ovirt.engine.api.model.BaseDevices;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface DevicesResource<D extends BaseDevice, C extends BaseDevices>
    extends ReadOnlyDevicesResource<D, C> {

    @POST
    @Consumes({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
    public Response add(D device);

    @DELETE
    @Path("{id}")
    public Response remove(@PathParam("id") String id);

    // Note the departure from the usual convention of naming the path
    // parameter as "id". This is to work-around a RESTEasy bug in handling
    // covariant return types - in this case, we've narrowed the return
    // type of the overridden getDeviceSubResource() method from the original
    // ReadOnlyDeviceResource to the DeviceResource sub-interface.
    @Path("{iden}")
    @Override
    public DeviceResource<D> getDeviceSubResource(@PathParam("iden") String id);
}
