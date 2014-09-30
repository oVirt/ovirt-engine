package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URLEncoder;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.DeserializationConfig.Feature;
import org.codehaus.jackson.map.ObjectMapper;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.BaseProviderProxy;
import org.ovirt.engine.core.common.businessentities.ExternalComputeResource;
import org.ovirt.engine.core.common.businessentities.ExternalDiscoveredHost;
import org.ovirt.engine.core.common.businessentities.ExternalHostGroup;
import org.ovirt.engine.core.common.businessentities.ExternalOperatingSystem;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.uutils.crypto.CryptMD5;

public class ForemanHostProviderProxy extends BaseProviderProxy implements HostProviderProxy {

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

    public ForemanHostProviderProxy(Provider hostProvider) {
        super(hostProvider);
        objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    private byte[] runHttpGetMethod(String relativeUrl) {
        return runHttpMethod(
                HttpMethodType.GET,
                "application/json; charset=utf-8",
                relativeUrl,
                null);
    }

    private List<VDS> runHostListMethod(String relativeUrl) {
        try {
            ForemanHostWrapper fhw = objectMapper.readValue(runHttpGetMethod(relativeUrl), ForemanHostWrapper.class);
            return mapHosts(Arrays.asList(fhw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalDiscoveredHost> runDiscoveredHostListMethod(String relativeUrl) {
        try {
            ForemanDiscoveredHostWrapper fdw =
                    objectMapper.readValue(runHttpGetMethod(relativeUrl), ForemanDiscoveredHostWrapper.class);
            return mapDiscoveredHosts(Arrays.asList(fdw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalHostGroup> runHostGroupListMethod(String relativeUrl) {
        try {
            ForemanHostGroupWrapper fhgw =
                    objectMapper.readValue(runHttpGetMethod(relativeUrl), ForemanHostGroupWrapper.class);
            return mapHostGroups(Arrays.asList(fhgw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalOperatingSystem> runOperationSystemMethod(String relativeUrl) {
        try {
            ForemanOperatingSystemWrapper fosw =
                    objectMapper.readValue(runHttpGetMethod(relativeUrl), ForemanOperatingSystemWrapper.class);
            return mapOperationSystem(Arrays.asList(fosw.getResults()));
        } catch (IOException e) {
            return null;
        }
    }

    private List<ExternalComputeResource> runComputeResourceMethod(String relativeUrl) {
        try {
            ForemanComputerResourceWrapper fcrw =
                    objectMapper.readValue(runHttpGetMethod(relativeUrl), ForemanComputerResourceWrapper.class);
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
        return runHostListMethod(ALL_HOSTS_QUERY);
    }

    @Override
    public List<VDS> getFiltered(String filter) {
        return runHostListMethod(HOSTS_ENTRY_POINT + String.format(SEARCH_QUERY_FORMAT, filter));
    }

    @Override
    public List<ExternalDiscoveredHost> getDiscoveredHosts() {
        return runDiscoveredHostListMethod(DISCOVERED_HOSTS_ENTRY_POINT);
    }

    @Override
    public List<ExternalHostGroup> getHostGroups() {
        return runHostGroupListMethod(HOST_GROUPS_QUERY);
    }

    @Override
    public List<ExternalComputeResource> getComputeResources() {
        return runComputeResourceMethod(COMPUTE_RESOURCES_HOSTS_ENTRY_POINT);
    }

    private List<ExternalOperatingSystem> getOperationSystems() {
        return runOperationSystemMethod(OPERATION_SYSTEM_QUERY);
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
        runHttpMethod(
                HttpMethodType.PUT,
                "application/json; charset=utf-8",
                Paths.get(DISCOVERED_HOSTS_ENTRY_POINT, discoverName).toString(),
                entityBody
                );
    }


    protected ConnectionWrapper createWrapper(HttpURLConnection result) {
        return new ConnectionWrapper(result) {

            @Override
            public void beforeReadResponse() throws Exception {

            }

            @Override
            public void afterReadResponse() throws Exception {
                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK
                        && connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                    ForemanErrorWrapper ferr = objectMapper.readValue(response, ForemanErrorWrapper.class);
                    String err = StringUtils.join(ferr.getForemanError().getFull_messages(), ", ");
                    throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, err);
                }
            }
        };
    }

    @Override
    public void testConnection() {
        runHttpGetMethod(API_ENTRY_POINT);

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


}
