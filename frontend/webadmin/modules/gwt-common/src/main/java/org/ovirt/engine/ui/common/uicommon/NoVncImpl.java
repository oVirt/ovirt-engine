package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.ConfigurationValues;
import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.INoVnc;

import com.google.gwt.user.client.Window.Location;

public class NoVncImpl extends AbstractVnc implements INoVnc {

    private boolean listensOnPostMessage = false;
    private final NoVncProxyConfig config;
    private String connectionTicket;

    private String getTargetOrigin() {
        return "http://" + Location.getHost(); //$NON-NLS-1$
    }

    public String getProxyHost() {
        return config.getProxyHost();
    }

    public String getProxyPort() {
        return config.getProxyPort();
    }

    public void setListensOnPostMessage(boolean listensOnPostMessage) {
        this.listensOnPostMessage = listensOnPostMessage;
    }

    private String getClientUrl() {
        return "http://" + Location.getHost() + "/ovirt-engine-novnc-main.html?host=" + getProxyHost() + "&port=" + getProxyPort();//$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$
    }

    public NoVncImpl() {
        super();
        this.config = new NoVncProxyConfig((String) AsyncDataProvider.getConfigValuePreConverted(ConfigurationValues.WebSocketProxy));
    }

    @Override
    public void invokeClient() {
        AsyncQuery signCallback = new AsyncQuery();
        signCallback.setModel(this);
        signCallback.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object returnValue) {
                VdcQueryReturnValue queryRetVal = (VdcQueryReturnValue) returnValue;
                String signature = (String) queryRetVal.getReturnValue();
                setConnectionTicket(signature);

                invokeClientNative();
            }
        };

        Frontend.RunQuery(VdcQueryType.SignString, new SignStringParameters(createConnectionString()), signCallback); //$NON-NLS-1$
    }

    private native void invokeClientNative()/*-{
       if (this.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::isClientUnsupportedExplorer()()) {
           alert("NoVnc console is not supported on Internet Explorer < 10");
       }

       var that = this;

       function postVncMessage(target) {
           var dataToSend = {
             connectionTicket: that.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::getConnectionTicket()(),
             vncTicket: that.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::getTicket()()
           };

           var targetOrigin = that.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::getTargetOrigin()();
           target.postMessage(dataToSend, targetOrigin);
       }

       var clientUrl = this.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::getClientUrl()();

       if (!this.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::listensOnPostMessage) {
           this.@org.ovirt.engine.ui.common.uicommon.NoVncImpl::listensOnPostMessage = true;
           $wnd.addEventListener("message", postVncMessage, false);
       }

       var win = $wnd.open(clientUrl, "_blank");
       win.focus();

       setTimeout(function() {
         postVncMessage(win);
       }, 1000);

       }-*/;

    /**
     * Creates a string in following form:
     *   "vncHost:vncPort"
     */
    private String createConnectionString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getVncHost()).append(":")//$NON-NLS-1$
            .append(getVncPort());

        return sb.toString();
    }

    private void setConnectionTicket(String ticket) {
        this.connectionTicket = ticket;
    }

    private String getConnectionTicket() {
        return connectionTicket;
    }

    private boolean isClientUnsupportedExplorer() {
        return((Configurator)TypeResolver.getInstance().resolve(Configurator.class)).isClientWindowsExplorer();
    }


    /**
     * Holder for ws-proxy related configuration.
     */
    private class NoVncProxyConfig {
        private final String configValue;

        private static final String HOST = "Host";//$NON-NLS-1$
        private static final String ENGINE = "Engine";//$NON-NLS-1$

        public NoVncProxyConfig(String configValue) {
            if (configValue == null) {
                throw new IllegalArgumentException("Config value must not be null");//$NON-NLS-1$
            }

            this.configValue = configValue;
        }

        public String getProxyHost() {
            if (configValue.startsWith(HOST)) {
                return getVncHost(); //the proxy runs on same host as the vm
            } else if (configValue.startsWith(ENGINE)) {
                return Location.getHostName(); //the proxy runs on the engine
            } else if (matchesHostColonPort(configValue)) { //the proxy runs on specified host:port
                return configValue.split(":")[0];//$NON-NLS-1$
            } else {
                throw new IllegalArgumentException("Illegal NoVncImpl configuration.");//$NON-NLS-1$
            }
        }

        public String getProxyPort() {
            if (matchesHostColonPort(configValue)) {
                return configValue.split(":")[1];//$NON-NLS-1$
            }

            throw new IllegalStateException("Missing port in noVNC proxy config: " + configValue);//$NON-NLS-1$
        }

        private boolean matchesHostColonPort(String s) {
            if (s == null) {
                return false;
            }

            return s.matches("\\S+:\\d+");//$NON-NLS-1$
        }
    }

}

