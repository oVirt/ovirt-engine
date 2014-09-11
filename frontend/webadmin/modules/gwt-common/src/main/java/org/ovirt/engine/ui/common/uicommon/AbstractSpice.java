package org.ovirt.engine.ui.common.uicommon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.ConsoleUtils;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.WANDisableEffects;
import org.ovirt.engine.ui.uicommonweb.models.vms.WanColorDepth;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class AbstractSpice {

    protected Event<EventArgs> disconnectedEvent = new Event<EventArgs>(
            SpiceConsoleModel.spiceDisconnectedEventDefinition);
    protected Event<EventArgs> connectedEvent = new Event<EventArgs>(
            SpiceConsoleModel.spiceConnectedEventDefinition);
    protected Event<EventArgs> menuItemSelectedEvent = new Event<EventArgs>(
            SpiceConsoleModel.spiceMenuItemSelectedEventDefinition);
    protected Event<EventArgs> usbAutoShareChangedEvent = new Event<EventArgs>(
            SpiceConsoleModel.usbAutoShareChangedEventDefinition);
    protected final Event<EventArgs> wanColorDepthChangedEvent = new Event<EventArgs>(
            SpiceConsoleModel.wanColorDepthChangedEventDefinition);
    protected final Event<EventArgs> wanDisableEffectsChangeEvent = new Event<EventArgs>(
            SpiceConsoleModel.wanDisableEffectsChangeEventDefinition);

    protected Version currentVersion = new Version(4, 4);
    protected Version desiredVersion = new Version(4, 4);
    protected int port;
    protected String host;
    protected boolean fullScreen;
    protected String password;
    protected int numberOfMonitors;
    protected int usbListenPort;
    protected boolean adminConsole;
    protected String guestHostName;
    protected int securePort;
    protected String sslChanels;
    protected String cipherSuite;
    protected String hostSubject;
    protected String trustStore;
    protected String title;
    protected String toggleFullscreenHotKey;
    protected String releaseCursorHotKey;
    protected String[] localizedStrings;
    protected String menu;
    protected String guestID;
    protected boolean noTaskMgrExecution;
    protected boolean remapCtrlAltDelete;
    protected boolean usbAutoShare;
    protected String usbFilter;
    protected WanColorDepth wanColorDepth;
    protected List<WANDisableEffects> wanDisableEffects;
    protected boolean wanOptionsEnabled;
    ClientAgentType cat = new ClientAgentType();
    protected String spiceBaseURL;
    protected boolean smartcardEnabled = false;
    protected String spiceProxy = null;
    private int ticketValiditySeconds;

    // the user can choose to disable the smartcard even when it is enabled, but can not choose to enable it, when it is
    // disabled
    protected boolean smartcardEnabledOverridden = false;

    // even the spice proxy is globally configured, user can choose to disable it for specific VM
    private boolean spiceProxyEnabled;

    private final ConsoleUtils consoleUtils = (ConsoleUtils) TypeResolver.getInstance().resolve(ConsoleUtils.class);

    public AbstractSpice() {
        setWANDisableEffects(new ArrayList<WANDisableEffects>());
        setWanOptionsEnabled(false);
        setWANColorDepth(WanColorDepth.depth16);
        setRemapCtrlAltDel(true);
        setNoTaskMgrExecution(false);
    }

    public void setWANDisableEffects(List<WANDisableEffects> disableEffects) {
        this.wanDisableEffects = disableEffects;
        getWANDisableEffectsChangeEvent().raise(this, EventArgs.EMPTY);
    }

    public void setWANColorDepth(WanColorDepth colorDepth) {
        this.wanColorDepth = colorDepth;
        getWANColorDepthChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public List<WANDisableEffects> getWANDisableEffects() {
        return wanDisableEffects;
    }

    public WanColorDepth getWANColorDepth() {
        return wanColorDepth;
    }

    public Event<EventArgs> getWANDisableEffectsChangeEvent() {
        return wanDisableEffectsChangeEvent;
    }

    public Event<EventArgs> getWANColorDepthChangedEvent() {
        return wanColorDepthChangedEvent;
    }

    public Event<EventArgs> getDisconnectedEvent() {
        return disconnectedEvent;
    }

    public void setDisconnectedEvent(Event<EventArgs> disconnectedEvent) {
        this.disconnectedEvent = disconnectedEvent;
    }

    public Event<EventArgs> getConnectedEvent() {
        return connectedEvent;
    }

    public void setConnectedEvent(Event<EventArgs> connectedEvent) {
        this.connectedEvent = connectedEvent;
    }

    public Event<EventArgs> getMenuItemSelectedEvent() {
        return menuItemSelectedEvent;
    }

    public void setMenuItemSelectedEvent(Event<EventArgs> menuItemSelectedEvent) {
        this.menuItemSelectedEvent = menuItemSelectedEvent;
    }

    public void setUsbAutoShareChangedEvent(Event<EventArgs> usbAutoShareChangedEvent) {
        this.usbAutoShareChangedEvent = usbAutoShareChangedEvent;
    }

    public Event<EventArgs> getUsbAutoShareChangedEvent() {
        return usbAutoShareChangedEvent;
    }

    public Version getCurrentVersion() {
        return currentVersion;
    }

    public void setCurrentVersion(Version currentVersion) {
        this.currentVersion = currentVersion;
    }

    // This should be defined by UiCommon
    public Version getDesiredVersion() {
        return desiredVersion;
    }

    public void setDesiredVersion(Version desiredVersion) {
        this.desiredVersion = desiredVersion;
    }

    public String getDesiredVersionStr() {
        return desiredVersion.toString().replace('.', ',');
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public boolean isFullScreen() {
        return fullScreen;
    }

    public void setFullScreen(boolean fullScreen) {
        this.fullScreen = fullScreen;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public int getNumberOfMonitors() {
        return numberOfMonitors;
    }

    public void setNumberOfMonitors(int numberOfMonitors) {
        this.numberOfMonitors = numberOfMonitors;
    }

    public int getUsbListenPort() {
        return usbListenPort;
    }

    public void setUsbListenPort(int usbListenPort) {
        this.usbListenPort = usbListenPort;
    }

    public boolean isAdminConsole() {
        return adminConsole;
    }

    public void setAdminConsole(boolean adminConsole) {
        this.adminConsole = adminConsole;
    }

    public String getGuestHostName() {
        return guestHostName;
    }

    public void setGuestHostName(String guestHostName) {
        this.guestHostName = guestHostName;
    }

    public int getSecurePort() {
        return securePort & 0xffff;
    }

    public void setSecurePort(int securePort) {
        this.securePort = securePort;
    }

    public String getSslChanels() {
        return sslChanels;
    }

    public void setSslChanels(String sslChanels) {
        this.sslChanels = adjustLegacySecureChannels(sslChanels);
    }

    /**
     * Reformat secure channels string if they are in legacy ('s'-prefixed) format.
     * @param legacySecureChannels (e.g. "smain,sinput")
     * @return secure channels in correct format (e.g. "main,input")
     */
    static String adjustLegacySecureChannels(String legacySecureChannels) {
        if (StringHelper.isNullOrEmpty(legacySecureChannels)) {
            return legacySecureChannels;
        }

        String secureChannels = legacySecureChannels;
        List<String> legacyChannels = Arrays.asList(
                new String[]{"smain", "sdisplay", "sinputs", "scursor", "splayback", "srecord", "ssmartcard", "susbredir"}); // $NON-NLS-1$ $NON-NLS-2$ $NON-NLS-3$ $NON-NLS-4$ $NON-NLS-5$ $NON-NLS-6$ $NON-NLS-7$ $NON-NLS-8$

        for (String channel : legacyChannels) {
            secureChannels = secureChannels.replace(channel, channel.substring(1));
        }

        return secureChannels;
    }

    public String getCipherSuite() {
        return cipherSuite;
    }

    public void setCipherSuite(String cipherSuite) {
        this.cipherSuite = cipherSuite;
    }

    public String getHostSubject() {
        return hostSubject;
    }

    public void setHostSubject(String hostSubject) {
        this.hostSubject = hostSubject;
    }

    public String getTrustStore() {
        return trustStore;
    }

    public void setTrustStore(String trustStore) {
        this.trustStore = trustStore;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToggleFullscreenHotKey() {
        return toggleFullscreenHotKey;
    }

    public void setToggleFullscreenHotKey(String toggleFullscreenHotKey) {
        this.toggleFullscreenHotKey = toggleFullscreenHotKey;
    }

    public String getReleaseCursorHotKey() {
        return releaseCursorHotKey;
    }

    public void setReleaseCursorHotKey(String releaseCursorHotKey) {
        this.releaseCursorHotKey = releaseCursorHotKey;
    }

    public String[] getLocalizedStrings() {
        return localizedStrings;
    }

    public void setLocalizedStrings(String[] localizedStrings) {
        this.localizedStrings = localizedStrings;
    }

    public String getMenu() {
        return menu;
    }

    public void setMenu(String menu) {
        this.menu = menu;
    }

    public String getGuestID() {
        return guestID;
    }

    public void setGuestID(String guestID) {
        this.guestID = guestID;
    }

    public boolean getNoTaskMgrExecution() {
        return noTaskMgrExecution;
    }

    public void setNoTaskMgrExecution(boolean noTaskMgrExecution) {
        this.noTaskMgrExecution = noTaskMgrExecution;
    }

    public boolean isRemapCtrlAltDel() {
        return remapCtrlAltDelete;
    }

    public void setRemapCtrlAltDel(boolean remapCtrlAltDelete) {
        this.remapCtrlAltDelete = remapCtrlAltDelete;
    }

    public boolean getUsbAutoShare() {
        return usbAutoShare;
    }

    public void setUsbAutoShare(boolean usbAutoShare) {
        this.usbAutoShare = usbAutoShare;
        getUsbAutoShareChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public String getUsbFilter() {
        return usbFilter;
    }

    public void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    public String getSpiceBaseURL() {
        return spiceBaseURL;
    }

    public void setSpiceBaseURL(String spiceBaseURL) {
        this.spiceBaseURL = spiceBaseURL;
    }

    public boolean passSmartcardOption() {
        return isSmartcardEnabled() && !isSmartcardEnabledOverridden();
    }

    public boolean isSmartcardEnabled() {
        return smartcardEnabled;
    }

    public void setSmartcardEnabled(boolean smartcardEnabled) {
        this.smartcardEnabled = smartcardEnabled;
    }

    protected int colorDepthAsInt() {
        if (getWANColorDepth() != null) {
            return getWANColorDepth().asInt();
        }

        return WanColorDepth.depth16.asInt();
    }

    public boolean isWanOptionsEnabled() {
        return wanOptionsEnabled;
    }

    public void setWanOptionsEnabled(boolean wanOptionsEnabled) {
        this.wanOptionsEnabled = wanOptionsEnabled;
    }

    public void setOverrideEnabledSmartcard(boolean enabled) {
        this.smartcardEnabledOverridden = enabled;
    }

    /**
     * Returns true if the user has choosen to disable the smartcard even it is by default enabled
     */
    public boolean isSmartcardEnabledOverridden() {
        return this.smartcardEnabledOverridden;
    }

    protected String disableEffectsAsString() {
        StringBuffer disableEffectsBuffer = new StringBuffer("");
        int countdown = getWANDisableEffects().size();
        for (WANDisableEffects disabledEffect : getWANDisableEffects()) {
            disableEffectsBuffer.append(disabledEffect.asString());

            if (countdown != 1) {
                disableEffectsBuffer.append(", "); //$NON-NLS-1$
            }
            countdown--;
        }

        return disableEffectsBuffer.toString();
    }

    public String getSpiceProxy() {
        return spiceProxy;
    }

    public void setSpiceProxy(String spiceProxy) {
        this.spiceProxy = spiceProxy;
    }

    public void setSpiceProxyEnabled(boolean enabled) {
        this.spiceProxyEnabled = enabled;
    }

    public boolean isSpiceProxyEnabled() {
        return spiceProxyEnabled;
    }

    protected String getSecureAttentionMapping() {
        return consoleUtils.getRemapCtrlAltDelHotkey();
    }

    public int getTicketValiditySeconds() {
        return ticketValiditySeconds;
    }

    public void setTicketValiditySeconds(int ticketValiditySeconds) {
        this.ticketValiditySeconds = ticketValiditySeconds;
    }
}
