package org.ovirt.engine.api.restapi.resource;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.ovirt.engine.api.restapi.test.util.TestHelper.eqQueryParams;
import static org.powermock.api.easymock.PowerMock.createMock;
import static org.powermock.api.easymock.PowerMock.replayAll;
import static org.powermock.api.easymock.PowerMock.verifyAll;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.ovirt.engine.api.common.invocation.Current;
import org.ovirt.engine.api.model.API;
import org.ovirt.engine.api.model.Link;
import org.ovirt.engine.api.model.SpecialObjects;
import org.ovirt.engine.api.restapi.logging.MessageBundle;
import org.ovirt.engine.api.restapi.util.SessionHelper;
import org.ovirt.engine.core.common.interfaces.BackendLocal;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.queries.GetConfigurationValueParameters;
import org.ovirt.engine.core.common.queries.GetSystemStatisticsQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;

public class BackendApiResourceTest extends Assert {

    private static final String ROOT_TAG_REL = "tags/root";

    private static final String ROOT_TAG_HREF = "/ovirt-engine/api/tags/00000000-0000-0000-0000-000000000000";

    private static final String BLANK_TEMPLATE_REL = "templates/blank";

    private static final String BLANK_TEMPLATE_HREF = "/ovirt-engine/api/templates/00000000-0000-0000-0000-000000000000";

    protected BackendApiResource resource;

    protected BackendLocal backend;
    protected Current current;
    protected SessionHelper sessionHelper;
    protected HttpHeaders httpHeaders;

    protected static final String USER = "Aladdin";
    protected static final String SECRET = "open sesame";
    protected static final String DOMAIN = "Maghreb";
    protected static final String NAMESPACE = "*";

    protected static final String URI_ROOT = "http://localhost:8099";
    protected static final String SLASH = "/";
    protected static final String BASE_PATH = "/ovirt-engine/api";
    protected static final String URI_BASE = URI_ROOT + BASE_PATH;
    protected static final String BUNDLE_PATH = "org/ovirt/engine/api/restapi/logging/Messages";
    protected static final String sessionId = Guid.newGuid().toString();
    private static String USER_FILTER_HEADER = "Filter";

    protected static final int MAJOR = 11;
    protected static final int MINOR = 0;
    protected static final int BUILD = 99;
    protected static final int REVISION = 13;
    protected static final String SYSTEM_VERSION =
        Integer.toString(MAJOR) + "." +
        Integer.toString(MINOR) + "." +
        Integer.toString(BUILD) + "." +
        Integer.toString(REVISION);

    protected static int TOTAL_VMS = 123456;
    protected static int ACTIVE_VMS = 23456;
    protected static int TOTAL_HOSTS = 23456;
    protected static int ACTIVE_HOSTS = 3456;
    protected static int TOTAL_USERS = 3456;
    protected static int ACTIVE_USERS = 456;
    protected static int TOTAL_STORAGE_DOMAINS = 56;
    protected static int ACTIVE_STORAGE_DOMAINS = 6;

    private static final String[] relationships = {
        "capabilities",
        "clusters",
        "clusters/search",
        "datacenters",
        "datacenters/search",
        "events",
        "events/search",
        "hosts",
        "hosts/search",
        "networks",
        "networks/search",
        "roles",
        "storagedomains",
        "storagedomains/search",
        "tags",
        "bookmarks",
        "templates",
        "templates/search",
        "instancetypes",
        "instancetypes/search",
        "users",
        "users/search",
        "groups",
        "groups/search",
        "domains",
        "vmpools",
        "vmpools/search",
        "vms",
        "vms/search",
        "disks",
        "disks/search",
        "jobs",
        "storageconnections",
        "vnicprofiles",
        "schedulingpolicyunits",
        "schedulingpolicies",
        "permissions",
        "macpools"
    };

    private static final String[] relationshipsGlusterOnly = {
            "capabilities",
            "clusters",
            "clusters/search",
            "events",
            "events/search",
            "hosts",
            "hosts/search",
            "networks",
            "networks/search",
            "roles",
            "tags",
            "users",
            "users/search",
            "groups",
            "groups/search",
            "domains",
    };

    private static final String[] hrefs = {
        BASE_PATH + "/capabilities",
        BASE_PATH + "/clusters",
        BASE_PATH + "/clusters?search={query}",
        BASE_PATH + "/datacenters",
        BASE_PATH + "/datacenters?search={query}",
        BASE_PATH + "/events",
        BASE_PATH + "/events;from={event_id}?search={query}",
        BASE_PATH + "/hosts",
        BASE_PATH + "/hosts?search={query}",
        BASE_PATH + "/networks",
        BASE_PATH + "/networks?search={query}",
        BASE_PATH + "/roles",
        BASE_PATH + "/storagedomains",
        BASE_PATH + "/storagedomains?search={query}",
        BASE_PATH + "/tags",
        BASE_PATH + "/bookmarks",
        BASE_PATH + "/templates",
        BASE_PATH + "/templates?search={query}",
        BASE_PATH + "/instancetypes",
        BASE_PATH + "/instancetypes?search={query}",
        BASE_PATH + "/users",
        BASE_PATH + "/users?search={query}",
        BASE_PATH + "/groups",
        BASE_PATH + "/groups?search={query}",
        BASE_PATH + "/domains",
        BASE_PATH + "/vmpools",
        BASE_PATH + "/vmpools?search={query}",
        BASE_PATH + "/vms",
        BASE_PATH + "/vms?search={query}",
        BASE_PATH + "/disks",
        BASE_PATH + "/disks?search={query}",
        BASE_PATH + "/jobs",
        BASE_PATH + "/storageconnections",
        BASE_PATH + "/vnicprofiles",
        BASE_PATH + "/schedulingpolicyunits",
        BASE_PATH + "/schedulingpolicies",
        BASE_PATH + "/permissions",
        BASE_PATH + "/macpools"
    };

    private static final String[] hrefsGlusterOnly = {
            BASE_PATH + "/capabilities",
            BASE_PATH + "/clusters",
            BASE_PATH + "/clusters?search={query}",
            BASE_PATH + "/events",
            BASE_PATH + "/events;from={event_id}?search={query}",
            BASE_PATH + "/hosts",
            BASE_PATH + "/hosts?search={query}",
            BASE_PATH + "/networks",
            BASE_PATH + "/networks?search={query}",
            BASE_PATH + "/roles",
            BASE_PATH + "/tags",
            BASE_PATH + "/users",
            BASE_PATH + "/users?search={query}",
            BASE_PATH + "/groups",
            BASE_PATH + "/groups?search={query}",
            BASE_PATH + "/domains",
    };

    public BackendApiResourceTest() {
        resource = new BackendApiResource();
    }

    @Before
    public void setUp() {
        current = createMock(Current.class);

        sessionHelper = new SessionHelper();
        sessionHelper.setCurrent(current);
        sessionHelper.setSessionId(sessionId);
        resource.setSessionHelper(sessionHelper);

        backend = createMock(BackendLocal.class);
        resource.setBackend(backend);

        MessageBundle messageBundle = new MessageBundle();
        messageBundle.setPath(BUNDLE_PATH);
        messageBundle.populate();
        resource.setMessageBundle(messageBundle);

        httpHeaders = createMock(HttpHeaders.class);
        List<Locale> locales = new ArrayList<Locale>();
        expect(httpHeaders.getAcceptableLanguages()).andReturn(locales).anyTimes();
        List<String> filterValue = new ArrayList<String>();
        filterValue.add("false");
        expect(httpHeaders.getRequestHeader(USER_FILTER_HEADER)).andReturn(filterValue).anyTimes();
        resource.setHttpHeaders(httpHeaders);
    }

    @After
    public void tearDown() {
        verifyAll();
    }

    @Test
    public void testGet() {
        doTestGet(ApplicationMode.AllModes);
    }

    @Test
    public void testGetVirtOnly() {
        doTestGet(ApplicationMode.VirtOnly);
    }

    @Test
    public void testGetWithTrailingSlash() {
        doTestGet(ApplicationMode.AllModes);
    }

    @Test
    public void testGetWithTrailingSlashVirtOnly() {
        doTestGet(ApplicationMode.VirtOnly);
    }

    @Test
    public void testGetGlusterOnly() {
        doTestGlusterOnlyGet();
    }

    protected void doTestGet(ApplicationMode appMode) {
        setupExpectations(appMode, relationships);
        verifyResponse(resource.get());
    }

    private void setupExpectations(ApplicationMode appMode, String[] relationships) {
        expect(current.get(ApplicationMode.class)).andReturn(appMode).anyTimes();
        resource.setUriInfo(setUpUriInfo(URI_BASE + "/", relationships));
        setUpGetSystemVersionExpectations();
        setUpGetSystemStatisticsExpectations();
    }

    protected void doTestGlusterOnlyGet() {
        setupExpectations(ApplicationMode.GlusterOnly, relationshipsGlusterOnly);
        verifyResponseGlusterOnly(resource.get());
    }

    protected HashMap<String, Integer> setUpStats() {
        HashMap<String, Integer> stats = new HashMap<String, Integer>();

        stats.put("total_vms", TOTAL_VMS);
        stats.put("active_vms", ACTIVE_VMS);
        stats.put("total_vds", TOTAL_HOSTS);
        stats.put("active_vds", ACTIVE_HOSTS);
        stats.put("total_users", TOTAL_USERS);
        stats.put("active_users", ACTIVE_USERS);
        stats.put("total_storage_domains", TOTAL_STORAGE_DOMAINS);
        stats.put("active_storage_domains", ACTIVE_STORAGE_DOMAINS);

        return stats;
    }

    protected void verifyResponse(Response response) {
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof API);
        verifyApi((API)response.getEntity());
    }

    protected void verifyResponseGlusterOnly(Response response) {
        assertEquals(200, response.getStatus());
        assertTrue(response.getEntity() instanceof API);
        verifyApiGlusterOnly((API) response.getEntity());
    }

    protected void verifyApi(API api) {
        assertNotNull(api);
        assertNotNull(api.getTime());
        assertNotNull(api.getLinks());

        assertEquals(relationships.length, api.getLinks().size());
        for (int i = 0; i < relationships.length; i++) {
            Link l = api.getLinks().get(i);
            assertNotNull(l);
            assertEquals(relationships[i], l.getRel());
            assertEquals(hrefs[i], l.getHref());
        }

        assertNotNull(api.getSpecialObjects());
        assertContainsRootTag(api.getSpecialObjects());
        assertContainsBlankTemplate(api.getSpecialObjects());
        assertNotNull(api.getProductInfo());
        assertNotNull(api.getProductInfo().getVersion());
        assertNotNull(api.getProductInfo().getFullVersion());
        assertEquals(MAJOR,    api.getProductInfo().getVersion().getMajor().intValue());
        assertEquals(MINOR,    api.getProductInfo().getVersion().getMinor().intValue());
        assertEquals(BUILD,    api.getProductInfo().getVersion().getBuild().intValue());
        assertEquals(REVISION, api.getProductInfo().getVersion().getRevision().intValue());

        assertNotNull(api.getSummary());
        assertEquals(TOTAL_VMS,              api.getSummary().getVMs().getTotal());
        assertEquals(ACTIVE_VMS,             api.getSummary().getVMs().getActive());
        assertEquals(TOTAL_HOSTS,            api.getSummary().getHosts().getTotal());
        assertEquals(ACTIVE_HOSTS,           api.getSummary().getHosts().getActive());
        assertEquals(TOTAL_USERS,            api.getSummary().getUsers().getTotal());
        assertEquals(ACTIVE_USERS,           api.getSummary().getUsers().getActive());
        assertEquals(TOTAL_STORAGE_DOMAINS,  api.getSummary().getStorageDomains().getTotal());
        assertEquals(ACTIVE_STORAGE_DOMAINS, api.getSummary().getStorageDomains().getActive());
    }

    protected void verifyApiGlusterOnly(API api) {
        assertNotNull(api);
        assertNotNull(api.getTime());
        assertNotNull(api.getLinks());

        assertEquals(relationshipsGlusterOnly.length, api.getLinks().size());
        for (int i = 0; i < relationshipsGlusterOnly.length; i++) {
            Link l = api.getLinks().get(i);
            assertNotNull(l);
            assertEquals(relationshipsGlusterOnly[i], l.getRel());
            assertEquals(hrefsGlusterOnly[i], l.getHref());
        }

        assertNotNull(api.getSpecialObjects());
        assertContainsRootTag(api.getSpecialObjects());
        assertNotNull(api.getProductInfo());
        assertNotNull(api.getProductInfo().getVersion());
        assertEquals(MAJOR, api.getProductInfo().getVersion().getMajor().intValue());
        assertEquals(MINOR, api.getProductInfo().getVersion().getMinor().intValue());
        assertEquals(BUILD, api.getProductInfo().getVersion().getBuild().intValue());
        assertEquals(REVISION, api.getProductInfo().getVersion().getRevision().intValue());

        assertNotNull(api.getSummary());
        assertEquals(TOTAL_HOSTS, api.getSummary().getHosts().getTotal());
        assertEquals(ACTIVE_HOSTS, api.getSummary().getHosts().getActive());
        assertEquals(TOTAL_USERS, api.getSummary().getUsers().getTotal());
        assertEquals(ACTIVE_USERS, api.getSummary().getUsers().getActive());
    }



    private static void assertContainsBlankTemplate(SpecialObjects objs) {
        for (Link link : objs.getLinks()) {
            if (link.getHref().equals(BLANK_TEMPLATE_HREF) && link.getRel().equals(BLANK_TEMPLATE_REL)) {
                return;
            }
        }
        fail();
    }

    private static void assertContainsRootTag(SpecialObjects objs) {
        for (Link link : objs.getLinks()) {
            if (link.getHref().equals(ROOT_TAG_HREF) && link.getRel().equals(ROOT_TAG_REL)) {
                return;
            }
        }
        fail();
    }

    private static void assertEquals(long expected, Long actual) {
        assertEquals(expected, actual.longValue());
    }

    protected UriInfo setUpUriInfo(String base, String[] relationships) {
        UriBuilder uriBuilder = createMock(UriBuilder.class);
        expect(uriBuilder.clone()).andReturn(uriBuilder).anyTimes();

        for (String rel : relationships) {
            UriBuilder colUriBuilder = createMock(UriBuilder.class);
            expect(colUriBuilder.build()).andReturn(URI.create(URI_ROOT + SLASH + rel+ "/")).anyTimes();
            if (rel.endsWith("/search")) {
                expect(uriBuilder.path(rel.replace("/search", ""))).andReturn(colUriBuilder);
            } else {
                expect(uriBuilder.path(rel)).andReturn(colUriBuilder);
            }
        }

        UriInfo uriInfo = createMock(UriInfo.class);
        expect(uriInfo.getBaseUri()).andReturn(URI.create(base)).anyTimes();
        expect(uriInfo.getBaseUriBuilder()).andReturn(uriBuilder);
        for (int i = 0; i < 2; i++) {
            expect(uriInfo.getQueryParameters()).andReturn(null);
        }

        return uriInfo;
    }

    protected void setUpGetSystemVersionExpectations() {
        VdcQueryReturnValue queryResult = createMock(VdcQueryReturnValue.class);

        expect(backend.runQuery(eq(VdcQueryType.GetConfigurationValue), queryVdcVersionParams())).andReturn(queryResult);
        expect(backend.runQuery(eq(VdcQueryType.GetConfigurationValue),
                queryProductRPMVersionParams())).andReturn(queryResult);

        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(SYSTEM_VERSION).anyTimes();
    }

    protected void setUpGetSystemStatisticsExpectations() {
        VdcQueryReturnValue queryResult = createMock(VdcQueryReturnValue.class);

        expect(backend.runQuery(eq(VdcQueryType.GetSystemStatistics), queryParams())).andReturn(queryResult);

        expect(queryResult.getSucceeded()).andReturn(true).anyTimes();
        expect(queryResult.getReturnValue()).andReturn(setUpStats()).anyTimes();

        replayAll();
    }

    protected VdcQueryParametersBase queryProductRPMVersionParams() {
        return eqQueryParams(GetConfigurationValueParameters.class,
                             new String[] { "SessionId"},
                             new Object[] { getSessionId() });
    }

    protected VdcQueryParametersBase queryVdcVersionParams() {
        return eqQueryParams(GetConfigurationValueParameters.class,
                             new String[] { "SessionId"},
                             new Object[] { getSessionId() });
    }

    protected VdcQueryParametersBase queryParams() {
        return eqQueryParams(GetSystemStatisticsQueryParameters.class,
                             new String[] { "SessionId" },
                             new Object[] { getSessionId() });
    }

    protected String getSessionId() {
        return sessionHelper.getSessionId();
    }
}

