package org.ovirt.engine.ui.common.uicommon;

import java.util.logging.Logger;

import org.ovirt.engine.ui.uicommonweb.models.ConsoleProtocol;
import org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.vms.IRdpPlugin;
import org.ovirt.engine.ui.uicommonweb.models.vms.RdpConsoleModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.Window;

public class RdpPluginImpl extends AbstractRdp implements IRdpPlugin, IEventListener<ErrorCodeEventArgs> {

    private static final Logger logger = Logger.getLogger(RdpPluginImpl.class.getName());

    private static EventDefinition RdpDisconnectedEventDefinition = new EventDefinition("RdpDisconnected", RdpConsoleModel.class); //$NON-NLS-1$
    private final Event<ErrorCodeEventArgs> disconnectedEvent = new Event<>(RdpDisconnectedEventDefinition);
    private RdpConsoleModel parentModel;

    @Override
    public void connect() {
        if (!consoleUtils.isBrowserPluginSupported(ConsoleProtocol.RDP)) {
            Window.alert(ConstantsManager.getInstance().getConstants().rdpIsNotSupportedInYourBrowser()); //$NON-NLS-1$
            return;
        }

        disconnectedEvent.addListener(this);
        connectNatively();
    }

    @Override
    public void setParentModel(RdpConsoleModel model) {
        this.parentModel = model;
    }

    public String getRDPCabURL() {
        return GWT.getModuleBaseURL() + "msrdp.cab";//$NON-NLS-1$
    }

    @Override
    public void eventRaised(Event<? extends ErrorCodeEventArgs> ev, Object sender, ErrorCodeEventArgs args) {
        if (disconnectedEvent.equals(ev)) {
            rdpDisconnected(sender, args);
        }
    }

    private void rdpDisconnected(Object sender, ErrorCodeEventArgs e) {
        disconnectedEvent.removeListener(this);

        if (e.getErrorCode() > 100) {
            parentModel.raiseErrorEvent(e);
        }
    }

    public native void connectNatively() /*-{
                                 try {
                                 var server = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getAddress()();
                                 var fullScreen = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getFullScreen()();
                                 var fullScreenTitle = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getFullScreenTitle()();
                                 var width = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getWidth()();
                                 var height = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getHeight()();
                                 var authenticationLevel = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getAuthenticationLevel()();
                                 var enableCredSspSupport = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getEnableCredSspSupport()();
                                 var redirectDrives = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getUseLocalDrives()();
                                 var redirectPrinters = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getRedirectPrinters()();
                                 var redirectClipboard = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getRedirectClipboard()();
                                 var redirectSmartCards = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getRedirectSmartCards()();
                                    var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::disconnectedEvent;
                                    var userName = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getUserNameAndDomain()();
                                    var password = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getUserPassword()();
                                    var domain = this.@org.ovirt.engine.ui.common.uicommon.RdpPluginImpl::getUserDomainController()();
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

                                 function OnConnected() {
                                 }

                                 function OnDisconnected(disconnectCode){
                                     var extendedDiscReason = MsRdpClient.ExtendedDisconnectReason;
                                     var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(disconnectCode);
                                     disconnectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, errorCodeEventArgs);
                                 }

                                 MsRdpClient.attachEvent('OnConnected', OnConnected);
                                 MsRdpClient.attachEvent('OnDisconnected', OnDisconnected);

                                 MsRdpClient.connect();
                                 } catch(e) { alert(e); }
                                 }-*/;

    @Override
    public boolean getEnableCredSspSupport() {
        return false;// Disable 'Credential Security Support Provider (CredSSP)' to enable SSO
    }

}
