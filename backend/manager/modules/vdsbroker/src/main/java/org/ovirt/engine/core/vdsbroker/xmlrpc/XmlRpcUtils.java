package org.ovirt.engine.core.vdsbroker.xmlrpc;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethodRetryHandler;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.params.HttpClientParams;
import org.apache.commons.httpclient.params.HttpConnectionManagerParams;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.apache.commons.httpclient.protocol.ProtocolSocketFactory;
import org.apache.commons.lang.StringUtils;
import org.apache.xmlrpc.XmlRpcRequest;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;
import org.apache.xmlrpc.client.XmlRpcClientException;
import org.apache.xmlrpc.client.XmlRpcCommonsTransport;
import org.apache.xmlrpc.client.XmlRpcCommonsTransportFactory;
import org.apache.xmlrpc.client.XmlRpcTransport;
import org.apache.xmlrpc.client.util.ClientFactory;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.utils.CorrelationIdTracker;
import org.ovirt.engine.core.utils.crypt.EngineEncryptionUtils;
import org.ovirt.engine.core.utils.ssl.AuthSSLProtocolSocketFactory;
import org.ovirt.engine.core.utils.threadpool.ThreadPoolUtil;
import org.ovirt.engine.core.vdsbroker.vdsbroker.FutureCall;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class XmlRpcUtils {

    private static final String HTTP = "http://";
    private static final String HTTPS = "https://";
    private static final Logger log = LoggerFactory.getLogger(XmlRpcUtils.class);
    static {
        if (Config.<Boolean> getValue(ConfigValues.EncryptHostCommunication)) {
            URL keystoreUrl;
            try {
                // registering the https protocol with a socket factory that
                // provides client authentication.
                ProtocolSocketFactory factory = new AuthSSLProtocolSocketFactory(EngineEncryptionUtils.getKeyManagers(),
                    EngineEncryptionUtils.getTrustManagers());
                Protocol clientAuthHTTPS = new Protocol("https", factory, 54321);
                Protocol.registerProtocol("https", clientAuthHTTPS);
            } catch (Exception e) {
                log.error("Failed to init AuthSSLProtocolSocketFactory. SSL connections will not work", e);
            }
        }
    }

    /**
     * wrapper for the apache xmlrpc client factory. gets the xmlrpc connection parameters and an interface to implement
     * and returns an instance which implements the given interface.
     * @param <T>
     *            the type of the instance for the interface
     * @param hostName
     *            - the host to connect to
     * @param port
     *            - the port to connect to
     * @param clientTimeOut
     *            - the time out for the connection
     * @param type
     *            - the instance type of the interface for this connection
     * @param isSecure
     *            - if a connection should be https or http
     * @return an instance of the given type.
     */
    public static <T> Pair<T, HttpClient> getConnection(String hostName, int port, int clientTimeOut,
            int connectionTimeOut, int clientRetries, Class<T> type, boolean isSecure) {
        Pair<String, URL> urlInfo = getConnectionUrl(hostName, port, null, isSecure);
        if (urlInfo == null) {
            return null;
        }

        return getHttpConnection(urlInfo.getSecond(), clientTimeOut, connectionTimeOut, clientRetries, type);
    }

    public static Pair<String, URL> getConnectionUrl(String hostName, int port, String path, boolean isSecure) {
        URL serverUrl;
        String prefix;
        String url;
        if (isSecure) {
            prefix = HTTPS;
        } else {
            prefix = HTTP;
        }
        try {
            url = prefix + hostName + ":" + port + (path != null ? "/" + path : "");
            serverUrl = new URL(url);
        } catch (MalformedURLException mfue) {
            log.error("failed to form the xml-rpc url", mfue);
            return null;
        }

        return new Pair<>(url, serverUrl);
    }

    public static void shutDownConnection(HttpClient httpClient) {
        if (httpClient != null && httpClient.getHttpConnectionManager() != null) {
            ((MultiThreadedHttpConnectionManager) (httpClient).getHttpConnectionManager()).shutdown();
        }
    }

    @SuppressWarnings("unchecked")
    private static <T> Pair<T, HttpClient> getHttpConnection(URL serverUrl, int clientTimeOut,
            int connectionTimeOut, int clientRetries, Class<T> type) {
        XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
        config.setServerURL(serverUrl);
        config.setConnectionTimeout(connectionTimeOut);
        config.setReplyTimeout(clientTimeOut);
        XmlRpcClient xmlRpcClient = new XmlRpcClient();
        xmlRpcClient.setConfig(config);

        XmlRpcCommonsTransportFactory transportFactory = new CustomXmlRpcCommonsTransportFactory(xmlRpcClient);
        HttpClient httpclient = createHttpClient(clientRetries, connectionTimeOut);
        transportFactory.setHttpClient(httpclient);
        xmlRpcClient.setTransportFactory(transportFactory);

        ClientFactory clientFactory = new ClientFactory(xmlRpcClient);
        T connector = (T) clientFactory.newInstance(Thread.currentThread().getContextClassLoader(), type, null);
        T asyncConnector = (T) AsyncProxy.newInstance(connector, clientTimeOut);

        Pair<T, HttpClient> returnValue =
                new Pair<T, HttpClient>(asyncConnector, httpclient);

        return returnValue;
    }

    private static HttpClient createHttpClient(int clientRetries, int connectionTimeout) {
        HttpConnectionManagerParams params = new HttpConnectionManagerParams();
        params.setConnectionTimeout(connectionTimeout);
        MultiThreadedHttpConnectionManager httpConnectionManager = new MultiThreadedHttpConnectionManager();
        httpConnectionManager.setParams(params);
        // Create the client:
        HttpClient client = new HttpClient(httpConnectionManager);

        // Configure the HTTP client so it will retry the execution of
        // methods when there are IO errors:
        int retries = Config.<Integer> getValue(ConfigValues.vdsRetries);
        HttpMethodRetryHandler handler = new DefaultHttpMethodRetryHandler(retries, false);
        HttpClientParams parameters = client.getParams();
        parameters.setParameter(HttpMethodParams.RETRY_HANDLER, handler);

        // Done:
        return client;
    }

    private static class AsyncProxy implements InvocationHandler {

        private Object obj;
        private long timeoutInMilisec;

        public static Object newInstance(Object obj, long timeoutInMilisec) {
            return Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(),
                    obj.getClass().getInterfaces(),
                    new AsyncProxy(obj, timeoutInMilisec));
        }

        private AsyncProxy(Object obj, long timeoutInMilisec) {
            this.obj = obj;
            this.timeoutInMilisec = timeoutInMilisec;
        }

        @Override
        public Object invoke(Object proxy, final Method m, final Object[] args)
                throws Throwable {
            Object result;
            FutureTask<Object> future;
            FutureCall annotation = m.getAnnotation(FutureCall.class);
            if (annotation != null) {
                future =
                        new FutureTask<Object>(createCallable(obj,
                                getMethod(m, annotation, proxy),
                                args,
                                CorrelationIdTracker.getCorrelationId()));
                ThreadPoolUtil.execute(future);
                return future;
            } else {
                future =
                        new FutureTask<Object>(createCallable(obj,
                                m,
                                args,
                                CorrelationIdTracker.getCorrelationId()));
                ThreadPoolUtil.execute(future);
                try {
                    result = future.get(timeoutInMilisec, TimeUnit.MILLISECONDS);
                } catch (Exception e) {
                    if (e instanceof TimeoutException) {
                        future.cancel(true);
                    }
                    throw new UndeclaredThrowableException(e);
                }

                return result;
            }
        }

        private Method getMethod(Method m, FutureCall annotation, Object proxy)
                throws SecurityException, NoSuchMethodException {
            if (!annotation.delegeteTo().isEmpty()) {
                return proxy.getClass().getDeclaredMethod(annotation.delegeteTo(), m.getParameterTypes());
            }
            return m;
        }

        private Callable<Object> createCallable(Object obj, Method m, Object[] args, String correlationId) {
            return new InternalCallable(obj, m, args, correlationId);
        }

        private static final class InternalCallable implements Callable<Object> {

            private Object obj;
            private Method m;
            private Object[] args;
            private String correlationId;

            public InternalCallable(Object obj, Method m, Object[] args, String correlationId) {
                this.obj = obj;
                this.m = m;
                this.args = args;
                this.correlationId = correlationId;
            }

            @Override
            public Object call() throws Exception {
                try {
                    CorrelationIdTracker.setCorrelationId(correlationId);
                    return m.invoke(obj, args);
                } catch (Exception e) {
                    throw e;
                }
            }

        }

    }

    /**
     * Designed to extend the request header with a value specifying the correlation ID to propagate to VDSM.
     */
    private static class CustomXmlRpcCommonsTransport extends XmlRpcCommonsTransport {

        private static final String FLOW_ID_HEADER_NAME = "FlowID";

        public CustomXmlRpcCommonsTransport(XmlRpcCommonsTransportFactory pFactory) {
            super(pFactory);
        }

        @Override
        protected void initHttpHeaders(XmlRpcRequest pRequest) throws XmlRpcClientException {
            super.initHttpHeaders(pRequest);

            String correlationId = CorrelationIdTracker.getCorrelationId();
            if (StringUtils.isNotBlank(correlationId)) {
                method.setRequestHeader(FLOW_ID_HEADER_NAME, correlationId);
            }
        }
    };

    /**
     * Designed to to override the factory with a customized transport.
     */
    private static class CustomXmlRpcCommonsTransportFactory extends XmlRpcCommonsTransportFactory {

        public CustomXmlRpcCommonsTransportFactory(XmlRpcClient pClient) {
            super(pClient);
        }

        @Override
        public XmlRpcTransport getTransport() {
            return new CustomXmlRpcCommonsTransport(this);
        }

    };

}
