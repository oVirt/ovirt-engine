package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.httpclient.Credentials;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.httpclient.methods.GetMethod;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

public class ForemanHostProviderProxy implements HostProviderProxy {

    private Provider hostProvider;

    private HttpClient httpClient = new HttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String ERROR_MESSAGE = "Failed connecting to provider. ";
    private static final String API_ENTRY_POINT = "/api";
    private static final String HOSTS_ENTRY_POINT = API_ENTRY_POINT + "/hosts";
    private static final String JSON_FORMAT = "format=json";
    private static final String ALL_HOSTS_QUERY = HOSTS_ENTRY_POINT + "?" + JSON_FORMAT;
    private static final String SEARCH_SECTION_FORMAT = "search=%1$s";
    private static final String SEARCH_QUERY_FORMAT = "?" + SEARCH_SECTION_FORMAT + "&" + JSON_FORMAT;

    public ForemanHostProviderProxy(Provider hostProvider) {
        this.hostProvider = hostProvider;
        objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Credentials hostProviderCredentials = new UsernamePasswordCredentials(hostProvider.getUsername(), hostProvider.getPassword());
        httpClient.getState().setCredentials(AuthScope.ANY, hostProviderCredentials);
        // Required when working with foreman's /api rather than accessing directly to /hosts
        httpClient.getState().setAuthenticationPreemptive(true);
    }

    @Override
    public List<VDS> getAll() {
        HttpMethod method = new GetMethod(hostProvider.getUrl() + ALL_HOSTS_QUERY);
        return runHostListMethod(method);
    }

    private List<VDS> runHostListMethod(HttpMethod httpMethod) {
        try{
            runHttpMethod(httpClient, httpMethod);
            ForemanHostWrapper[] hosts = objectMapper.readValue(httpMethod.getResponseBody(), ForemanHostWrapper[].class);

            return map(Arrays.asList(hosts));
        } catch (JsonParseException e) {
            throw new RuntimeException(ERROR_MESSAGE + e.getMessage());
        } catch (JsonMappingException e) {
            throw new RuntimeException(ERROR_MESSAGE + e.getMessage());
        } catch (IOException e) {
            throw new RuntimeException(ERROR_MESSAGE + e.getMessage());
        }
    }

    private List<VDS> map(List<ForemanHostWrapper> foremanHosts) {
        ArrayList<VDS> hosts = new ArrayList<VDS>(foremanHosts.size());

        for (ForemanHostWrapper host : foremanHosts) {
            VDS vds = new VDS();
            String hostName = host.getHost().getName();
            vds.setVdsName(hostName);
            vds.setHostName(hostName);
            hosts.add(vds);
        }

        return hosts;
    }

    @Override
    public List<VDS> getFiltered(String filter) {
        String url = hostProvider.getUrl() + HOSTS_ENTRY_POINT + String.format(SEARCH_QUERY_FORMAT, filter);
        HttpMethod method = new GetMethod(url);
        return runHostListMethod(method);
    }

    @Override
    public void testConnection() {
        HttpMethod httpMethod = new GetMethod(hostProvider.getUrl());
        runHttpMethod(httpClient, httpMethod);
    }

    private void runHttpMethod(HttpClient httpClient, HttpMethod httpMethod) {
        try {
            int result = httpClient.executeMethod(httpMethod);

            if (result != HttpURLConnection.HTTP_OK) {
                throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE);
            }
        } catch (HttpException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e.getMessage());
        } catch (IOException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE, e.getMessage());
        }
    }
}
