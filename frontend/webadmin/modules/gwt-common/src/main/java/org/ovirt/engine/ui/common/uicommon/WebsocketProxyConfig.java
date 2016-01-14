package org.ovirt.engine.ui.common.uicommon;

import com.google.gwt.user.client.Window.Location;

/**
 * Holder for ws-proxy related configuration.
 */
public class WebsocketProxyConfig {
    private final String configValue;
    private String targetHost;

    private static final String ENGINE = "Engine";//$NON-NLS-1$
    private static final String HOST = "Host";//$NON-NLS-1$

    /**
     * @param configValue - websocket proxy config string
     * @param targetHost - this is used only when the proxy is deployed on each host
     */
    public WebsocketProxyConfig(String configValue, String targetHost) {
        if (!matchesHostColonPort(configValue)) {
            throw new IllegalArgumentException("Config value must be in following form: host:port");//$NON-NLS-1$
        }

        this.configValue = configValue;
        this.targetHost = targetHost;
    }

    public String getProxyHost() {
        String val = configValue.split(":")[0];//$NON-NLS-1$
        if (val.equals(ENGINE)) {
            return Location.getHostName(); //the proxy runs on the engine
        } else if (val.equals(HOST)) {
            return this.targetHost; //the proxy runs on the engine
        }
        return val;
    }

    public String getProxyPort() {
        return configValue.split(":")[1];//$NON-NLS-1$
    }

    private boolean matchesHostColonPort(String s) {
        return s == null ? false : s.matches("\\S+:\\d+"); //$NON-NLS-1$
    }
}
