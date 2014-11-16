package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.net.ssl.SSLException;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpURL;
import org.apache.commons.httpclient.HttpsURL;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.BaseProviderProxy;
import org.ovirt.engine.core.bll.provider.ExternalTrustStoreInitializer;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.ExternalOperatingSystem;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.utils.ssl.AuthSSLProtocolSocketFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.ovirt.engine.core.uutils.crypto.CryptMD5;

public class ForemanHostProviderProxy extends BaseProviderProxy implements HostProviderProxy {

    private Provider<?> hostProvider;

    private HttpClient httpClient = new HttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_ENTRY_POINT = "/api/v2";
    private static final String JSON_FORMAT = "format=json";

    private static final String HOSTS_ENTRY_POINT = API_ENTRY_POINT + "/hosts";
    private static final String ALL_HOSTS_QUERY = HOSTS_ENTRY_POINT + "?" + JSON_FORMAT;
    private static final String SEARCH_SECTION_FORMAT = "search=%1$s";
    private static final String SEARCH_QUERY_FORMAT = "?" + SEARCH_SECTION_FORMAT + "&" + JSON_FORMAT;

    private static final String HOST_GROUPS_ENTRY_POINT = API_ENTRY_POINT + "/hostgroups";
    private static final String HOST_GROUPS_QUERY = HOST_GROUPS_ENTRY_POINT + "?" + JSON_FORMAT;

    private static final String COMPUTE_RESOURCES_HOSTS_ENTRY_POINT = API_ENTRY_POINT
            + "/compute_resources?search=" + URLEncoder.encode("oVirt|RHEV");

    private static final String DISCOVERED_HOSTS = "/discovered_hosts";
    private static final String DISCOVERED_HOSTS_ENTRY_POINT = API_ENTRY_POINT + DISCOVERED_HOSTS;

    private static final String OPERATION_SYSTEM_ENTRY_POINT = API_ENTRY_POINT + "/operatingsystems";
    private static final String OPERATION_SYSTEM_QUERY = OPERATION_SYSTEM_ENTRY_POINT + "?" + JSON_FORMAT;

    public ForemanHostProviderProxy(Provider<?> hostProvider) {
        super(hostProvider);
        this.hostProvider = hostProvider;
        initHttpClient();
    }

    private List<VDS> runHostListMethod(HttpMethod httpMethod) {
        try {
            runHttpMethod(httpClient, httpMethod);
            ForemanHostWrapper fhw = objectMapper.readValue(httpMethod.getResponseBody(), ForemanHostWrapper.class);
            return mapHosts(Arrays.asList(fhw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalDiscoveredHost> runDiscoveredHostListMethod(HttpMethod httpMethod) {
        try {
            runHttpMethod(httpClient, httpMethod);
            ForemanDiscoveredHostWrapper fdw =
                    objectMapper.readValue(httpMethod.getResponseBody(), ForemanDiscoveredHostWrapper.class);
            return mapDiscoveredHosts(Arrays.asList(fdw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalHostGroup> runHostGroupListMethod(HttpMethod httpMethod) {
        try {
            runHttpMethod(httpClient, httpMethod);
            ForemanHostGroupWrapper fhgw =
                    objectMapper.readValue(httpMethod.getResponseBody(), ForemanHostGroupWrapper.class);
            return mapHostGroups(Arrays.asList(fhgw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalOperatingSystem> runOperationSystemMethod(HttpMethod httpMethod) {
        try {
            runHttpMethod(httpClient, httpMethod);
            ForemanOperatingSystemWrapper fosw =
                    objectMapper.readValue(httpMethod.getResponseBody(), ForemanOperatingSystemWrapper.class);
            return mapOperationSystem(Arrays.asList(fosw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalComputeResource> runComputeResourceMethod(HttpMethod httpMethod) {
        try {
            runHttpMethod(httpClient, httpMethod);
            ForemanComputerResourceWrapper fcrw =
                    objectMapper.readValue(httpMethod.getResponseBody(), ForemanComputerResourceWrapper.class);
            return mapComputeResource(Arrays.asList(fcrw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    // Mapping
    private List<ExternalComputeResource> mapComputeResource(List<ForemanComputerResource> foremanCrs) {
        ArrayList<ExternalComputeResource> crs = new ArrayList<ExternalComputeResource>(foremanCrs.size());
        for (ForemanComputerResource cr : foremanCrs) {
            ExternalComputeResource computeResource = new ExternalComputeResource();
            computeResource.setName(cr.getName());
            computeResource.setUrl(cr.getUrl());
            computeResource.setId(cr.getId());
            computeResource.setProvider(cr.getProvider());
            computeResource.setUser(cr.getUser());
            crs.add(computeResource);
        }
        return crs;
    }

    private List<ExternalOperatingSystem> mapOperationSystem(List<ForemanOperatingSystem> foremanOss) {
        ArrayList<ExternalOperatingSystem> oss = new ArrayList<ExternalOperatingSystem>(foremanOss.size());
        for (ForemanOperatingSystem os : foremanOss) {
            ExternalOperatingSystem eos = new ExternalOperatingSystem();
            eos.setName(os.getName());
            eos.setId(os.getId());
            oss.add(eos);
        }
        return oss;
    }

    private List<ExternalDiscoveredHost> mapDiscoveredHosts(List<ForemanDiscoveredHost> foremanHosts) {
        ArrayList<ExternalDiscoveredHost> hosts = new ArrayList<ExternalDiscoveredHost>(foremanHosts.size());
        for (ForemanDiscoveredHost host : foremanHosts) {
            ExternalDiscoveredHost dhost = new ExternalDiscoveredHost();
            dhost.setName(host.getName());
            dhost.setIp(host.getIp());
            dhost.setMac(host.getMac());
            dhost.setLastReport(host.getLast_report());
            dhost.setSubnetName(host.getSubnet_name());
            hosts.add(dhost);
        }
        return hosts;
    }

    private List<VDS> mapHosts(List<ForemanHost> foremanHosts) {
        ArrayList<VDS> hosts = new ArrayList<VDS>(foremanHosts.size());
        for (ForemanHost host : foremanHosts) {
            VDS vds = new VDS();
            vds.setVdsName(host.getName());
            vds.setHostName(host.getName());
            hosts.add(vds);
        }
        return hosts;
    }

    private List<ExternalHostGroup> mapHostGroups(List<ForemanHostGroup> foremanHostGroups) {
        ArrayList<ExternalHostGroup> hostGroups = new ArrayList<ExternalHostGroup>(foremanHostGroups.size());

        for (ForemanHostGroup hostGroup : foremanHostGroups) {
            ExternalHostGroup hostgroup = new ExternalHostGroup();
            hostgroup.setHostgroupId(hostGroup.getId());
            hostgroup.setName(hostGroup.getName());
            hostgroup.setOsId(hostGroup.getOperatingsystem_id());
            hostgroup.setEnvironmentId(hostGroup.getEnvironment_id());
            hostgroup.setDomainId(hostGroup.getDomain_id());
            hostgroup.setSubnetId(hostGroup.getSubnet_id());
            hostgroup.setParameters(hostGroup.getParameters());
            hostgroup.setMediumId(hostGroup.getMedium_id());
            hostgroup.setArchitectureId(hostGroup.getArchitecture_id());
            hostgroup.setPtableId(hostGroup.getPtable_id());
            hostgroup.setOperatingsystemName(hostGroup.getOperatingsystem_name());
            hostgroup.setDomainName(hostGroup.getDomain_name());
            hostgroup.setSubnetName(hostGroup.getSubnet_name());
            hostgroup.setArchitectureName(hostGroup.getArchitecture_name());
            hostGroups.add(hostgroup);
        }
        return hostGroups;
    }

    @Override
    public List<VDS> getAll() {
        HttpMethod method = new GetMethod(ALL_HOSTS_QUERY);
        return runHostListMethod(method);
    }

    @Override
    public List<VDS> getFiltered(String filter) {
        String url = HOSTS_ENTRY_POINT + String.format(SEARCH_QUERY_FORMAT, filter);
        HttpMethod method = new GetMethod(url);
        return runHostListMethod(method);
    }

    @Override
    public List<ExternalDiscoveredHost> getDiscoveredHosts() {
        HttpMethod method = new GetMethod(DISCOVERED_HOSTS_ENTRY_POINT);
        return runDiscoveredHostListMethod(method);
    }

    @Override
    public List<ExternalHostGroup> getHostGroups() {
        HttpMethod method = new GetMethod(HOST_GROUPS_QUERY);
        return runHostGroupListMethod(method);
    }

    @Override
    public List<ExternalComputeResource> getComputeResources() {
        HttpMethod method = new GetMethod(COMPUTE_RESOURCES_HOSTS_ENTRY_POINT);
        return runComputeResourceMethod(method);
    }

    private List<ExternalOperatingSystem> getOperationSystems() {
        HttpMethod method = new GetMethod(OPERATION_SYSTEM_QUERY);
        return runOperationSystemMethod(method);
    }

    @Override
    public void provisionHost(VDS vds,
            ExternalHostGroup hg,
            ExternalComputeResource computeResource,
            String mac,
            String discoverName,
            String rootPassword,
            String ip) {
        final String entityBody = "{\n" +
                "    \"discovered_host\": {\n" +
                "        \"name\": \"" + vds.getName() + "\",\n" +
                "        \"hostgroup_id\": \"" + hg.getHostgroupId() + "\",\n" +
                "        \"environment_id\": \"" + hg.getEnvironmentId() + "\",\n" +
                "        \"mac\": \"" + mac + "\",\n" +
                "        \"domain_id\": \"" + hg.getDomainId() + "\",\n" +
                "        \"subnet_id\": \"" + hg.getSubnetId() + "\",\n" +
                "        \"ip\": \"" + ip + "\",\n" +
                "        \"architecture_id\": \"" + hg.getArchitectureId() + "\",\n" +
                "        \"operatingsystem_id\": \"" + hg.getOsId() + "\",\n" +
                "        \"medium_id\": \"" + hg.getMediumId() + "\",\n" +
                "        \"ptable_id\": \"" + hg.getPtableId() + "\",\n" +
                "        \"root_pass\": \"" + rootPassword + "\",\n" +
                "        \"host_parameters_attributes\": [\n" +
                "           {\n" +
                "                \"name\": \"host_ovirt_id\",\n" +
                "                \"value\": \"" + vds.getStaticData().getId() + "\",\n" +
                "                \"_destroy\": \"false\",\n" +
                "                \"nested\": \"\"\n" +
                "            },\n" +
                "           {\n" +
                "                \"name\": \"compute_resource_id\",\n" +
                "                \"value\": \"" + computeResource.getId() + "\",\n" +
                "                \"_destroy\": \"false\",\n" +
                "                \"nested\": \"\"\n" +
                "            },\n" +
                "           {\n" +
                "                \"name\": \"pass\",\n" +
                "                \"value\": \"" + CryptMD5.crypt(rootPassword) + "\",\n" +
                "                \"_destroy\": \"false\",\n" +
                "                \"nested\": \"\"\n" +
                "            },\n" +
                "           {\n" +
                "                \"name\": \"management\",\n" +
                "                \"value\": \"" + computeResource.getUrl().replaceAll("(http://|/api|/ovirt-engine)", "") + "\",\n" +
                "                \"_destroy\": \"false\",\n" +
                "                \"nested\": \"\"\n" +
                "            }\n" +
                "        ]\n" +
                "    }\n" +
                "}";
        PutMethod httpMethod = new PutMethod(DISCOVERED_HOSTS_ENTRY_POINT + "/" + discoverName);
        RequestEntity entity = new RequestEntity() {
            @Override
            public boolean isRepeatable() {
                return false;
            }

            @Override
            public void writeRequest(OutputStream outputStream) throws IOException {
                PrintWriter pr = new PrintWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8));
                pr.println(entityBody);
                pr.flush();
            }

            @Override
            public long getContentLength() {
                return entityBody.getBytes(StandardCharsets.UTF_8).length;
            }

            @Override
            public String getContentType() {
                return "application/json";
            }
        };
        httpMethod.setRequestEntity(entity);
        runHttpMethod(httpClient, httpMethod);
    }

    @Override
    public void testConnection() {
        HttpMethod httpMethod = new GetMethod(API_ENTRY_POINT);
        runHttpMethod(httpClient, httpMethod);

        // validate permissions to discovered host and host group.
        getDiscoveredHosts();
        getHostGroups();
    }

    @Override
    public void onAddition() {
    }

    @Override
    public void onModification() {
    }

    @Override
    public void onRemoval() {
    }

    private void runHttpMethod(HttpClient httpClient, HttpMethod httpMethod) {
        try {
            int result = httpClient.executeMethod(httpMethod);

            if (result == HttpURLConnection.HTTP_UNAUTHORIZED) {
                throw new VdcBLLException(VdcBllErrors.PROVIDER_AUTHENTICATION_FAILURE);
            }

            // after post request the return value is HTTP_MOVED_TEMP on success
            if (result != HttpURLConnection.HTTP_OK && result != HttpURLConnection.HTTP_MOVED_TEMP) {
                ForemanErrorWrapper ferr = objectMapper.readValue(httpMethod.getResponseBody(), ForemanErrorWrapper.class);
                String err = StringUtils.join(ferr.getForemanError().getFull_messages(), ", ");
                throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, err);
            }
        } catch (HttpException e) {
            handleException(e);
        } catch (SSLException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_SSL_FAILURE, e.getMessage());
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void initHttpClient() {
        try {
            URL hostUrl = getUrl();
            if (isSecured()) {
                int hostPort = hostUrl.getPort() == -1 ? HttpsURL.DEFAULT_PORT : hostUrl.getPort();
                Protocol httpsProtocol =
                        new Protocol(String.valueOf(HttpsURL.DEFAULT_SCHEME),
                                (ProtocolSocketFactory) new AuthSSLProtocolSocketFactory(ExternalTrustStoreInitializer.getTrustStore(), "SSLv3"),
                                hostPort);
                httpClient.getHostConfiguration().setHost(hostUrl.getHost(), hostPort, httpsProtocol);
            } else {
                int hostPort = hostUrl.getPort() == -1 ? HttpURL.DEFAULT_PORT : hostUrl.getPort();
                httpClient.getHostConfiguration().setHost(hostUrl.getHost(), hostPort);
            }
            objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            if (hostProvider.getUsername() != null && hostProvider.getPassword() != null) {
                Credentials hostProviderCredentials =
                        new UsernamePasswordCredentials(hostProvider.getUsername(), hostProvider.getPassword());
                httpClient.getState().setCredentials(AuthScope.ANY, hostProviderCredentials);
                // Required when working with foreman's /api rather than accessing directly to /hosts
                httpClient.getParams().setAuthenticationPreemptive(true);
            }
        } catch (RuntimeException e) {
            handleException(e);
        }
    }
}
