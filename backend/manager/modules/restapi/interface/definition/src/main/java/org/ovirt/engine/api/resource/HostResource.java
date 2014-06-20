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

import javax.ws.rs.Path;
import javax.ws.rs.POST;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.model.Host;

@Produces({ApiMediaType.APPLICATION_XML, ApiMediaType.APPLICATION_JSON, ApiMediaType.APPLICATION_X_YAML})
public interface HostResource extends UpdatableResource<Host>, MeasurableResource {

    @Path("{action: (approve|install|fence|activate|deactivate|commitnetconfig|iscsidiscover|iscsilogin|forceselectspm)}/{oid}")
    public ActionResource getActionSubresource(@PathParam("action")String action, @PathParam("oid")String oid);

    @POST
    @Actionable
    @Path("approve")
    public Response approve(Action action);

    @POST
    @Actionable
    @Path("install")
    public Response install(Action action);

    @POST
    @Actionable
    @Path("fence")
    public Response fence(Action action);

    @POST
    @Actionable
    @Path("activate")
    public Response activate(Action action);

    @POST
    @Actionable
    @Path("deactivate")
    public Response deactivate(Action action);

    @POST
    @Actionable
    @Path("commitnetconfig")
    public Response commitNetConfig(Action action);

    @POST
    @Actionable
    @Path("iscsidiscover")
    public Response iscsiDiscover(Action action);

    @POST
    @Actionable
    @Path("iscsilogin")
    public Response iscsiLogin(Action action);

    @POST
    @Actionable
    @Path("forceselectspm")
    public Response forceSelectSPM(Action action);

    @Path("numanodes")
    public HostNumaNodesResource getHostNumaNodesResource();

    @Path("nics")
    public HostNicsResource getHostNicsResource();

    @Path("storage")
    public HostStorageResource getHostStorageResource();

    @Path("tags")
    public AssignedTagsResource getTagsResource();

    @Path("hooks")
    public HostHooksResource getHooksResource();

    @Path("permissions")
    public AssignedPermissionsResource getPermissionsResource();
}
