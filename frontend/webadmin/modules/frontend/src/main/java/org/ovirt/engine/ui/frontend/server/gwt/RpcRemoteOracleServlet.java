package org.ovirt.engine.ui.frontend.server.gwt;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;

import org.apache.http.client.utils.URIBuilder;

import com.google.gwt.rpc.server.RpcServlet;
import com.google.gwt.user.client.rpc.SerializationException;

/**
 * Extension of {@link RpcServlet} that supports both Classic and Super Dev Mode.
 */
public abstract class RpcRemoteOracleServlet extends RpcServlet {

    /**
     * Options for Super Dev Mode loaded from META-INF/gwt-super-dev.properties file.
     */
    private static class SuperDevConfig {

        static final String KEY_PREFIX = "gwt.superDev."; //$NON-NLS-1$

        final boolean enabled;
        final String host;
        final int port;
        final int connectTimeout;
        final int readTimeout;

        SuperDevConfig(boolean enabled, String host, int port, int connectTimeout, int readTimeout) {
            this.enabled = enabled;
            this.host = host;
            this.port = port;
            this.connectTimeout = connectTimeout;
            this.readTimeout = readTimeout;
        }

    }

    private static final long serialVersionUID = -2475477364514562504L;

    private static final String APP_NAME = "applicationName"; //$NON-NLS-1$

    private static final String SUPER_DEV_CONFIG_FILE = "META-INF/gwt-super-dev.properties"; //$NON-NLS-1$
    private static final String GWT_RPC_EXT = ".gwt.rpc"; //$NON-NLS-1$
    private static final String SLASH = "/"; //$NON-NLS-1$

    private SuperDevConfig superDevConfig;

    public RpcRemoteOracleServlet() {
        super();
    }

    public RpcRemoteOracleServlet(Object delegate) {
        super(delegate);
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        super.init(config);

        try (InputStream superDevConfigInput = RpcRemoteOracleServlet.this.getClass()
                .getClassLoader().getResourceAsStream(SUPER_DEV_CONFIG_FILE)) {
            Properties superDevConfigProps = new Properties();
            superDevConfigProps.load(superDevConfigInput);

            superDevConfig = new SuperDevConfig(
                    readBooleanConfigKey(superDevConfigProps, "enabled"), //$NON-NLS-1$
                    readConfigKey(superDevConfigProps, "host"), //$NON-NLS-1$
                    readIntConfigKey(superDevConfigProps, "port"), //$NON-NLS-1$
                    readIntConfigKey(superDevConfigProps, "connectTimeout"), //$NON-NLS-1$
                    readIntConfigKey(superDevConfigProps, "readTimeout") //$NON-NLS-1$
            );
        } catch (IOException e) {
            throw new ServletException(e);
        }
    }

    String readConfigKey(Properties props, String key) {
        return props.getProperty(SuperDevConfig.KEY_PREFIX + key);
    }

    boolean readBooleanConfigKey(Properties props, String key) {
        return Boolean.parseBoolean(readConfigKey(props, key));
    }

    int readIntConfigKey(Properties props, String key) {
        return Integer.parseInt(readConfigKey(props, key));
    }

    @Override
    protected InputStream findClientOracleData(String requestModuleBasePath,
            String permutationStrongName) throws SerializationException {
        String applicationName = getServletContext().getInitParameter(APP_NAME);

        try {
            // Retrieve ClientOracle data as resource from ServletContext (Classic Dev Mode)
            return super.findClientOracleData(requestModuleBasePath, permutationStrongName);
        } catch (SerializationException e) {
            // Resource missing, try to retrieve ClientOracle data from URL (Super Dev Mode)
            if (superDevConfig != null && superDevConfig.enabled) {
                return findRemoteOracleData(getSuperDevOracleUrl(applicationName, permutationStrongName));
            }
            throw e;
        }
    }

    String getSuperDevOracleUrl(String applicationName, String permutationStrongName) {
        URIBuilder builder = new URIBuilder();
        builder.setScheme("http"); //$NON-NLS-1$
        builder.setHost(superDevConfig.host);
        builder.setPort(superDevConfig.port);
        builder.setPath(SLASH + applicationName + SLASH + permutationStrongName + GWT_RPC_EXT);
        return builder.toString();
    }

    InputStream findRemoteOracleData(String url) {
        URLConnection conn;

        try {
            conn = new URL(url).openConnection();
            conn.setConnectTimeout(superDevConfig.connectTimeout);
            conn.setReadTimeout(superDevConfig.readTimeout);

            // Super Dev Mode code server doesn't redirect.
            // Fail fast if we get a redirect since it's likely a configuration error.
            if (conn instanceof HttpURLConnection) {
                ((HttpURLConnection) conn).setInstanceFollowRedirects(false);
            }

            conn.connect();
            return conn.getInputStream();
        } catch (MalformedURLException e) {
            return null;
        } catch (IOException e) {
            return null;
        }
    }

}
