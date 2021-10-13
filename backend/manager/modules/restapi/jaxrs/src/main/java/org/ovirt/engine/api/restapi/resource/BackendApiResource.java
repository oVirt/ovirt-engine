/*
 * Copyright oVirt Authors
 * SPDX-License-Identifier: Apache-2.0
*/

package org.ovirt.engine.api.restapi.resource;

import static java.util.stream.Collectors.joining;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Map;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.Api;
import org.ovirt.engine.api.model.ApiSummary;
import org.ovirt.engine.api.model.ApiSummaryItem;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.ProductInfo;
import org.ovirt.engine.api.model.Rsdl;
import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.model.Tag;
import org.ovirt.engine.api.model.Template;
import org.ovirt.engine.api.model.User;
import org.ovirt.engine.api.model.Version;
import org.ovirt.engine.api.resource.AffinityLabelsResource;
import org.ovirt.engine.api.resource.BookmarksResource;
import org.ovirt.engine.api.resource.ClusterLevelsResource;
import org.ovirt.engine.api.resource.ClustersResource;
import org.ovirt.engine.api.resource.CpuProfilesResource;
import org.ovirt.engine.api.resource.DataCentersResource;
import org.ovirt.engine.api.resource.DiskProfilesResource;
import org.ovirt.engine.api.resource.DisksResource;
import org.ovirt.engine.api.resource.EventsResource;
import org.ovirt.engine.api.resource.ExternalTemplateImportsResource;
import org.ovirt.engine.api.resource.ExternalVmImportsResource;
import org.ovirt.engine.api.resource.HostsResource;
import org.ovirt.engine.api.resource.IconsResource;
import org.ovirt.engine.api.resource.ImageTransfersResource;
import org.ovirt.engine.api.resource.InstanceTypesResource;
import org.ovirt.engine.api.resource.JobsResource;
import org.ovirt.engine.api.resource.MacPoolsResource;
import org.ovirt.engine.api.resource.NetworkFiltersResource;
import org.ovirt.engine.api.resource.NetworksResource;
import org.ovirt.engine.api.resource.OperatingSystemsResource;
import org.ovirt.engine.api.resource.RolesResource;
import org.ovirt.engine.api.resource.SchedulingPoliciesResource;
import org.ovirt.engine.api.resource.SchedulingPolicyUnitsResource;
import org.ovirt.engine.api.resource.StorageDomainsResource;
import org.ovirt.engine.api.resource.StorageServerConnectionsResource;
import org.ovirt.engine.api.resource.SystemOptionsResource;
import org.ovirt.engine.api.resource.SystemPermissionsResource;
import org.ovirt.engine.api.resource.SystemResource;
import org.ovirt.engine.api.resource.TagsResource;
import org.ovirt.engine.api.resource.TemplatesResource;
import org.ovirt.engine.api.resource.VmPoolsResource;
import org.ovirt.engine.api.resource.VmsResource;
import org.ovirt.engine.api.resource.VnicProfilesResource;
import org.ovirt.engine.api.resource.aaa.DomainsResource;
import org.ovirt.engine.api.resource.aaa.GroupsResource;
import org.ovirt.engine.api.resource.aaa.UsersResource;
import org.ovirt.engine.api.resource.externalhostproviders.EngineKatelloErrataResource;
import org.ovirt.engine.api.resource.externalhostproviders.ExternalHostProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenstackImageProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenstackNetworkProvidersResource;
import org.ovirt.engine.api.resource.openstack.OpenstackVolumeProvidersResource;
import org.ovirt.engine.api.restapi.invocation.Current;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.logging.Messages;
import org.ovirt.engine.api.restapi.resource.aaa.BackendDomainsResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendGroupsResource;
import org.ovirt.engine.api.restapi.resource.aaa.BackendUsersResource;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendEngineKatelloErrataResource;
import org.ovirt.engine.api.restapi.resource.externalhostproviders.BackendExternalHostProvidersResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackImageProvidersResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackNetworkProvidersResource;
import org.ovirt.engine.api.restapi.resource.openstack.BackendOpenStackVolumeProvidersResource;
import org.ovirt.engine.api.restapi.rsdl.RsdlLoader;
import org.ovirt.engine.api.restapi.types.DateMapper;
import org.ovirt.engine.api.restapi.types.MappingLocator;
import org.ovirt.engine.api.restapi.types.VersionMapper;
import org.ovirt.engine.api.restapi.util.LinkHelper;
import org.ovirt.engine.api.restapi.util.ParametersHelper;
import org.ovirt.engine.api.utils.ApiRootLinksCreator;
import org.ovirt.engine.api.utils.LinkCreator;
import org.ovirt.engine.core.branding.BrandingManager;
import org.ovirt.engine.core.common.action.ActionParametersBase;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.constants.QueryConstants;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.QueryParametersBase;
import org.ovirt.engine.core.common.queries.QueryReturnValue;
import org.ovirt.engine.core.common.queries.QueryType;
import org.ovirt.engine.core.compat.Guid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendApiResource
    extends AbstractBackendActionableResource<Api, Object>
    implements SystemResource {

    private static volatile BackendApiResource instance;

    public static BackendApiResource getInstance() {
        if (instance == null) {
            synchronized (BackendApiResource.class) {
                if (instance == null) {
                    BackendApiResource tmp = new BackendApiResource();
                    tmp.init();
                    instance = tmp;
                }
            }
        }
        return instance;
    }

    private static final Logger log = LoggerFactory.getLogger(BackendApiResource.class);
    private static final String API_SCHEMA = "api.xsd";
    private static final String RSDL_CONSTRAINT_PARAMETER = "rsdl";
    private static final String SCHEMA_CONSTRAINT_PARAMETER = "schema";
    private static final String SCHEMA_NAME = "ovirt-engine-api-schema.xsd";

    ApplicationMode appMode = ApplicationMode.AllModes;

    public BackendApiResource() {
        super(Guid.Empty.toString(), Api.class, Object.class);
    }

    private void init() {
        // Create and populate the message bundle:
        messageBundle = new MessageBundle();
        messageBundle.setPath(Messages.class.getName());
        messageBundle.populate();

        // Create and populate the mapping locator:
        mappingLocator = new MappingLocator();
        mappingLocator.populate();

    }


    private Collection<DetailedLink> getLinks() {
        return ApiRootLinksCreator.getLinks(getAbsolutePath());
    }

    private Collection<DetailedLink> getGlusterLinks() {
        return ApiRootLinksCreator.getGlusterLinks(getAbsolutePath());
    }

    private String getAbsolutePath(String... segments) {
        Current current = getCurrent();
        return current.getAbsolutePath(segments);
    }

    private Template createBlankTemplate() {
        Template template = new Template();
        String id = "00000000-0000-0000-0000-000000000000";
        template.setId(id);
        template.setHref(getAbsolutePath( "templates", id));
        return template;
    }

    private Tag createRootTag() {
        Tag tag = new Tag();
        String id = "00000000-0000-0000-0000-000000000000";
        tag.setId(id);
        tag.setHref(getAbsolutePath("tags", id));
        return tag;
    }

    private Api getApi() {
        Api api = new Api();
        api.setTime(DateMapper.map(new Date(), null));
        for (DetailedLink detailedLink : getLinks()) {
            //add thin link
            api.getLinks().add(LinkCreator.createLink(detailedLink.getHref(), detailedLink.getRel()));
            //when required - add extra link for search
            if (detailedLink.isSetLinkCapabilities() && detailedLink.getLinkCapabilities().isSetSearchable() && detailedLink.getLinkCapabilities().isSearchable()) {
                api.getLinks().add(LinkCreator.createLink(detailedLink.getHref(), detailedLink.getRel(), detailedLink.getRequest().getUrl().getParametersSets()));
            }
            //add special links
            SpecialObjects specialObjects = new SpecialObjects();
            specialObjects.setBlankTemplate(createBlankTemplate());
            specialObjects.setRootTag(createRootTag());
            api.setSpecialObjects(specialObjects);
        }
        return api;
    }

    private Api getGlusterApi() {
        Api api = new Api();
        api.setTime(DateMapper.map(new Date(), null));
        for (DetailedLink detailedLink : getGlusterLinks()) {
            // add thin link
            api.getLinks().add(LinkCreator.createLink(detailedLink.getHref(), detailedLink.getRel()));
            // when required - add extra link for search
            if (detailedLink.isSetLinkCapabilities() && detailedLink.getLinkCapabilities().isSetSearchable()
                    && detailedLink.getLinkCapabilities().isSearchable()) {
                api.getLinks().add(LinkCreator.createLink(detailedLink.getHref(),
                        detailedLink.getRel(),
                        detailedLink.getRequest().getUrl().getParametersSets()));
            }
            // add special links
            SpecialObjects specialObjects = new SpecialObjects();
            specialObjects.setRootTag(createRootTag());
            api.setSpecialObjects(specialObjects);
        }
        return api;
    }

    private void addHeader(BaseResource response, Response.ResponseBuilder responseBuilder) {
        // Concatenate links in a single header with a comma-separated value, which is the canonical form according
        // to http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2.
        String root = getCurrent().getRoot();
        String links = response.getLinks().stream()
            .map(link -> String.format("<%s>; rel=%s", root + link.getHref(), link.getRel()))
            .sorted()
            .collect(joining(","));
        responseBuilder.header("Link", links);
    }

    private Response.ResponseBuilder getResponseBuilder(BaseResource response) {
        Response.ResponseBuilder responseBuilder = Response.ok();

        if (response instanceof Api) {
            addHeader(response, responseBuilder);
        }

        return responseBuilder;
    }

    @Override
    public Response head() {
        appMode = getCurrent().getApplicationMode();
        Api api;
        if(appMode == ApplicationMode.GlusterOnly) {
            api = getGlusterApi();
        } else {
            api = getApi();
        }
        return getResponseBuilder(api).build();
    }

    @Override
    public Response get() {
        appMode = getCurrent().getApplicationMode();
        if (ParametersHelper.getParameter(httpHeaders, uriInfo, RSDL_CONSTRAINT_PARAMETER) != null) {
            try {
                Rsdl rsdl = addSystemVersion(getRSDL());
                return Response.ok().entity(rsdl).build();
            } catch (Exception e) {
                throw new WebApplicationException(e, Response.status(Response.Status.INTERNAL_SERVER_ERROR).build());
            }
        } else if (ParametersHelper.getParameter(httpHeaders, uriInfo, SCHEMA_CONSTRAINT_PARAMETER) != null) {
            return getSchema();
        } else {
            Api api;
            if (appMode == ApplicationMode.GlusterOnly) {
                api = addSystemVersion(getGlusterApi());
                //only add summary for admin-users, since non-admin users
                //don't have permission to see the system summary
                //(https://bugzilla.redhat.com/1612124)
                if (!isFiltered()) {
                    addGlusterSummary(api);
                }
            } else {
                api = addSystemVersion(getApi());
                //only add summary for admin-users, since non-admin users
                //don't have permission to see the system summary
                //(https://bugzilla.redhat.com/1612124)
                if (!isFiltered()) {
                    addSummary(api);
                }
            }
            setAuthenticatedUser(api);
            return getResponseBuilder(api).entity(api).build();
        }
    }

    /**
     * Set a link to the user of the current session
     * (the 'authenticated user') in the API object.
     * This link enables users a convenient way to see
     * which is the logged-in user, using the system.
     */
    private void setAuthenticatedUser(Api api) {
        QueryReturnValue returnValue = runQuery(QueryType.GetUserBySessionId, new QueryParametersBase());
        DbUser authenticatedUser = (DbUser)returnValue.getReturnValue();
        User user = new User();
        user.setId(authenticatedUser.getId().toString());
        LinkHelper.addLinks(user);
        api.setAuthenticatedUser(user);
        api.setEffectiveUser(user);
        //currently the authenticated and effective users are the same one,
        //but if and when impersonation is introduced, they may be different.
    }

    private Response getSchema() {
        byte[] buffer = new byte[4096];
        String version = getCurrent().getVersion();
        String resourcePath = String.format("/v%s/%s", version, API_SCHEMA);
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);){
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
        }
    }

    private Rsdl addSystemVersion(Rsdl rsdl) {
        rsdl.setVersion(getVersion());
        return rsdl;
    }

    private Version getVersion() {
        QueryReturnValue result = runQuery(QueryType.GetProductVersion, new QueryParametersBase());
        return VersionMapper.map((org.ovirt.engine.core.compat.Version) result.getReturnValue());
    }

    public Rsdl getRSDL() throws ClassNotFoundException, IOException {
        return RsdlLoader.loadRsdl(Rsdl.class);
    }

    private Api addSystemVersion(Api api) {
        String productVersion = getConfigurationValueDefault(ConfigValues.ProductRPMVersion);
        String instanceId = getConfigurationValueDefault(ConfigValues.InstanceId);
        if (productVersion != null) {
            BrandingManager obrand = BrandingManager.getInstance();
            ProductInfo productInfo = new ProductInfo();
            productInfo.setName(obrand.getMessage("obrand.backend.product"));
            productInfo.setVendor(obrand.getMessage("obrand.backend.vendor"));
            productInfo.setInstanceId(instanceId);
            Version version = getVersion();
            version.setFullVersion(productVersion);
            productInfo.setVersion(version);
            api.setProductInfo(productInfo);
        }
        return api;
    }

    private Map<String, Integer> getSystemStatistics() {
        QueryReturnValue result = runQuery(
            QueryType.GetSystemStatistics,
            new GetSystemStatisticsQueryParameters(-1)
        );
        if (result.getSucceeded()) {
            return asStatisticsMap(result.getReturnValue());
        }
        return Collections.emptyMap();
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> asStatisticsMap(Object result) {
        return (Map<String, Integer>)result;
    }

    private Api addSummary(Api api) {
        Map<String, Integer> stats = getSystemStatistics();

        ApiSummary summary = new ApiSummary();

        summary.setVms(
            makeSummaryItem(
                stats,
                QueryConstants.SYSTEM_STATS_TOTAL_VMS_FIELD,
                QueryConstants.SYSTEM_STATS_ACTIVE_VMS_FIELD
            )
        );

        summary.setHosts(
            makeSummaryItem(
                stats,
                QueryConstants.SYSTEM_STATS_TOTAL_HOSTS_FIELD,
                QueryConstants.SYSTEM_STATS_ACTIVE_HOSTS_FIELD
            )
        );

        summary.setUsers(
            makeSummaryItem(
                stats,
                QueryConstants.SYSTEM_STATS_TOTAL_USERS_FIELD,
                QueryConstants.SYSTEM_STATS_ACTIVE_USERS_FIELD
            )
        );

        summary.setStorageDomains(
            makeSummaryItem(
                stats,
                QueryConstants.SYSTEM_STATS_TOTAL_STORAGE_DOMAINS_FIELD,
                QueryConstants.SYSTEM_STATS_ACTIVE_STORAGE_DOMAINS_FIELD
            )
        );

        api.setSummary(summary);

        return api;
    }

    private Api addGlusterSummary(Api api) {
        Map<String, Integer> stats = getSystemStatistics();

        ApiSummary summary = new ApiSummary();

        summary.setHosts(
            makeSummaryItem(
                stats,
                "total_vds",
                "active_vds"
            )
        );

        summary.setUsers(
            makeSummaryItem(
                stats,
                "total_users",
                "active_users"
            )
        );

        api.setSummary(summary);

        return api;
    }

    private ApiSummaryItem makeSummaryItem(Map<String, Integer> values, String totalKey, String activeKey) {
        Integer totalValue = values.get(totalKey);
        Integer activeValue = values.get(activeKey);
        if (totalValue == null && activeValue == null) {
            return null;
        }
        ApiSummaryItem item = new ApiSummaryItem();
        if (totalValue != null) {
            item.setTotal(totalValue);
        }
        if (activeValue != null) {
            item.setActive(activeValue);
        }
        return item;
    }

    @Override
    public Response reloadConfigurations(Action action) {
        return doAction(ActionType.ReloadConfigurations,
                        new ActionParametersBase(),
                        action);
    }

    @Override
    public BookmarksResource getBookmarksResource() {
        return inject(new BackendBookmarksResource());
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
    public ExternalVmImportsResource getExternalVmImportsResource() {
        return inject(new BackendExternalVmImportsResource());
    }

    @Override
    public ExternalTemplateImportsResource getExternalTemplateImportsResource() {
        return inject(new BackendExternalTemplateImportsResource());
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
    public NetworkFiltersResource getNetworkFiltersResource() {
        return inject(new BackendNetworkFiltersResource());
    }

    @Override
    public NetworksResource getNetworksResource() {
        return inject(new BackendNetworksResource());
    }

    @Override
    public OpenstackImageProvidersResource getOpenstackImageProvidersResource() {
        return inject(new BackendOpenStackImageProvidersResource());
    }

    @Override
    public OpenstackNetworkProvidersResource getOpenstackNetworkProvidersResource() {
        return inject(new BackendOpenStackNetworkProvidersResource());
    }

    @Override
    @Deprecated
    public OpenstackVolumeProvidersResource getOpenstackVolumeProvidersResource() {
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
    public EngineKatelloErrataResource getKatelloErrataResource() {
        return inject(new BackendEngineKatelloErrataResource());
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

    @Override
    public ImageTransfersResource getImageTransfersResource() {
        return inject(new BackendImageTransfersResource());
    }

    @Override
    public AffinityLabelsResource getAffinityLabelsResource() {
        return inject(new BackendAffinityLabelsResource());
    }

    @Override
    public ClusterLevelsResource getClusterLevelsResource() {
        return inject(new BackendClusterLevelsResource());
    }

    @Override
    public SystemOptionsResource getOptionsResource() {
        return inject(new BackendSystemOptionsResource());
    }
}
