package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.core.common.queries.SignStringParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ErrorPopupManager;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIConstants;

import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;

public class WebClientConsoleInvoker {

    private static final UIConstants constants = ConstantsManager.getInstance().getConstants();
    private final String clientPage;
    private final WebsocketProxyConfig proxyConfig;
    private final String password;
    private final boolean useSsl;
    private final String host;
    private final String port;
    private final String consoleTitle;

    public WebClientConsoleInvoker(String clientPage, WebsocketProxyConfig proxyConfig, String host, Integer port, String password, boolean useSsl, String consoleTitle) {
        this.clientPage = clientPage;
        this.proxyConfig = proxyConfig;
        this.host = host;
        this.password = password;
        this.useSsl = useSsl;
        this.port = (port == null) ? null : port.toString();
        this.consoleTitle = consoleTitle;
    }

    public void invokeClient() {
        Frontend.getInstance().runQuery(VdcQueryType.SignString,
                new SignStringParameters(createConnectionString(host, port, useSsl)),
                new AsyncQuery<>((AsyncCallback<VdcQueryReturnValue>) returnValue -> {
                    String signedTicket = returnValue.getReturnValue();
                    invokeClientNative(signedTicket);
                }));
    }

    private void openingWindowFailed() {
        final ErrorPopupManager popupManager =
                (ErrorPopupManager) TypeResolver.getInstance().resolve(ErrorPopupManager.class);
        popupManager.show(constants.openingNewConsoleWindowFailed());
    }

    private native void invokeClientNative(String connectionTicket)/*-{
        if (!this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::isClientBrowserSupported()()) {
            alert("This feature is not supported in your browser.");
        }

        var that = this;
        var clientUrl = this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::createClientUrl()();
        var win = $wnd.open(clientUrl, "_blank");

        if (win === null) {
            this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::openingWindowFailed()();
            return;
        }

        win.addEventListener('load', function () {
            var dataToSend = {
                connectionTicket: connectionTicket,
                password: that.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::password
            };

            win.postMessage(dataToSend, clientUrl);
        }, false);
        win.focus();
    }-*/;

   /**
     * Creates an urlencoded json object that represent target endpoint.
     * @return encoded json object that holds host, port and sslTarget information.
     */
   private static String createConnectionString(String host, String port, boolean sslTarget) {
        return URL.encode(createConnectionJsonString(host, port, sslTarget));
   }

    /**
     * Helper method for creating json object out of host, port and sslTarget
     */
    private static native String createConnectionJsonString(String host, String port, boolean sslTarget)/*-{
        return JSON.stringify({
            "host": host,
            "port": port,
            "ssl_target": sslTarget
            });
    }-*/;

    private String createClientUrl() {
        return Window.Location.getProtocol() + "//" + Window.Location.getHost() + //$NON-NLS-1$
            "/" + clientPage + //$NON-NLS-1$
            "?host=" + proxyConfig.getProxyHost() + //$NON-NLS-1$
            "&port=" + proxyConfig.getProxyPort() + //$NON-NLS-1$
            "&title=" + consoleTitle;               //$NON-NLS-1$
    }

    private boolean isClientBrowserSupported() {
        boolean isExplorer = ((Configurator) TypeResolver.getInstance().resolve(Configurator.class)).isClientWindowsExplorer();
        Float browserVersion = ((Configurator) TypeResolver.getInstance().resolve(Configurator.class)).clientBrowserVersion();

        return isExplorer ? (browserVersion >= 11f) : true;
    }
}
