package org.ovirt.engine.ui.common.uicommon;

import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpicePlugin;
import com.google.inject.Inject;

@Deprecated
public class SpicePluginImpl extends AbstractSpice implements ISpicePlugin {

    protected Version pluginVersion = new Version(4, 4);
    protected String spiceBaseURL;

    private static final Logger logger = Logger.getLogger(SpicePluginImpl.class.getName());

    @Inject
    private Configurator configurator;

    private final ClientAgentType cat = new ClientAgentType();

    @Override
    public void invokeClient() {
        logger.warning("Connecting via spice..."); //$NON-NLS-1$

        if (configurator.isClientLinuxFirefox()) {
            connectNativelyViaXPI();
        } else if (configurator.isClientWindowsExplorer() || consoleUtils.isIE11()) {
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

    private String getPortAsString() {
        Integer port = getOptions().getPort();
        return (port == null)
                ? null
                : port.toString();
    }

    public String getHotKeysAsString() {
        List<String> hotKeysList = getHotKeysAsList();

        if (hotKeysList.size() == 0) {
            return null;
        }

        StringBuilder hotKeysAsString = new StringBuilder();
        for (String hotkey : hotKeysList) {
            hotKeysAsString.append(hotkey);
            hotKeysAsString.append(","); // $NON-NLS-1$
        }

        return hotKeysAsString.substring(0, hotKeysAsString.length() - 1); // chop last comma
    }

    private List<String> getHotKeysAsList() {
        List<String> result = new LinkedList<>();
        ConsoleOptions options = getOptions();

        if (!StringHelper.isNullOrEmpty(options.getReleaseCursorHotKey())) {
            result.add("release-cursor=" + options.getReleaseCursorHotKey()); // $NON-NLS-1$
        }

        if (!StringHelper.isNullOrEmpty(options.getToggleFullscreenHotKey())) {
            result.add("toggle-fullscreen=" + options.getToggleFullscreenHotKey()); // $NON-NLS-1$
        }

        if (options.isRemapCtrlAltDelete()) {
            result.add("secure-attention=" + ConsoleOptions.SECURE_ATTENTION_MAPPING); // $NON-NLS-1$
        }

        return result;
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

                                               var options = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getOptions()();
                                               // the !! is there to convert the value to boolean because it is returned as int
                                               var hostIp = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getHost()();
                                               var port = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPortAsString()();
                                               var fullScreen = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isFullScreen()();
                                               var password = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getTicket()();
                                               var numberOfMonitors = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getNumberOfMonitors()();
                                               var usbListenPort = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getUsbListenPort()();
                                               var adminConsole = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isAdminConsole()();
                                               var guestHostName = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getGuestHostName()();
                                               var securePort = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getSecurePort()();
                                               var sslChanels = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getSslChanels()();
                                               var cipherSuite = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getCipherSuite()();
                                               var hostSubject = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getHostSubject()();
                                               var trustStore = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getTrustStore()();
                                               var title = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getTitle()();
                                               var hotKey = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHotKeysAsString()();
                                               var id = "SpiceX_" + guestHostName;
                                               var noTaskMgrExecution = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isNoTaskMgrExecution()();
                                               var usbAutoShare = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isUsbAutoShare()();
                                               var usbFilter = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getUsbFilter()();
                                               var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getDisconnectedEvent()();
                                               var connectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getConnectedEvent()();
                                               var wanOptionsEnabled = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isWanOptionsEnabled()();
                                               var smartcardEnabled =  !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::passSmartcardOption()();
                                               var colorDepth = options.@org.ovirt.engine.core.common.console.ConsoleOptions::colorDepthAsInt()();
                                               var disableEffects = options.@org.ovirt.engine.core.common.console.ConsoleOptions::disableEffectsAsString()();
                                               var spiceProxy = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getSpiceProxy()();
                                               var model = this;

                                               //alert("Smartcard ["+smartcardEnabled+"] disableEffects ["+disableEffects+"], wanOptionsEnabled ["+wanOptionsEnabled+"], colorDepth ["+colorDepth+"], Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"]);
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
                                               client.SendCtrlAltDelete = false;
                                               client.UsbAutoShare = usbAutoShare;
                                               client.SetUsbFilter(usbFilter);
                                               client.Smartcard = smartcardEnabled;

                                               if (wanOptionsEnabled) {
                                                  client.DisableEffects = disableEffects;
                                                  client.ColorDepth = colorDepth;
                                               } else { //reset to defaults
                                                  client.DisableEffects = "";
                                                  client.ColorDepth = ""; // "" is default for spice-xpi
                                               }

                                               // only if the proxy is defined in VDC_OPTIONS
                                               if (spiceProxy != null) {
                                                  client.Proxy = spiceProxy;
                                               } else {
                                                  client.Proxy = '';
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
        } else if (configurator.isClientWindowsExplorer() || consoleUtils.isIE11()) {
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
                                                   // helper for attaching callbacks for ie 11
                                                   function attachEventIe11Safe(object, eventName, callbackFn) {
                                                       if($wnd.document.attachEvent) { // ie < 11
                                                           object.attachEvent(eventName, callbackFn);
                                                       } else {
                                                           object.addEventListener(eventName, callbackFn, false);
                                                       }
                                                   }
                                                   var options = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getOptions()();
                                                   // the !! is there to convert the value to boolean because it is returned as int
                                                   var hostIp = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getHost()();
                                                   var port = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPortAsString()();
                                                   var fullScreen = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isFullScreen()();
                                                   var password = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getTicket()();
                                                   var numberOfMonitors = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getNumberOfMonitors()();
                                                   var usbListenPort = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getUsbListenPort()();
                                                   var adminConsole = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isAdminConsole()();
                                                   var guestHostName = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getGuestHostName()();
                                                   var securePort = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getSecurePort()();
                                                   var sslChanels = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getSslChanels()();
                                                   var cipherSuite = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getCipherSuite()();
                                                   var hostSubject = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getHostSubject()();
                                                   var trustStore = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getTrustStore()();
                                                   var title = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getTitle()();
                                                   var hotKey = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getHotKeysAsString()();
                                                   var menu = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getMenu()();
                                                   var version = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getPluginVersionStr()();
                                                   var spiceCabURL = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceCabURL()();
                                                   var spiceCabOjectClassId = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getSpiceObjectClassId()();
                                                   var noTaskMgrExecution = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isNoTaskMgrExecution()();
                                                   var usbAutoShare = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isUsbAutoShare()();
                                                   var usbFilter = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getUsbFilter()();
                                                   var disconnectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getDisconnectedEvent()();
                                                   var menuItemSelectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getMenuItemSelectedEvent()();
                                                   var connectedEvent = this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::getConnectedEvent()();
                                                   var wanOptionsEnabled = !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::isWanOptionsEnabled()();
                                                   var smartcardEnabled =  !!options.@org.ovirt.engine.core.common.console.ConsoleOptions::passSmartcardOption()();
                                                   var colorDepth = options.@org.ovirt.engine.core.common.console.ConsoleOptions::colorDepthAsInt()();
                                                   var disableEffects = options.@org.ovirt.engine.core.common.console.ConsoleOptions::disableEffectsAsString()();
                                                   var spiceProxy = options.@org.ovirt.engine.core.common.console.ConsoleOptions::getSpiceProxy()();
                                                   var codebase = spiceCabURL + "#version=" + version;
                                                   var model = this;
                                                   var id = "SpiceX_" + guestHostName;
                                                   //alert("Host IP ["+hostIp+"], port ["+port+"], fullScreen ["+fullScreen+"], password ["+password+"], numberOfMonitors ["+numberOfMonitors+"], Usb Listen Port ["+usbListenPort+"], Admin Console ["+adminConsole+"], Guest HostName ["+guestHostName+"], Secure Port ["+securePort+"], Ssl Chanels ["+sslChanels+"], cipherSuite ["+cipherSuite+"], Host Subject ["+hostSubject+"], Title [" + title+"], Hot Key ["+hotKey+"], Menu ["+menu+"], version ["+version+"]");
                                                   //alert("Trust Store ["+trustStore+"]");
                                                   this.@org.ovirt.engine.ui.common.uicommon.SpicePluginImpl::loadActiveX(Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;)(id,codebase,spiceCabOjectClassId);
                                                   var client = $wnd.document.getElementById(id);
                                                   attachEventIe11Safe(client, 'onreadystatechange', onReadyStateChange);
                                                   attachEventIe11Safe(client, 'onmenuitemselected', onMenuItemSelected);
                                                   tryToConnect();
                                                   function tryToConnect() {
                                                   if (client.readyState == 4) {
                                                   try {
                                                   client.style.width = "0px";
                                                   client.style.height = "0px";
                                                   client.hostIP = hostIp;
                                                   client.port = port;
                                                   client.Title = title;
                                                   client.dynamicMenu = menu;
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
                                                   client.SendCtrlAltDelete = false;
                                                   client.UsbAutoShare = usbAutoShare;
                                                   client.SetUsbFilter(usbFilter);
                                                   client.Smartcard = smartcardEnabled;

                                                   if (wanOptionsEnabled) {
                                                       client.DisableEffects = disableEffects;
                                                       client.ColorDepth = colorDepth;
                                                   }else { //reset to defaults
                                                      client.DisableEffects = "";
                                                      client.ColorDepth = 0; // 0 is default for spice-activex
                                                   }

                                                   // only if the proxy is defined in VDC_OPTIONS
                                                   if (spiceProxy != null) {
                                                       client.Proxy = spiceProxy;
                                                   } else {
                                                       client.Proxy = '';
                                                   }

                                                   attachEventIe11Safe(client, 'ondisconnected', onDisconnected);
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

                                                   function onMenuItemSelected(itemId) {
                                                   var spiceMenuItemEventArgs = @org.ovirt.engine.ui.uicommonweb.models.vms.SpiceMenuItemEventArgs::new(I)(itemId);
                                                   menuItemSelectedEvent.@org.ovirt.engine.ui.uicompat.Event::raise(Ljava/lang/Object;Lorg/ovirt/engine/ui/uicompat/EventArgs;)(model, spiceMenuItemEventArgs);
                                                   }
                                                   }-*/;

    public void setPluginVersion(Version pluginVersion) {
        this.pluginVersion = pluginVersion;
    }

    private String getPluginVersionStr() {
        return pluginVersion.toString().replace('.', ',');
    }

    public String getSpiceBaseURL() {
        return spiceBaseURL;
    }

    public void setSpiceBaseURL(String spiceBaseURL) {
        this.spiceBaseURL = spiceBaseURL;
    }

}
