package org.ovirt.engine.ui.common.uicommon;

import java.util.logging.Logger;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpicePlugin;

public class SpicePluginImpl extends AbstractSpice implements ISpicePlugin {
    private static Logger logger = Logger.getLogger(SpicePluginImpl.class.getName());
    private final Configurator configurator = (Configurator) TypeResolver.getInstance().Resolve(Configurator.class);

    @Override
    public void Connect() {
        logger.warning("Connecting via spice..."); //$NON-NLS-1$

        if (configurator.isClientLinuxFirefox()) {
            connectNativelyViaXPI();
        } else if (configurator.isClientWindowsExplorer()) {
            connectNativelyViaActiveX();
        }
    }

    public String getSpiceCabURL() {
        // According to the OS type, return the appropriate CAB URL
        if (cat.getPlatform().equalsIgnoreCase("win32")) { //$NON-NLS-1$
            return getSpiceBaseURL() + "SpiceX.cab"; //$NON-NLS-1$
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) { //$NON-NLS-1$
            return getSpiceBaseURL() + "SpiceX_x64.cab"; //$NON-NLS-1$
        } else {
            return null;
        }
    }

    public String getSpiceObjectClassId() {
        // According to the OS type, return the appropriate (x64/x86) object
        // class ID
        if (cat.getPlatform().equalsIgnoreCase("win32")) { //$NON-NLS-1$
            return "ACD6D89C-938D-49B4-8E81-DDBD13F4B48A"; //$NON-NLS-1$
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) { //$NON-NLS-1$
            return "ACD6D89C-938D-49B4-8E81-DDBD13F4B48B"; //$NON-NLS-1$
        } else {
            return null;
        }
    }

    public String getHotKeysAsString() {
        if (StringHelper.isNullOrEmpty(getToggleFullscreenHotKey()) && StringHelper.isNullOrEmpty(getReleaseCursorHotKey())) {
            return null;
        }

        String comma = (!StringHelper.isNullOrEmpty(getReleaseCursorHotKey()) && !StringHelper.isNullOrEmpty(getToggleFullscreenHotKey())) ? "," //$NON-NLS-1$
                            : ""; //$NON-NLS-1$

        String releaseCursorKeysParameter =
                StringHelper.isNullOrEmpty(getReleaseCursorHotKey()) ? "" : "release-cursor=" + getReleaseCursorHotKey(); //$NON-NLS-1$ //$NON-NLS-2$

        String toggleFullScreenKeysParameter =
                StringHelper.isNullOrEmpty(getToggleFullscreenHotKey()) ? "" : "toggle-fullscreen=" + getToggleFullscreenHotKey(); //$NON-NLS-1$ //$NON-NLS-2$

        return releaseCursorKeysParameter + comma + toggleFullScreenKeysParameter;
    }

    public native String loadActiveX(String id, String codebase, String classId) /*-{
                                                                                 var container = $wnd.document.createElement("div");
                                                                                 container.innerHTML = '<object id="' + id + '" codebase="' + codebase + '" classid="CLSID:' + classId
                                                                                 + '" width="0" height="0"></object>';
                                                                                 container.style.width = "0px";
                                                                                 container.style.height = "0px";
                                                                                 container.style.position = "absolute";
                                                                                 container.style.top = "0px";
                                                                                 container.style.left = "0px";
                                                                                 var target_element = $wnd.document.getElementsByTagName("body")[0];
                                                                                 if (typeof (target_element) == "undefined" || target_element == null)
                                                                                 return false;
                                                                                 target_element.appendChild(container);
                                                                                 }-*/;

    public native String loadXpi(String id) /*-{
                                            var container = document.createElement("div");
                                            container.innerHTML = '<embed id="' + id + '" type="application/x-spice" width=0 height=0/>';
                                            container.style.width = "0px";
                                            container.style.height = "0px";
                                            container.style.position = "absolute";
                                            container.style.top = "0px";
                                            container.style.left = "0px";
                                            var target_element = document.getElementsByTagName("body")[0];
                                            if (typeof (target_element) == "undefined" || target_element == null)
                                            return false;
                                            target_element.appendChild(container);
                                            }-*/;

    public native void connectNativelyViaXPI() /*-{
                                               var pluginFound = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::detectXpiPlugin()();
                                               if (!pluginFound) {
                                               alert("Spice XPI addon was not found, please install Spice XPI addon first.");
                                               return;
                                               }

                                               var hostIp = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHost()();
                                               var port = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPort()();
                                               var fullScreen = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::isFullScreen()();
                                               var password = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPassword()();
                                               var numberOfMonitors = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getNumberOfMonitors()();
                                               var usbListenPort = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getUsbListenPort()();
                                               var adminConsole = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::isAdminConsole()();
                                               var guestHostName = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getGuestHostName()();
                                               var securePort = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSecurePort()();
                                               var sslChanels = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSslChanels()();
                                               var cipherSuite = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getCipherSuite()();
                                               var hostSubject = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHostSubject()();
                                               var trustStore = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getTrustStore()();
                                               var title = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getTitle()();
                                               var hotKey = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHotKeysAsString()();
                                               var menu = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getMenu()();
                                               var guestID = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getGuestID()();
                                               var version = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getCurrentVersion()();
                                               var spiceCabURL = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceCabURL()();
                                               var spiceCabOjectClassId = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceObjectClassId()();
                                               var id = "SpiceX_" + guestHostName;
                                               var noTaskMgrExecution = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getNoTaskMgrExecution()();
                                               var sendCtrlAltDelete = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSendCtrlAltDelete()();
                                               var usbAutoShare = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getUsbAutoShare()();
                                               var usbFilter = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getUsbFilter()();
                                               var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getDisconnectedEvent()();
                                               var connectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getConnectedEvent()();
                                               var wanOptionsEnabled = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::isWanOptionsEnabled()();
                                               // the !! is there to convert the value to boolean because it is returned as int
                                               var smartcardEnabled =  !!this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::passSmartcardOption()();
                                               var colorDepth = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::colorDepthAsInt()();
                                               var disableEffects = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::disableEffectsAsString()();
                                               var spiceProxy = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceProxy()();
                                               var model = this;

                                               //alert("Smartcard ["+smartcardEnabled+"] disableEffects ["+disableEffects+"], wanOptionsEnabled ["+wanOptionsEnabled+"], colorDepth ["+colorDepth+"], Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], GuestID [" + guestID+"], version ["+version+"]");
                                               this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::loadXpi(Ljava/lang/String;)(id);
                                               var client = document.getElementById(id);
                                               client.hostIP = hostIp;
                                               client.port = port;
                                               client.Title = title;
                                               client.dynamicMenu = "";
                                               client.fullScreen = fullScreen;
                                               client.Password = password;
                                               client.NumberOfMonitors = numberOfMonitors;
                                               client.UsbListenPort = usbListenPort;
                                               client.AdminConsole = adminConsole;
                                               client.SecurePort = securePort;
                                               if(sslChanels != null && sslChanels.length > 0)
                                               client.SSLChannels = sslChanels;
                                               client.GuestHostName = guestHostName;
                                               if (cipherSuite != null && cipherSuite.length > 0)
                                               client.CipherSuite = cipherSuite;
                                               if (hostSubject != null)
                                               client.HostSubject = hostSubject;
                                               if (trustStore != null)
                                               client.TrustStore = trustStore;
                                               client.HotKey = hotKey;
                                               client.NoTaskMgrExecution = noTaskMgrExecution;
                                               client.SendCtrlAltDelete = sendCtrlAltDelete;
                                               client.UsbAutoShare = usbAutoShare;
                                               client.SetUsbFilter(usbFilter);
                                               client.Smartcard = smartcardEnabled;
                                               if (wanOptionsEnabled) {
                                                  client.DisableEffects = disableEffects;
                                                  client.ColorDepth = colorDepth;
                                               }
                                               // set it only if the proxy is defined in VDC_OPTIONS
                                               if (spiceProxy != null) {
                                                   client.Proxy = spiceProxy
                                               }
                                               client.connect();

                                               connectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, null);

                                               //since the 'ondisconnected' event doesn't work well in linux, we use polling instead:
                                               var checkConnectStatusIntervalID = setInterval(checkConnectStatus, 2000);
                                               function checkConnectStatus() {
                                               if (client.ConnectedStatus() >= 0) {
                                               clearInterval(checkConnectStatusIntervalID);

                                               var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(client.ConnectedStatus());
                                               disconnectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, errorCodeEventArgs);

                                               // Refresh grid on disconnect (to re-enable console button)
                                               // var gridRefreshManager = @org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::getInstance()();
                                               // gridRefreshManager.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::refreshGrids()();
                                               }
                                               }
                                               }-*/;

    @Override
    public boolean detectBrowserPlugin() {
        if (configurator.isClientLinuxFirefox()) {
            return detectXpiPlugin();
        } else if (configurator.isClientWindowsExplorer()) {
            return detectActiveXPlugin();
        }

        return false;
    }

    private native boolean detectXpiPlugin() /*-{
                                            var pluginsFound = false;
                                            if (navigator.plugins && navigator.plugins.length > 0) {
                                            var daPlugins = [ "Spice" ];
                                            var pluginsAmount = navigator.plugins.length;
                                            for (counter = 0; counter < pluginsAmount; counter++) {
                                            var numFound = 0;
                                            for (namesCounter = 0; namesCounter < daPlugins.length; namesCounter++) {
                                            if ((navigator.plugins[counter].name.indexOf(daPlugins[namesCounter]) > 0)
                                            || (navigator.plugins[counter].description.indexOf(daPlugins[namesCounter]) >= 0)) {
                                            numFound++;
                                            }
                                            }
                                            if (numFound == daPlugins.length) {
                                            pluginsFound = true;
                                            break;
                                            }
                                            }

                                            }
                                            return pluginsFound;
                                            }-*/;

    private native boolean detectActiveXPlugin() /*-{
       var pluginObject = null;
       try {
           pluginObject = new ActiveXObject('SpiceX.OSpiceX');
       } catch (e) {
       }

       if (pluginObject) {
           return true;
       } else {
           return false;
       }
    }-*/;

    public native void connectNativelyViaActiveX() /*-{
                                                   var hostIp = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHost()();
                                                   var port = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPort()();
                                                   var fullScreen = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::isFullScreen()();
                                                   var password = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPassword()();
                                                   var numberOfMonitors = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getNumberOfMonitors()();
                                                   var usbListenPort = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getUsbListenPort()();
                                                   var adminConsole = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::isAdminConsole()();
                                                   var guestHostName = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getGuestHostName()();
                                                   var securePort = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSecurePort()();
                                                   var sslChanels = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSslChanels()();
                                                   var cipherSuite = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getCipherSuite()();
                                                   var hostSubject = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHostSubject()();
                                                   var trustStore = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getTrustStore()();
                                                   var title = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getTitle()();
                                                   var hotKey = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHotKeysAsString()();
                                                   var menu = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getMenu()();
                                                   var guestID = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getGuestID()();
                                                   var version = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getDesiredVersionStr()();
                                                   var spiceCabURL = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceCabURL()();
                                                   var spiceCabOjectClassId = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceObjectClassId()();
                                                   var noTaskMgrExecution = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getNoTaskMgrExecution()();
                                                   var sendCtrlAltDelete = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSendCtrlAltDelete()();
                                                   var usbAutoShare = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getUsbAutoShare()();
                                                   var usbFilter = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getUsbFilter()();
                                                   var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getDisconnectedEvent()();
                                                   var connectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getConnectedEvent()();
                                                   var wanOptionsEnabled = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::isWanOptionsEnabled()();
                                                   // the !! is there to convert the value to boolean because it is returned as int
                                                   var smartcardEnabled =  !!this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::passSmartcardOption()();
                                                   var colorDepth = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::colorDepthAsInt()();
                                                   var disableEffects = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::disableEffectsAsString()();
                                                   var spiceProxy = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceProxy()();
                                                   var codebase = spiceCabURL + "#version=" + version;
                                                   var model = this;
                                                   var id = "SpiceX_" + guestHostName;
                                                   //alert("Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], GuestID [" + guestID+"], version ["+version+"]");
                                                   //alert("Trust Store ["+trustStore+"]");

                                                   this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::loadActiveX(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(id,codebase,spiceCabOjectClassId);

                                                   var client = $wnd.document.getElementById(id);
                                                   client.attachEvent('onreadystatechange', onReadyStateChange);
                                                   tryToConnect();

                                                   function tryToConnect() {
                                                   if (client.readyState == 4) {
                                                   try {
                                                   client.style.width = "0px";
                                                   client.style.height = "0px";
                                                   client.hostIP = hostIp;
                                                   client.port = port;
                                                   client.Title = title;
                                                   client.dynamicMenu = "";
                                                   client.fullScreen = fullScreen;
                                                   client.Password = password;
                                                   client.NumberOfMonitors = numberOfMonitors;
                                                   client.UsbListenPort = usbListenPort;
                                                   client.AdminConsole = adminConsole;
                                                   client.SecurePort = securePort;
                                                   if (sslChanels != null && sslChanels.length > 0)
                                                   client.SSLChannels = sslChanels;
                                                   client.GuestHostName = guestHostName;
                                                   if (cipherSuite != null && cipherSuite.length > 0)
                                                   client.CipherSuite = cipherSuite;
                                                   if (hostSubject != null)
                                                   client.HostSubject = hostSubject;
                                                   if (trustStore != null)
                                                   client.TrustStore = trustStore;
                                                   client.HotKey = hotKey;
                                                   client.NoTaskMgrExecution = noTaskMgrExecution;
                                                   client.SendCtrlAltDelete = sendCtrlAltDelete;
                                                   client.UsbAutoShare = usbAutoShare;
                                                   client.SetUsbFilter(usbFilter);
                                                   client.Smartcard = smartcardEnabled;
                                                   if (wanOptionsEnabled) {
                                                       client.DisableEffects = disableEffects;
                                                       client.ColorDepth = colorDepth;
                                                   }

                                                    // only if the proxy is defined in VDC_OPTIONS
                                                   if (spiceProxy != null) {
                                                       client.Proxy = spiceProxy
                                                   }

                                                   client.attachEvent('ondisconnected', onDisconnected);
                                                   client.connect();

                                                   connectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, null);
                                                   } catch (ex) {
                                                   onDisconnected();
                                                   }
                                                   }
                                                   }

                                                   function onReadyStateChange() {
                                                   tryToConnect();
                                                   }

                                                   function onDisconnected(errorCode) {
                                                   var errorCodeEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.ErrorCodeEventArgs::new(I)(errorCode);
                                                   disconnectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, errorCodeEventArgs);

                                                   // Refresh grid on disconnect (to re-enable console button)
                                                   // var gridRefreshManager = @org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::getInstance()();
                                                   // gridRefreshManager.@org.ovirt.engine.ui.userportal.client.components.GridRefreshManager::refreshGrids()();
                                                   }
                                                   }-*/;

    @Override
    public void Install() {
        logger.finer("Spice must be installed ahead..."); //$NON-NLS-1$
        InstallNatively();
    }

    public native void InstallNatively() /*-{
                                         alert("Spice must be already installed!");
                                         }-*/;

    @Override
    public boolean getIsInstalled() {
        boolean result = getIsInstalledNative();
        logger.finer("Determining whether spice is installed [" + result + "]"); //$NON-NLS-1$ //$NON-NLS-2$

        return result;
    }

    public native boolean getIsInstalledNative() /*-{
                                                 return true;
                                                 //var daPlugins = "Spice";
                                                 //var pluginFound = false;
                                                 //if (navigator.plugins && navigator.plugins.length > 0) {
                                                 //var pluginsArrayLength = navigator.plugins.length;
                                                 //for (pluginsArrayCounter=0; pluginsArrayCounter < pluginsArrayLength; pluginsArrayCounter++ ) {
                                                 //var numFound = 0;
                                                 //for(namesCounter=0; namesCounter < daPlugins.length; namesCounter++) {
                                                 //if( (navigator.plugins[pluginsArrayCounter].name.indexOf(daPlugins[namesCounter]) >= 0) ||
                                                 //(navigator.plugins[pluginsArrayCounter].description.indexOf(daPlugins[namesCounter]) >= 0) ) {
                                                 //numFound++;
                                                 //}
                                                 //}
                                                 //if(numFound == daPlugins.length) {
                                                 //pluginFound = true;
                                                 //break;
                                                 //}
                                                 //}
                                                 //}
                                                 //return pluginFound;
                                                 }-*/;

    public static boolean isBrowserSupported() {
        ClientAgentType cat = new ClientAgentType();
        logger.finer("Determining whether browser [" + cat.browser //$NON-NLS-1$
                + "], version [" + cat.version + "] on OS [" + cat.os //$NON-NLS-1$ //$NON-NLS-2$
                + "] is supported by spice"); //$NON-NLS-1$

        if ((cat.os.equalsIgnoreCase("Windows")) //$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Explorer")) //$NON-NLS-1$
                && (cat.version >= 7.0)) {
            return true;
        } else if ((cat.os.equalsIgnoreCase("Linux")) //$NON-NLS-1$
                && (cat.browser.equalsIgnoreCase("Firefox")) //$NON-NLS-1$
                && (cat.version >= 2.0)) {
            return true;
        }

        return false;
    }

}
