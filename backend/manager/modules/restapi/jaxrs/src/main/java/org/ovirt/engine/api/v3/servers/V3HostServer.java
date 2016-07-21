/*
Copyright (c) 2016 Red Hat, Inc.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/

package org.ovirt.engine.api.v3.servers;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.HostResource;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.v3.V3Server;
import org.ovirt.engine.api.v3.types.V3Action;
import org.ovirt.engine.api.v3.types.V3Host;

@Produces({"application/xml", "application/json"})
public class V3HostServer extends V3Server<HostResource> {
    public V3HostServer(HostResource delegate) {
        super(delegate);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("activate")
    public Response activate(V3Action action) {
        return adaptAction(getDelegate()::activate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("approve")
    public Response approve(V3Action action) {
        return adaptAction(getDelegate()::approve, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("commitnetconfig")
    public Response commitNetConfig(V3Action action) {
        return adaptAction(getDelegate()::commitNetConfig, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("deactivate")
    public Response deactivate(V3Action action) {
        return adaptAction(getDelegate()::deactivate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("enrollcertificate")
    public Response enrollCertificate(V3Action action) {
        return adaptAction(getDelegate()::enrollCertificate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("fence")
    public Response fence(V3Action action) {
        return adaptAction(getDelegate()::fence, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("forceselectspm")
    public Response forceSelectSpm(V3Action action) {
        return adaptAction(getDelegate()::forceSelectSpm, action);
    }

    @GET
    public V3Host get(@Context HttpHeaders headers, @Context UriInfo ui) {
        // V3 supported a "force" matrix parameter, that is equivalent to calling the "refresh" action:
        boolean force = ParametersHelper.getBooleanParameter(headers, ui, "force", true, false);
        if (force) {
            try {
                getDelegate().refresh(new Action());
            }
            catch (WebApplicationException exception) {
                throw adaptException(exception);
            }
        }
        return adaptGet(getDelegate()::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("install")
    public Response install(V3Action action) {
        return adaptAction(getDelegate()::install, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("iscsidiscover")
    public Response iscsiDiscover(V3Action action) {
        return adaptAction(getDelegate()::iscsiDiscover, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("iscsilogin")
    public Response iscsiLogin(V3Action action) {
        return adaptAction(getDelegate()::iscsiLogin, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("unregisteredstoragedomainsdiscover")
    public Response unregisteredStorageDomainsDiscover(V3Action action) {
        return adaptAction(getDelegate()::unregisteredStorageDomainsDiscover, action);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Host update(V3Host host) {
        return adaptUpdate(getDelegate()::update, host);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("upgrade")
    public Response upgrade(V3Action action) {
        return adaptAction(getDelegate()::upgrade, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("refresh")
    public Response refresh(V3Action action) {
        return adaptAction(getDelegate()::refresh, action);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(getDelegate()::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("setupnetworks")
    public Response setupNetworks(V3Action action) {
        return adaptAction(getDelegate()::setupNetworks, action);
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(getDelegate().getPermissionsResource());
    }

    @Path("tags")
    public V3AssignedTagsServer getTagsResource() {
        return new V3AssignedTagsServer(getDelegate().getTagsResource());
    }

    @Path("fenceagents")
    public V3FenceAgentsServer getFenceAgentsResource() {
        return new V3FenceAgentsServer(getDelegate().getFenceAgentsResource());
    }

    @Path("devices")
    public V3HostDevicesServer getDevicesResource() {
        return new V3HostDevicesServer(getDelegate().getDevicesResource());
    }

    @Path("hooks")
    public V3HostHooksServer getHooksResource() {
        return new V3HostHooksServer(getDelegate().getHooksResource());
    }

    @Path("nics")
    public V3HostNicsServer getNicsResource() {
        return new V3HostNicsServer(getDelegate().getNicsResource());
    }

    @Path("numanodes")
    public V3HostNumaNodesServer getNumaNodesResource() {
        return new V3HostNumaNodesServer(getDelegate().getNumaNodesResource());
    }

    @Path("storage")
    public V3HostStorageServer getStorageResource() {
        return new V3HostStorageServer(getDelegate().getStorageResource());
    }

    @Path("katelloerrata")
    public V3KatelloErrataServer getKatelloErrataResource() {
        return new V3KatelloErrataServer(getDelegate().getKatelloErrataResource());
    }

    @Path("networkattachments")
    public V3NetworkAttachmentsServer getNetworkAttachmentsResource() {
        return new V3NetworkAttachmentsServer(getDelegate().getNetworkAttachmentsResource());
    }

    @Path("storageconnectionextensions")
    public V3StorageServerConnectionExtensionsServer getStorageConnectionExtensionsResource() {
        return new V3StorageServerConnectionExtensionsServer(getDelegate().getStorageConnectionExtensionsResource());
    }

    @Path("unmanagednetworks")
    public V3UnmanagedNetworksServer getUnmanagedNetworksResource() {
        return new V3UnmanagedNetworksServer(getDelegate().getUnmanagedNetworksResource());
    }

    @Path("statistics")
    public V3StatisticsServer getStatisticsResource() {
        return new V3StatisticsServer(getDelegate().getStatisticsResource());
    }

    @Path("{action: (activate|approve|commitnetconfig|deactivate|enrollcertificate|fence|forceselectspm|install|iscsidiscover|iscsilogin|refresh|setupnetworks|unregisteredstoragedomainsdiscover|upgrade)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(getDelegate().getActionResource(action, oid));
    }
}
