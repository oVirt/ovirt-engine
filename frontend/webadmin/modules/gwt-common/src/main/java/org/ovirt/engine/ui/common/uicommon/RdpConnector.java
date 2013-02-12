package org.ovirt.engine.ui.common.uicommon;

import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicompat.Event;

import com.google.gwt.core.client.GWT;

public class RdpConnector {
    private String server;
    private Boolean fullScreen = true;
    private String fullScreenTitle;
    private Integer width = 640;
    private Integer height = 480;
    private Integer authenticationLevel = 2;
    private Boolean enableCredSspSupport = false; // Disable 'Credential Security Support Provider (CredSSP)' to enable
                                                  // SSO.
    private Boolean redirectDrives = false;
    private Boolean redirectPrinters = false;
    private Boolean redirectClipboard = false;
    private Boolean redirectSmartCards = false;

    private Event disconnectedEvent;

    public RdpConnector() {

    }

    public RdpConnector(String server, Event disconnectedEvent) {
        setServer(server);
        setDisconnectedEvent(disconnectedEvent);
    }

    public Event getDisconnectedEvent() {
        return disconnectedEvent;
    }

    public void setDisconnectedEvent(Event disconnectedEvent) {
        this.disconnectedEvent = disconnectedEvent;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public Boolean getFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(Boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public String getFullScreenTitle() {
        return fullScreenTitle;
    }

    public void setFullScreenTitle(String fullScreenTitle) {
        this.fullScreenTitle = fullScreenTitle;
    }

    public Integer getWidth() {
        return width;
    }

    public void setWidth(Integer width) {
        this.width = width;
    }

    public Integer getHeight() {
        return height;
    }

    public void setHeight(Integer height) {
        this.height = height;
    }

    public Integer getAuthenticationLevel() {
        return authenticationLevel;
    }

    public void setAuthenticationLevel(Integer authenticationLevel) {
        this.authenticationLevel = authenticationLevel;
    }

    public Boolean getEnableCredSspSupport() {
        return enableCredSspSupport;
    }

    public void setEnableCredSspSupport(Boolean enableCredSspSupport) {
        this.enableCredSspSupport = enableCredSspSupport;
    }

    public Boolean getRedirectDrives() {
        return redirectDrives;
    }

    public void setRedirectDrives(Boolean redirectDrives) {
        this.redirectDrives = redirectDrives;
    }

    public Boolean getRedirectPrinters() {
        return redirectPrinters;
    }

    public void setRedirectPrinters(Boolean redirectPrinters) {
        this.redirectPrinters = redirectPrinters;
    }

    public Boolean getRedirectClipboard() {
        return redirectClipboard;
    }

    public void setRedirectClipboard(Boolean redirectClipboard) {
        this.redirectClipboard = redirectClipboard;
    }

    public Boolean getRedirectSmartCards() {
        return redirectSmartCards;
    }

    public void setRedirectSmartCards(Boolean redirectSmartCards) {
        this.redirectSmartCards = redirectSmartCards;
    }

    public String getRdpCabURL() {
        return GWT.getModuleBaseURL() + "msrdp.cab";//$NON-NLS-1$
    }

    public String getVdcUserNameAndDomain() {
        String username = Frontend.getLoggedInUser().getUserName();
        String domain = Frontend.getLoggedInUser().getDomainControler();

        return username.contains("@") ? username : username + "@" + domain;//$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getVdcUserName() {
        return Frontend.getLoggedInUser().getUserName();
    }

    public String getVdcUserPassword() {
        String password = Frontend.getLoggedInUser().getPassword();
        return password == null ? "" : password;
    }

    public String getVdcUserDomainController() {
        return Frontend.getLoggedInUser().getDomainControler();
    }

    public native void init() /*-{
                              var server = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getServer()();
                              var rdpCabURL = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getRdpCabURL()();
                              var connectAreaDiv = document.createElement("<div style='display: none' id='connectArea'/>");
                              //var objStr = "<object id='MsRdpClient_" + server + "' onReadyStateChange='OnControlLoad()' onError='OnControlLoadError()' classid='CLSID:4eb89ff4-7f78-4a0f-8b8d-2bf02e94e4b2' width='800' height='600'></object>";
                              var objStr = "<object id='MsRdpClient_" + server + "' codebase='" + rdpCabURL + "' classid='CLSID:4eb89ff4-7f78-4a0f-8b8d-2bf02e94e4b2' width='800' height='600'></object>";
                              var client = document.createElement(objStr);
                              connectAreaDiv.appendChild(client);
                              document.body.appendChild(connectAreaDiv);

                              function OnControlLoad() {}

                              function OnControlLoadError() {}
                              }-*/;

    public native void connect() /*-{
                                 function OnConnected() {
                                 }

                                 function OnDisconnected(disconnectCode){
                                     var extendedDiscReason = MsRdpClient.ExtendedDisconnectReason;
                                     var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(disconnectCode);
                                     disconnectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, errorCodeEventArgs);
                                     $wnd.document.body.removeChild(MsRdpClient);
                                 }

                                 var server = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getServer()();
                                 var fullScreen = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getFullScreen()();
                                 var fullScreenTitle = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getFullScreenTitle()();
                                 var width = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getWidth()();
                                 var height = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getHeight()();
                                 var authenticationLevel = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getAuthenticationLevel()();
                                 var enableCredSspSupport = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getEnableCredSspSupport()();
                                 var redirectDrives = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getRedirectDrives()();
                                 var redirectPrinters = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getRedirectPrinters()();
                                 var redirectClipboard = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getRedirectClipboard()();
                                 var redirectSmartCards = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getRedirectSmartCards()();
                                    var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getDisconnectedEvent()();
                                    var userName = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getVdcUserNameAndDomain()();
                                    var password = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getVdcUserPassword()();
                                    var domain = this.@org.ovirt.engine.ui.common.uicommon.RdpConnector::getVdcUserDomainController()();
                                    var model = this;

                                 //var MsRdpClient = document.getElementById("MsRdpClient_"+server);
                                 //TODO: Only in DEBUG mode
                                 //alert("Server [" + server + "] fullScreen? [" + fullScreen + "], FullScreen Title [" +
                                 //fullScreenTitle + "], width [" + width + "], height [" + height + "], auth level [" + authenticationLevel + "] enableCredSSPSupport? [" +
                                 //enableCredSspSupport + "] redirect drives? [" + redirectDrives + "], redirect printers? [" + redirectPrinters + "], redirect clipboard? [" +
                                 //redirectClipboard + "], redirect SmartCards [" + redirectSmartCards + "]");

                                    // Remove previous client object
                                    var previousMsRdpClient = $wnd.document.getElementById('MsRdpClient_' + server);
                                    if (previousMsRdpClient)
                                        $wnd.document.body.removeChild(previousMsRdpClient);

                                 var MsRdpClient = $wnd.document.createElement("object");
                                 $wnd.document.body.appendChild(MsRdpClient);

                                 MsRdpClient.id = "MsRdpClient_" + server;
                                 MsRdpClient.classid = "CLSID:4eb89ff4-7f78-4a0f-8b8d-2bf02e94e4b2";
                                 MsRdpClient.server = server;
                                 MsRdpClient.FullScreen = fullScreen;
                                 MsRdpClient.desktopWidth = screen.width;
                                 MsRdpClient.desktopHeight = screen.height;
                                 MsRdpClient.width = 1;
                                 MsRdpClient.height = 1;
                                    MsRdpClient.UserName = userName;
                                    MsRdpClient.AdvancedSettings.ClearTextPassword = password;
                                 MsRdpClient.AdvancedSettings5.AuthenticationLevel = authenticationLevel;
                                 MsRdpClient.AdvancedSettings7.EnableCredSspSupport = enableCredSspSupport;
                                 MsRdpClient.AdvancedSettings2.RedirectDrives = redirectDrives;
                                 MsRdpClient.AdvancedSettings2.RedirectPrinters = redirectPrinters;
                                 MsRdpClient.AdvancedSettings2.RedirectClipboard = redirectClipboard;
                                 MsRdpClient.AdvancedSettings2.RedirectSmartCards = redirectSmartCards;
                                 MsRdpClient.AdvancedSettings2.ConnectionBarShowRestoreButton = false;

                                 MsRdpClient.attachEvent('OnConnected', OnConnected);
                                 MsRdpClient.attachEvent('OnDisconnected', OnDisconnected);

                                 MsRdpClient.connect();
                                 }-*/;
}
