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
import org.ovirt.engine.api.model.API;
import org.ovirt.engine.api.model.Action;
import org.ovirt.engine.api.model.ApiSummary;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.DetailedLink;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.LinkHeader;
import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.model.ProductInfo;
import org.ovirt.engine.api.model.RSDL;
import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.ApiResource;
import org.ovirt.engine.api.restapi.types.DateMapper;
import org.ovirt.engine.api.restapi.util.ErrorMessageHelper;
import org.ovirt.engine.api.restapi.util.VersionHelper;
import org.ovirt.engine.api.rsdl.RsdlManager;
import org.ovirt.engine.api.utils.ApiRootLinksCreator;
import org.ovirt.engine.api.utils.LinkHelper;
import org.ovirt.engine.core.common.action.VdcActionParametersBase;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.constants.QueryConstants;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.branding.BrandingManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BackendApiResource
    extends AbstractBackendActionableResource<API, Object>
    implements ApiResource {

    private static final Logger log = LoggerFactory.getLogger(BackendApiResource.class);
    private static final String SYSTEM_STATS_ERROR = "Unknown error querying system statistics";
    private static final String API_SCHEMA = "api.xsd";
    private static final String RSDL_CONSTRAINT_PARAMETER = "rsdl";
    private static final String SCHEMA_CONSTRAINT_PARAMETER = "schema";
    private static final String SCHEMA_NAME = "ovirt-engine-api-schema.xsd";

    private RSDL rsdl = null;

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();
    ApplicationMode appMode = ApplicationMode.AllModes;

    public BackendApiResource() {
        super(Guid.Empty.toString(), API.class, Object.class);
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

    private API getApi() {
        API api = new API();
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

    private API getGlusterApi() {
        API api = new API();
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

        if(response instanceof API) {
            addHeader(response, responseBuilder, uriBuilder);
        }

        return responseBuilder;
    }

    @Override
    public Response head() {
        appMode = getCurrent().getApplicationMode();
        API api = null;
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
                RSDL rsdl = addSystemVersion(getRSDL());
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
            } catch (IOException ignored) {}
        }
    }

    private RSDL addSystemVersion(RSDL rsdl) {
            rsdl.setVersion(VersionHelper.parseVersion(getConfigurationValueDefault(String.class, ConfigurationValues.VdcVersion)));
            return rsdl;
    }

    public synchronized RSDL getRSDL() throws ClassNotFoundException, IOException {
        if (rsdl == null) {
            rsdl = RsdlManager.loadRsdl(
                getCurrent().getApplicationMode(),
                getUriInfo().getBaseUri().getPath()
            );
        }
        return rsdl;
    }

    private API addSystemVersion(API api) {
        String productVersion = getConfigurationValueDefault(String.class,
                ConfigurationValues.ProductRPMVersion);
        BrandingManager obrand = BrandingManager.getInstance();
        api.setProductInfo(new ProductInfo());
        api.getProductInfo().setName(obrand.getMessage("obrand.backend.product"));
        api.getProductInfo().setVendor(obrand.getMessage("obrand.backend.vendor"));
        api.getProductInfo().setFullVersion(productVersion);
        api.getProductInfo().setVersion(VersionHelper.parseVersion(getConfigurationValueDefault(String.class, ConfigurationValues.VdcVersion)));
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

    private API addSummary(API api) {
        if (!isFiltered()) {
            HashMap<String, Integer> stats = getSystemStatistics();

            ApiSummary summary = new ApiSummary();

            summary.setVMs(new VMs());
            summary.getVMs().setTotal(get(stats, QueryConstants.SYSTEM_STATS_TOTAL_VMS_FIELD));
            summary.getVMs().setActive(get(stats, QueryConstants.SYSTEM_STATS_ACTIVE_VMS_FIELD));

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

    private API addGlusterSummary(API api) {
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
    protected API doPopulate(API model, Object entity) {
        return model;
    }
}
