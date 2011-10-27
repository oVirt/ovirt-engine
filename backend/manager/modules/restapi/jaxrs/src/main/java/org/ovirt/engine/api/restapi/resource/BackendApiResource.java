/*
 * Copyright Â© 2010 Red Hat, Inc.
 *
 * @Placeholder for License boilerplate@
 */
package org.ovirt.engine.api.restapi.resource;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.ovirt.engine.api.model.API;
import org.ovirt.engine.api.model.ApiSummary;
import org.ovirt.engine.api.model.BaseResource;
import org.ovirt.engine.api.model.Hosts;
import org.ovirt.engine.api.model.LinkHeader;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.ObjectFactory;
import org.ovirt.engine.api.model.ProductInfo;
import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.model.StorageDomains;
import org.ovirt.engine.api.model.Users;
import org.ovirt.engine.api.model.VMs;
import org.ovirt.engine.api.resource.ApiResource;
import org.ovirt.engine.api.common.util.JAXBHelper;
import org.ovirt.engine.api.common.util.LinkHelper;
import org.ovirt.engine.api.common.util.LinkHelper.LinkFlags;
import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.api.restapi.util.VersionHelper;

public class BackendApiResource
    extends BackendResource
    implements ApiResource {

    private static final String SYSTEM_STATS_ERROR = "Unknown error querying system statistics";

    protected final ObjectFactory OBJECT_FACTORY = new ObjectFactory();

    private API addLinks(API api) {
        addLink(api, "capabilities");
        addLink(api, "clusters", LinkFlags.SEARCHABLE);
        addLink(api, "datacenters", LinkFlags.SEARCHABLE);
        addLink(api, "events", LinkFlags.SEARCHABLE, new HashMap<String, String>(){{put("from", "event_id");}});
        addLink(api, "hosts", LinkFlags.SEARCHABLE);
        addLink(api, "networks");
        addLink(api, "roles");
        addLink(api, "storagedomains", LinkFlags.SEARCHABLE);
        addLink(api, "tags");
        addLink(api, "templates", LinkFlags.SEARCHABLE);
        addLink(api, "users", LinkFlags.SEARCHABLE);
        addLink(api, "groups", LinkFlags.SEARCHABLE);
        addLink(api, "domains");
        addLink(api, "vmpools", LinkFlags.SEARCHABLE);
        addLink(api, "vms", LinkFlags.SEARCHABLE);

        addStaticLinks(getSpecialObjects(api).getLinks(),
                new String[]{"templates/blank", "tags/root"},
                new String[]{getTemplateBlankUri(), getTagRootUri()});

        return api;
    }

    private BaseResource getSpecialObjects(API api) {
        api.setSpecialObjects(new SpecialObjects());
        return api.getSpecialObjects();
    }

    private String getTagRootUri() {
        return LinkHelper.combine(getUriInfo().getBaseUri().getPath(), "tags/00000000-0000-0000-0000-000000000000");
    }

    private String getTemplateBlankUri() {
        return LinkHelper.combine(getUriInfo().getBaseUri().getPath(), "templates/00000000-0000-0000-0000-000000000000");
    }

    private void addStaticLinks(List<Link> linker, String[] rels, String[] refs) {
        if(rels.length == refs.length){
            for(int i = 0; i < rels.length; i++){
                Link link = new Link();
                link.setRel(rels[i]);
                link.setHref(refs[i]);
                linker.add(link);
            }
        }
    }

    private void addLink(API api, String rel, LinkFlags flags) {
        LinkHelper.addLink(getUriInfo().getBaseUri().getPath(),api, rel, flags);
    }

    private void addLink(API api, String rel, LinkFlags flags, Map<String, String> params) {
        LinkHelper.addLink(getUriInfo().getBaseUri().getPath(),api, rel, flags, params);
    }

    private void addLink(API api, String rel) {
        LinkHelper.addLink(getUriInfo().getBaseUri().getPath(),api, rel, LinkFlags.NONE);
    }

    private String addPath(UriBuilder uriBuilder, Link link) {
        String query = "";
        String path = relative(link);

        // otherwise UriBuilder.build() will substitute {query}
        if (path.contains("?")) {
            query = path.substring(path.indexOf("?"));
            path = path.substring(0, path.indexOf("?"));
        }

        link = JAXBHelper.clone(OBJECT_FACTORY.createLink(link));
        link.setHref(uriBuilder.clone().path(path).build() + query);

        return LinkHeader.format(link);
    }

    private void addHeader(API api, Response.ResponseBuilder responseBuilder, UriBuilder uriBuilder) {
        // concantenate links in a single header with a comma-separated value,
        // which is the canonical form according to:
        // http://www.w3.org/Protocols/rfc2616/rfc2616-sec4.html#sec4.2
        //
        StringBuffer header = new StringBuffer();

        for (Link l : api.getLinks()) {
            header.append(addPath(uriBuilder, l)).append(",");
        }

        header.setLength(header.length() - 1);

        responseBuilder.header("Link", header);
    }

    private Response.ResponseBuilder getResponseBuilder(API api) {
        UriBuilder uriBuilder = getUriInfo().getBaseUriBuilder();

        Response.ResponseBuilder responseBuilder = Response.ok();

        addHeader(api, responseBuilder, uriBuilder);

        return responseBuilder;
    }

    @Override
    public Response head() {
        API api = addLinks(new API());
        return getResponseBuilder(api).build();
    }

    @Override
    public Response get() {
        API api = addSummary(addSystemVersion(addLinks(new API())));
        return getResponseBuilder(api).entity(api).build();
    }

    private API addSystemVersion(API api) {
        api.setProductInfo(new ProductInfo());
        api.getProductInfo().setName("oVirt Enterprise Virtualization Engine");
        api.getProductInfo().setVendor("Red Hat");
        api.getProductInfo().setVersion(VersionHelper.parseVersion(getConfigurationValue(String.class, ConfigurationValues.VdcVersion, null)));
        return api;
    }

    private HashMap<String, Integer> getSystemStatistics() {
        try {
            VdcQueryReturnValue result = backend.RunQuery(VdcQueryType.GetSystemStatistics,
                                                          sessionize(new GetSystemStatisticsQueryParameters(-1)));

            if (!result.getSucceeded() || result.getReturnValue() == null) {
                String failure;
                if (result.getExceptionString() != null) {
                    failure = localize(result.getExceptionString());
                } else {
                    failure = SYSTEM_STATS_ERROR;
                }
                throw new BackendFailureException(failure);
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
        HashMap<String, Integer> stats = getSystemStatistics();

        ApiSummary summary = new ApiSummary();

        summary.setVMs(new VMs());
        summary.getVMs().setTotal(get(stats, "total_vms"));
        summary.getVMs().setActive(get(stats, "active_vms"));

        summary.setHosts(new Hosts());
        summary.getHosts().setTotal(get(stats, "total_vds"));
        summary.getHosts().setActive(get(stats, "active_vds"));

        summary.setUsers(new Users());
        summary.getUsers().setTotal(get(stats, "total_users"));
        summary.getUsers().setActive(get(stats, "active_users"));

        summary.setStorageDomains(new StorageDomains());
        summary.getStorageDomains().setTotal(get(stats, "total_storage_domains"));
        summary.getStorageDomains().setActive(get(stats, "active_storage_domains"));

        api.setSummary(summary);

        return api;
    }

    private long get(HashMap<String, Integer> stats, String key) {
        return stats.get(key).longValue();
    }

    private String relative(Link link) {
        return link.getHref().substring(link.getHref().indexOf(link.getRel().split("/")[0]), link.getHref().length());
    }
}
