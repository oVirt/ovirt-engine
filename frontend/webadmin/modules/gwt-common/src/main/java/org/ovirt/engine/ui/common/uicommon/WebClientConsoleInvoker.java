package org.ovirt.engine.ui.common.uicommon;

import com.google.gwt.http.client.URL;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;

public class WebClientConsoleInvoker {

    private final String connectionTicket;
    private final String password;
    private final String clientUrl;
    private boolean listensOnPostMessage = false;

    public WebClientConsoleInvoker(String connectionTicket, String password, String clientUrl) {
        this.connectionTicket = connectionTicket;
        this.password = password;
        this.clientUrl = clientUrl;
    }

    /**
     * Creates an urlencoded json object that represent target endpoint.
     * @params host, port, sslTarget
     * @return encoded json object that holds host, port and sslTarget information.
     */
    public static String createConnectionString(String host, String port, boolean sslTarget) {
        return URL.encode(createConnectionJsonString(host, port, sslTarget));
    }

    /**
     * Helper method for creating json object out of host, port and sslTarget
     */
    private native static String createConnectionJsonString(String host, String port, boolean sslTarget)/*-{
        return JSON.stringify({
            "host": host,
            "port": port,
            "ssl_target": sslTarget
            });
    }-*/;

    public native void invokeClientNative()/*-{
       if (this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::isClientBrowserUnsupported()()) {
           alert("This feature is not supported in your browser.");
       }

       var that = this;

       var clientUrl = this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::clientUrl;

       function postMessage(target) {
           var dataToSend = {
             connectionTicket: that.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::connectionTicket,
             password: that.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::password
           };

           target.postMessage(dataToSend, clientUrl);
       }


       if (!this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::listensOnPostMessage) {
           this.@org.ovirt.engine.ui.common.uicommon.WebClientConsoleInvoker::listensOnPostMessage = true;
           $wnd.addEventListener("message", postMessage, false);
       }

       var win = $wnd.open(clientUrl, "_blank");
       win.focus();

       setTimeout(function() {
         postMessage(win);
       }, 1000);

       }-*/;

    private boolean isClientBrowserUnsupported() {
        return ((Configurator) TypeResolver.getInstance().resolve(Configurator.class)).isClientWindowsExplorer();
    }

    public void setListensOnPostMessage(boolean val) {
        this.listensOnPostMessage = val;
    }

}
