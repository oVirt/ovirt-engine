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

package org.ovirt.engine.api.restapi.resource;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.UriBuilder;

import org.ovirt.engine.api.common.util.JAXBHelper;
import org.ovirt.engine.api.common.util.QueryHelper;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.model.ApiSummary;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.LinkHeader;
import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.model.ProductInfo;
import org.ovirt.engine.api.model.Rsdl;
import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.model.Vms;
import org.ovirt.engine.api.resource.ApiResource;
import org.ovirt.engine.api.resource.BookmarksResource;
import org.ovirt.engine.api.resource.CapabilitiesResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.resource.CpuProfilesResource;
import org.ovirt.engine.api.resource.DataCentersResource;
import org.ovirt.engine.api.resource.DiskProfilesResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.resource.IconsResource;
import org.ovirt.engine.api.resource.InstanceTypesResource;
import org.ovirt.engine.api.resource.JobsResource;
import org.ovirt.engine.api.resource.MacPoolsResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitsResource;
import org.ovirt.engine.api.resource.StorageDomainsResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.api.resource.SystemPermissionsResource;
import org.ovirt.engine.api.resource.TagsResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.api.resource.aaa.DomainsResource;
import org.ovirt.engine.api.resource.aaa.GroupsResource;
import org.ovirt.engine.api.resource.aaa.UsersResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProvidersResource;
import org.ovirt.engine.api.resource.externalhostproviders.SystemKatelloErrataResource;
import org.ovirt.engine.api.resource.openstack.OpenStackImageProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenStackNetworkProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenStackVolumeProvidersResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendDomainsResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendGroupsResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUsersResource;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendExternalHostProvidersResource;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendSystemKatelloErrataResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackImageProvidersResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackNetworkProvidersResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackVolumeProvidersResource;
import org.ovirt.engine.api.restapi.types.DateMapper;
import org.ovirt.engine.api.restapi.types.VersionMapper;
import org.ovirt.engine.api.restapi.util.ErrorMessageHelper;
import org.ovirt.engine.api.rsdl.RsdlManager;
import org.ovirt.engine.api.utils.ApiRootLinksCreator;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.constants.QueryConstants;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendApiResource
    extends AbstractBackendActionableResource<Api, Object>
    implements ApiResource {

    private static final Logger log = LoggerFactory.getLogger(BackendApiResource.class);
    private static final String SYSTEM_STATS_ERROR = "Unknown error querying system statistics";
    private static final String API_SCHEMA = "api.xsd";
    private static final String RSDL_CONSTRAINT_PARAMETER = "rsdl";
    private static final String SCHEMA_CONSTRAINT_PARAMETER = "schema";
    private static final String SCHEMA_NAME = "ovirt-engine-api-schema.xsd";

    private Rsdl rsdl = null;

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    ApplicationMode appMode = ApplicationMode.AllModes;

    public BackendApiResource() {
        super(Guid.Empty.toString(), Api.class, Object.class);
    }

    private Collection<DetailedLink> getLinks() {
        return ApiRootLinksCreator.getLinks(getUriInfo().getBaseUri().getPath());
    }

    private Collection<DetailedLink> getGlusterLinks() {
        return ApiRootLinksCreator.getGlusterLinks(getUriInfo().getBaseUri().getPath());
    }

    private Link createBlankTemplateLink() {
        Link link = new Link();
        link.setRel("templates/blank");
        link.setHref(getTemplateBlankUri());
        return link;
    }

    private Link createRootTagLink() {
        Link link = new Link();
        link.setRel("tags/root");
        link.setHref(getTagRootUri());
        return link;
    }

    private Api getApi() {
        Api api = new Api();
        api.setTime(DateMapper.map(new Date(), null));
        for (DetailedLink detailedLink : getLinks()) {
            //add thin link
            api.getLinks().add(LinkHelper.createLink(detailedLink.getHref(), detailedLink.getRel()));
            //when required - add extra link for search
            if (detailedLink.isSetLinkCapabilities() && detailedLink.getLinkCapabilities().isSetSearchable() && detailedLink.getLinkCapabilities().isSearchable()) {
                api.getLinks().add(LinkHelper.createLink(detailedLink.getHref(), detailedLink.getRel(), detailedLink.getRequest().getUrl().getParametersSets()));
            }
            //add special links
            api.setSpecialObjects(new SpecialObjects());
            api.getSpecialObjects().getLinks().add(createBlankTemplateLink());
            api.getSpecialObjects().getLinks().add(createRootTagLink());
        }
        return api;
    }

    private Api getGlusterApi() {
        Api api = new Api();
        api.setTime(DateMapper.map(new Date(), null));
        for (DetailedLink detailedLink : getGlusterLinks()) {
            // add thin link
            api.getLinks().add(LinkHelper.createLink(detailedLink.getHref(), detailedLink.getRel()));
            // when required - add extra link for search
            if (detailedLink.isSetLinkCapabilities() && detailedLink.getLinkCapabilities().isSetSearchable()
                    && detailedLink.getLinkCapabilities().isSearchable()) {
                api.getLinks().add(LinkHelper.createLink(detailedLink.getHref(),
                        detailedLink.getRel(),
                        detailedLink.getRequest().getUrl().getParametersSets()));
            }
            // add special links
            api.setSpecialObjects(new SpecialObjects());
            api.getSpecialObjects().getLinks().add(createRootTagLink());
        }
        return api;
    }

    private String getTagRootUri() {
        return LinkHelper.combine(getUriInfo().getBaseUri().getPath(), "tags/00000000-0000-0000-0000-000000000000");
    }

    private String getTemplateBlankUri() {
        return LinkHelper.combine(getUriInfo().getBaseUri().getPath(), "templates/00000000-0000-0000-0000-000000000000");
    }

    private String addPath(UriBuilder uriBuilder, Link link) {
        String query = "";
        String matrix = "";
        String path = relative(link);

        // otherwise UriBuilder.build() will substitute {query}
        if (path.contains("?")) {
            query = path.substring(path.indexOf("?"));
            path = path.substring(0, path.indexOf("?"));
        }

        // otherwise UriBuilder.build() will substitute {matrix}
        if (path.contains(";")) {
            matrix = path.substring(path.indexOf(";"));
            path = path.substring(0, path.indexOf(";"));
        }

        link = JAXBHelper.clone(OBJECT_FACTORY.createLink(link));
        link.setHref(uriBuilder.clone().path(path).build().toString() + matrix + query);

        return LinkHeader.format(link);
    }

    private void addHeader(BaseResource response, Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder) {
        // concantenate links in a single header with a comma-separated value,
        // which is the canonical form according to:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        //
        final StringBuilder header = new StringBuilder(response.getLinks().size() * 16);

        for (Link l : response.getLinks()) {
            header.append(addPath(uriBuilder, l)).append(",");
        }

        header.setLength(header.length() - 1);

        responseBuilder.header("Link", header);
    }

    private Response.ResponseBuilder getResponseBuilder(BaseResource response) {
        UriBuilder uriBuilder = getUriInfo().getBaseUriBuilder();

        Response.ResponseBuilder responseBuilder = Response.ok();

        if(response instanceof Api) {
            addHeader(response, responseBuilder, uriBuilder);
        }

        return responseBuilder;
    }

    @Override
    public Response head() {
        appMode = getCurrent().getApplicationMode();
        Api api = null;
        if(appMode == ApplicationMode.GlusterOnly) {
            api = getGlusterApi();
        }
        else {
            api = getApi();
        }
        return getResponseBuilder(api).build();
    }

    @Override
    public Response get() {
        appMode = getCurrent().getApplicationMode();
        if (QueryHelper.hasConstraint(getUriInfo(), RSDL_CONSTRAINT_PARAMETER)) {
            try {
                Rsdl rsdl = addSystemVersion(getRSDL());
                return Response.ok().entity(rsdl).build();
            } catch (Exception e) {
                throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
            }
        } else if (QueryHelper.hasConstraint(getUriInfo(), SCHEMA_CONSTRAINT_PARAMETER)) {
            return getSchema();
        } else {
            BaseResource response = null;
            if (appMode == ApplicationMode.GlusterOnly) {
                response = addGlusterSummary(addSystemVersion(getGlusterApi()));
            }
            else {
                response = addSummary(addSystemVersion(getApi()));
            }
            return getResponseBuilder(response).entity(response).build();
        }
    }

    private Response getSchema() {
        ByteArrayOutputStream baos = null;
        InputStream is = null;
        byte[] buffer = new byte[4096];
        try {
            baos = new ByteArrayOutputStream();
            is = Thread.currentThread().getContextClassLoader().getResourceAsStream(API_SCHEMA);
            int count;
            while ((count = is.read(buffer)) != -1) {
                baos.write(buffer, 0, count);
            }
            baos.flush();
            return Response.ok(baos.toByteArray(), MediaType.APPLICATION_OCTET_STREAM)
                           .header("content-disposition", "attachment; filename = " + SCHEMA_NAME)
                           .build();
        } catch (IOException e) {
            log.error("Loading api.xsd file failed.", e);
            return Response.serverError().build();
        } finally {
            try {
                if (baos != null) baos.close();
                if (is != null) is.close();
            } catch (IOException e) {
                log.error("cannot close a resource", e);
            }
        }
    }

    private Rsdl addSystemVersion(Rsdl rsdl) {
        rsdl.setVersion(getVersion());
        return rsdl;
    }

    private Version getVersion() {
        VdcQueryReturnValue result = runQuery(VdcQueryType.GetProductVersion, new VdcQueryParametersBase());
        return VersionMapper.map((org.ovirt.engine.core.compat.Version) result.getReturnValue(), null);
    }

    public synchronized Rsdl getRSDL() throws ClassNotFoundException, IOException {
        if (rsdl == null) {
            rsdl = RsdlManager.loadRsdl(
                getCurrent().getApplicationMode(),
                getUriInfo().getBaseUri().getPath()
            );
        }
        return rsdl;
    }

    private Api addSystemVersion(Api api) {
        String productVersion = getConfigurationValueDefault(String.class,
                ConfigurationValues.ProductRPMVersion);
        BrandingManager obrand = BrandingManager.getInstance();
        api.setProductInfo(new ProductInfo());
        api.getProductInfo().setName(obrand.getMessage("obrand.backend.product"));
        api.getProductInfo().setVendor(obrand.getMessage("obrand.backend.vendor"));
        api.getProductInfo().setFullVersion(productVersion);
        api.getProductInfo().setVersion(getVersion());
        return api;
    }

    private HashMap<String, Integer> getSystemStatistics() {
        try {
            VdcQueryReturnValue result = runQuery(VdcQueryType.GetSystemStatistics,
                    new GetSystemStatisticsQueryParameters(-1));

            if (!result.getSucceeded() || result.getReturnValue() == null) {
                String failure;
                Status status;
                if (result.getExceptionString() != null) {
                    failure = localize(result.getExceptionString());
                    status = ErrorMessageHelper.getErrorStatus(result.getExceptionString());
                } else {
                    failure = SYSTEM_STATS_ERROR;
                    status = Status.INTERNAL_SERVER_ERROR;
                }
                throw new BackendFailureException(failure, status);
            }

            return asStatisticsMap(result.getReturnValue());
        } catch (Exception e) {
            return handleError(e, false);
        }
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Integer> asStatisticsMap(Object result) {
        return (HashMap<String, Integer>)result;
    }

    private Api addSummary(Api api) {
        if (!isFiltered()) {
            HashMap<String, Integer> stats = getSystemStatistics();

            ApiSummary summary = new ApiSummary();

            summary.setVms(new Vms());
            summary.getVms().setTotal(get(stats, QueryConstants.SYSTEM_STATS_TOTAL_VMS_FIELD));
            summary.getVms().setActive(get(stats, QueryConstants.SYSTEM_STATS_ACTIVE_VMS_FIELD));

            summary.setHosts(new Hosts());
            summary.getHosts().setTotal(get(stats, QueryConstants.SYSTEM_STATS_TOTAL_HOSTS_FIELD));
            summary.getHosts().setActive(get(stats, QueryConstants.SYSTEM_STATS_ACTIVE_HOSTS_FIELD));

            summary.setUsers(new Users());
            summary.getUsers().setTotal(get(stats, QueryConstants.SYSTEM_STATS_TOTAL_USERS_FIELD));
            summary.getUsers().setActive(get(stats, QueryConstants.SYSTEM_STATS_ACTIVE_USERS_FIELD));

            summary.setStorageDomains(new StorageDomains());
            summary.getStorageDomains().setTotal(get(stats, QueryConstants.SYSTEM_STATS_TOTAL_STORAGE_DOMAINS_FIELD));
            summary.getStorageDomains().setActive(get(stats, QueryConstants.SYSTEM_STATS_ACTIVE_STORAGE_DOMAINS_FIELD));

            api.setSummary(summary);
        }
        return api;
    }

    private Api addGlusterSummary(Api api) {
        HashMap<String, Integer> stats = getSystemStatistics();

        ApiSummary summary = new ApiSummary();

        summary.setHosts(new Hosts());
        summary.getHosts().setTotal(get(stats, "total_vds"));
        summary.getHosts().setActive(get(stats, "active_vds"));

        summary.setUsers(new Users());
        summary.getUsers().setTotal(get(stats, "total_users"));
        summary.getUsers().setActive(get(stats, "active_users"));

        api.setSummary(summary);

        return api;
    }

    private long get(HashMap<String, Integer> stats, String key) {
        return stats.get(key).longValue();
    }

    private String relative(Link link) {
        return link.getHref().substring(link.getHref().indexOf(link.getRel().split("/")[0]), link.getHref().length());
    }

    @Override
    public Response reloadConfigurations(Action action) {
        return doAction(VdcActionType.ReloadConfigurations,
                        new VdcActionParametersBase(),
                        action);
    }

    @Override
    public BookmarksResource getBookmarksResource() {
        return inject(new BackendBookmarksResource());
    }

    @Override
    public CapabilitiesResource getCapabilitiesResource() {
        return inject(new BackendCapabilitiesResource());
    }

    @Override
    public ClustersResource getClustersResource() {
        return inject(new BackendClustersResource());
    }

    @Override
    public CpuProfilesResource getCpuProfilesResource() {
        return inject(new BackendCpuProfilesResource());
    }

    @Override
    public DataCentersResource getDataCentersResource() {
        return inject(new BackendDataCentersResource());
    }

    @Override
    public DiskProfilesResource getDiskProfilesResource() {
        return inject(new BackendDiskProfilesResource());
    }

    @Override
    public DisksResource getDisksResource() {
        return inject(new BackendDisksResource());
    }

    @Override
    public DomainsResource getDomainsResource() {
        return inject(new BackendDomainsResource());
    }

    @Override
    public EventsResource getEventsResource() {
        return inject(new BackendEventsResource());
    }

    @Override
    public ExternalHostProvidersResource getExternalHostProvidersResource() {
        return inject(new BackendExternalHostProvidersResource());
    }

    @Override
    public GroupsResource getGroupsResource() {
        return inject(new BackendGroupsResource());
    }

    @Override
    public HostsResource getHostsResource() {
        return inject(new BackendHostsResource());
    }

    @Override
    public IconsResource getIconsResource() {
        return inject(new BackendIconsResource());
    }

    @Override
    public InstanceTypesResource getInstanceTypesResource() {
        return inject(new BackendInstanceTypesResource());
    }

    @Override
    public JobsResource getJobsResource() {
        return inject(new BackendJobsResource());
    }

    @Override
    public MacPoolsResource getMacPoolsResource() {
        return inject(new BackendMacPoolsResource());
    }

    @Override
    public NetworksResource getNetworksResource() {
        return inject(new BackendNetworksResource());
    }

    @Override
    public OpenStackImageProvidersResource getOpenStackImageProviersResource() {
        return inject(new BackendOpenStackImageProvidersResource());
    }

    @Override
    public OpenStackNetworkProvidersResource getOpenStackNetworkProvidersResource() {
        return inject(new BackendOpenStackNetworkProvidersResource());
    }

    @Override
    public OpenStackVolumeProvidersResource getOpenStackVolumeProvidersResource() {
        return inject(new BackendOpenStackVolumeProvidersResource());
    }

    @Override
    public OperatingSystemsResource getOperatingSystemsResource() {
        return inject(new BackendOperatingSystemsResource());
    }

    @Override
    public RolesResource getRolesResource() {
        return inject(new BackendRolesResource());
    }

    @Override
    public SchedulingPoliciesResource getSchedulingPoliciesResource() {
        return inject(new BackendSchedulingPoliciesResource());
    }

    @Override
    public SchedulingPolicyUnitsResource getSchedulingPolicyUnitsResource() {
        return inject(new BackendSchedulingPolicyUnitsResource());
    }

    @Override
    public StorageDomainsResource getStorageDomainsResource() {
        return inject(new BackendStorageDomainsResource());
    }

    @Override
    public StorageServerConnectionsResource getStorageConnectionsResource() {
        return inject(new BackendStorageServerConnectionsResource());
    }

    @Override
    public SystemKatelloErrataResource getKatelloErrataResource() {
        return inject(new BackendSystemKatelloErrataResource());
    }

    @Override
    public SystemPermissionsResource getPermissionsResource() {
        return inject(new BackendSystemPermissionsResource());
    }

    @Override
    public TagsResource getTagsResource() {
        return inject(new BackendTagsResource());
    }

    @Override
    public TemplatesResource getTemplatesResource() {
        return inject(new BackendTemplatesResource());
    }

    @Override
    public UsersResource getUsersResource() {
        return inject(new BackendUsersResource());
    }

    @Override
    public VmPoolsResource getVmPoolsResource() {
        return inject(new BackendVmPoolsResource());
    }

    @Override
    public VmsResource getVmsResource() {
        return inject(new BackendVmsResource());
    }

    @Override
    public VnicProfilesResource getVnicProfilesResource() {
        return inject(new BackendVnicProfilesResource());
    }
}
