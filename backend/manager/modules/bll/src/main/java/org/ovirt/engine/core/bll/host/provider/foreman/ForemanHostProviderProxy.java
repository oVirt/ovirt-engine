package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.host.provider.ContentHostProvider;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.BaseProviderProxy;
import org.ovirt.engine.core.common.businessentities.ErrataData;
import org.ovirt.engine.core.common.businessentities.Erratum;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.common.errors.EngineException;
import org.ovirt.engine.core.common.queries.ErrataFilter;
import org.ovirt.engine.core.compat.Version;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

public class ForemanHostProviderProxy extends BaseProviderProxy implements HostProviderProxy {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_ENTRY_POINT = "/api/v2";
    private static final String JSON_FORMAT = "format=json";
    private static final String API_VERSION_ENTRY_POINT = API_ENTRY_POINT + "/status";

    private static final String SEARCH_SECTION_FORMAT = "search=%1$s";
    static final String SEARCH_QUERY_FORMAT = "?" + SEARCH_SECTION_FORMAT + "&" + JSON_FORMAT;

    private static final Version KATELLO_V3_VERSION = new Version("1.11");


    public ForemanHostProviderProxy(Provider<?> hostProvider) {
        super(hostProvider);
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    byte[] runHttpGetMethod(String relativeUrl) {
        return runHttpMethod(
                HttpMethodType.GET,
                "application/json; charset=utf-8",
                null,
                createConnection(relativeUrl));
    }

    /**
     *  Retrieves list of hosts from Foreman responding to the request URL:
     *  /api/v2/hosts?format=json&per_page=9999
     *
     * @deprecated no longer in use
     *
     * @return a list of hosts
     */
    @Override
    @Deprecated
    public List<VDS> getAll() {
        return Collections.emptyList();
    }

    /**
     *  Retrieves list of filtered hosts from Foreman responding to the request URL:
     *  /api/v2/hosts?search=FILTER&format=json&per_page=9999
     *
     * @deprecated no longer in use
     *
     * @param filter The filter for hosts
     * @return a list of filtered hosts
     */
    @Override
    @Deprecated
    public List<VDS> getFiltered(String filter) {
        return Collections.emptyList();
    }

    /**
     *  Retrieves list of discovered hosts from Foreman responding to the request URL:
     *  /api/v2/discovered_hosts?format=json&per_page=9999
     *
     * @deprecated no longer in use
     *
     * @return a list of discovered hosts
     */
    @Override
    @Deprecated
    public List<ExternalDiscoveredHost> getDiscoveredHosts() {
        return Collections.emptyList();
    }

    /**
     *  Retrieves list of host groups from Foreman responding to the request URL:
     *  /api/v2/hostgroups?format=json&per_page=9999
     *
     * @deprecated no longer in use
     *
     * @return a list of host groups
     */
    @Override
    @Deprecated
    public List<ExternalHostGroup> getHostGroups() {
        return Collections.emptyList();
    }

    /**
     *  Retrieves list of compute resource from Foreman responding to the request URL:
     *  /api/v2/compute_resources?search=oVirt%7CRHEV&per_page=9999
     * @deprecated no longer in use
     *
     * @return a list of compute resources
     */
    @Override
    public List<ExternalComputeResource> getComputeResources() {
        return Collections.emptyList();
    }

    @Override
    @Deprecated
    public void provisionHost(VDS host,
            ExternalHostGroup hg,
            ExternalComputeResource computeResource,
            String mac,
            String discoverName,
            String rootPassword,
            String ip) {
        // NOP no longer in use
    }

    @Override
    protected void afterReadResponse(HttpURLConnection connection, byte[] response) throws Exception {
        if (isUnsuccessfulResponseCode(connection)) {
            ForemanErrorWrapper ferr = objectMapper.readValue(response, ForemanErrorWrapper.class);
            String err = StringUtils.join(ferr.getForemanError().getFullMessages(), ", ");
            throw new EngineException(EngineError.PROVIDER_FAILURE, err);
        }
    }

    @Override
    public void testConnection() {
        runHttpGetMethod(API_ENTRY_POINT);
    }

    @Override
    public ErrataData getErrataForHost(ContentHostIdentifier contentHostIdentifier, ErrataFilter errataFilter) {
        return getContentHostProvider().getErrataForHost(contentHostIdentifier, errataFilter);
    }

    @Override
    public Erratum getErratumForHost(ContentHostIdentifier contentHostIdentifier, String erratumId) {
        return getContentHostProvider().getErratumForHost(contentHostIdentifier, erratumId);
    }

    @Override
    public boolean isContentHostExist(ContentHostIdentifier contentHostIdentifier) {
        return getContentHostProvider().isContentHostExist(contentHostIdentifier);
    }

    private ContentHostProvider getContentHostProvider() {
        Version foremanVersion = getForemanVersion();
        if (foremanVersion != null && foremanVersion.greaterOrEquals(KATELLO_V3_VERSION)) {
            return new KatelloV30Provider(this);
        } else {
            return new KatelloV21Provider(this);
        }
    }

    private Version getForemanVersion() {
        try {
            ReportedForemanStatus status =
                    objectMapper.readValue(runHttpGetMethod(API_VERSION_ENTRY_POINT), ReportedForemanStatus.class);
            return new Version(status.getVersion());
        } catch (IOException e) {
            log.warn(
                    "Unable to detect Foreman version for provider {}. Using older version to connect to the provider",
                    getProvider().getName());
            return null;
        }
    }
}
