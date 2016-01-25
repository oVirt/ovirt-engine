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
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Actionable;
import org.ovirt.engine.api.resource.HostResource;
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
        return adaptAction(delegate::activate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("approve")
    public Response approve(V3Action action) {
        return adaptAction(delegate::approve, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("commitnetconfig")
    public Response commitNetConfig(V3Action action) {
        return adaptAction(delegate::commitNetConfig, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("deactivate")
    public Response deactivate(V3Action action) {
        return adaptAction(delegate::deactivate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("enrollcertificate")
    public Response enrollCertificate(V3Action action) {
        return adaptAction(delegate::enrollCertificate, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("fence")
    public Response fence(V3Action action) {
        return adaptAction(delegate::fence, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("forceselectspm")
    public Response forceSelectSpm(V3Action action) {
        return adaptAction(delegate::forceSelectSpm, action);
    }

    @GET
    public V3Host get() {
        return adaptGet(delegate::get);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("install")
    public Response install(V3Action action) {
        return adaptAction(delegate::install, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("iscsidiscover")
    public Response iscsiDiscover(V3Action action) {
        return adaptAction(delegate::iscsiDiscover, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("iscsilogin")
    public Response iscsiLogin(V3Action action) {
        return adaptAction(delegate::iscsiLogin, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("unregisteredstoragedomainsdiscover")
    public Response unregisteredStorageDomainsDiscover(V3Action action) {
        return adaptAction(delegate::unregisteredStorageDomainsDiscover, action);
    }

    @PUT
    @Consumes({"application/xml", "application/json"})
    public V3Host update(V3Host host) {
        return adaptUpdate(delegate::update, host);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("upgrade")
    public Response upgrade(V3Action action) {
        return adaptAction(delegate::upgrade, action);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("refresh")
    public Response refresh(V3Action action) {
        return adaptAction(delegate::refresh, action);
    }

    @DELETE
    public Response remove() {
        return adaptRemove(delegate::remove);
    }

    @POST
    @Consumes({"application/xml", "application/json"})
    @Actionable
    @Path("setupnetworks")
    public Response setupNetworks(V3Action action) {
        return adaptAction(delegate::setupNetworks, action);
    }

    @Path("permissions")
    public V3AssignedPermissionsServer getPermissionsResource() {
        return new V3AssignedPermissionsServer(delegate.getPermissionsResource());
    }

    @Path("tags")
    public V3AssignedTagsServer getTagsResource() {
        return new V3AssignedTagsServer(delegate.getTagsResource());
    }

    @Path("fenceagents")
    public V3FenceAgentsServer getFenceAgentsResource() {
        return new V3FenceAgentsServer(delegate.getFenceAgentsResource());
    }

    @Path("devices")
    public V3HostDevicesServer getDevicesResource() {
        return new V3HostDevicesServer(delegate.getDevicesResource());
    }

    @Path("hooks")
    public V3HostHooksServer getHooksResource() {
        return new V3HostHooksServer(delegate.getHooksResource());
    }

    @Path("nics")
    public V3HostNicsServer getNicsResource() {
        return new V3HostNicsServer(delegate.getNicsResource());
    }

    @Path("numanodes")
    public V3HostNumaNodesServer getNumaNodesResource() {
        return new V3HostNumaNodesServer(delegate.getNumaNodesResource());
    }

    @Path("storage")
    public V3HostStorageServer getStorageResource() {
        return new V3HostStorageServer(delegate.getStorageResource());
    }

    @Path("katelloerrata")
    public V3KatelloErrataServer getKatelloErrataResource() {
        return new V3KatelloErrataServer(delegate.getKatelloErrataResource());
    }

    @Path("networkattachments")
    public V3NetworkAttachmentsServer getNetworkAttachmentsResource() {
        return new V3NetworkAttachmentsServer(delegate.getNetworkAttachmentsResource());
    }

    @Path("storageconnectionextensions")
    public V3StorageServerConnectionExtensionsServer getStorageConnectionExtensionsResource() {
        return new V3StorageServerConnectionExtensionsServer(delegate.getStorageConnectionExtensionsResource());
    }

    @Path("unmanagednetworks")
    public V3UnmanagedNetworksServer getUnmanagedNetworksResource() {
        return new V3UnmanagedNetworksServer(delegate.getUnmanagedNetworksResource());
    }

    @Path("{action: (activate|approve|commitnetconfig|deactivate|enrollcertificate|fence|forceselectspm|install|iscsidiscover|iscsilogin|refresh|setupnetworks|unregisteredstoragedomainsdiscover|upgrade)}/{oid}")
    public V3ActionServer getActionResource(@PathParam("action") String action, @PathParam("oid") String oid) {
        return new V3ActionServer(delegate.getActionResource(action, oid));
    }
}
