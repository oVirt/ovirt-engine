package org.ovirt.engine.core.bll.host.provider.foreman;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
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
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.ovirt.engine.core.bll.host.provider.HostProviderProxy;
import org.ovirt.engine.core.bll.provider.BaseProviderProxy;
import org.ovirt.engine.core.bll.provider.ExternalTrustStoreInitializer;
import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.VdcBLLException;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.utils.ssl.AuthSSLProtocolSocketFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.DeserializationConfig.Feature;

public class ForemanHostProviderProxy extends BaseProviderProxy implements HostProviderProxy {

    private Provider hostProvider;

    private HttpClient httpClient = new HttpClient();

    private ObjectMapper objectMapper = new ObjectMapper();
    private static final String API_ENTRY_POINT = "/api";
    private static final String HOSTS_ENTRY_POINT = API_ENTRY_POINT + "/hosts";
    private static final String JSON_FORMAT = "format=json";
    private static final String ALL_HOSTS_QUERY = HOSTS_ENTRY_POINT + "?" + JSON_FORMAT;
    private static final String SEARCH_SECTION_FORMAT = "search=%1$s";
    private static final String SEARCH_QUERY_FORMAT = "?" + SEARCH_SECTION_FORMAT + "&" + JSON_FORMAT;

    public ForemanHostProviderProxy(Provider hostProvider) {
        super(hostProvider);
        this.hostProvider = hostProvider;
        initHttpClient(hostProvider.getUrl());
    }

    @Override
    public List<VDS> getAll() {
        HttpMethod method = new GetMethod(ALL_HOSTS_QUERY);
        return runHostListMethod(method);
    }

    private List<VDS> runHostListMethod(HttpMethod httpMethod) {
        try{
            runHttpMethod(httpClient, httpMethod);
            ForemanHostWrapper[] hosts = objectMapper.readValue(httpMethod.getResponseBody(), ForemanHostWrapper[].class);
            return map(Arrays.asList(hosts));
        } catch (JsonParseException e) {
            handleException(e);
        } catch (JsonMappingException e) {
            handleException(e);
        } catch (IOException e) {
            handleException(e);
        }
        return null;
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
        String url = HOSTS_ENTRY_POINT + String.format(SEARCH_QUERY_FORMAT, filter);
        HttpMethod method = new GetMethod(url);
        return runHostListMethod(method);
    }

    @Override
    public void testConnection() {
        HttpMethod httpMethod = new GetMethod(API_ENTRY_POINT);
        runHttpMethod(httpClient, httpMethod);
    }

    private void runHttpMethod(HttpClient httpClient, HttpMethod httpMethod) {
        try {
            int result = httpClient.executeMethod(httpMethod);

            if (result != HttpURLConnection.HTTP_OK) {
                throw new VdcBLLException(VdcBllErrors.PROVIDER_FAILURE);
            }
        } catch (HttpException e) {
            handleException(e);
        } catch (SSLException e) {
            throw new VdcBLLException(VdcBllErrors.PROVIDER_SSL_FAILURE, e.getMessage());
        } catch (IOException e) {
            handleException(e);
        }
    }

    private void initHttpClient(String hostUrlString) {
        try {
            URL hostUrl = getUrl();
            if (isSecured()) {
                int hostPort = hostUrl.getPort() == -1 ? HttpsURL.DEFAULT_PORT : hostUrl.getPort();
                Protocol httpsProtocol = new Protocol(String.valueOf(HttpsURL.DEFAULT_SCHEME), (ProtocolSocketFactory) new AuthSSLProtocolSocketFactory(ExternalTrustStoreInitializer.getTrustStore()),  hostPort);
                httpClient.getHostConfiguration().setHost(hostUrl.getHost(), hostPort, httpsProtocol);
            } else {
                int hostPort = hostUrl.getPort() == -1 ? HttpURL.DEFAULT_PORT : hostUrl.getPort();
                httpClient.getHostConfiguration().setHost(hostUrl.getHost(), hostPort);
            }
            objectMapper.configure(Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            Credentials hostProviderCredentials = new UsernamePasswordCredentials(hostProvider.getUsername(), hostProvider.getPassword());
            httpClient.getState().setCredentials(AuthScope.ANY, hostProviderCredentials);
            // Required when working with foreman's /api rather than accessing directly to /hosts
            httpClient.getState().setAuthenticationPreemptive(true);
        } catch (RuntimeException e) {
            handleException(e);
        }
    }
}
