package org.ovirt.engine.ui.common.uicommon;

import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.GraphicsType;
import org.ovirt.engine.core.common.console.ConsoleOptions;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.vms.SpiceConsoleModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

public abstract class AbstractSpice {

    protected final ConsoleOptions consoleOptions = new ConsoleOptions(GraphicsType.SPICE);

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

    protected String guestID;

    ClientAgentType cat = new ClientAgentType();
    protected String spiceBaseURL;

    public void setWANDisableEffects(List<ConsoleOptions.WanDisableEffects> disableEffects) {
        this.consoleOptions.setWanDisableEffects(disableEffects);
        getWANDisableEffectsChangeEvent().raise(this, EventArgs.EMPTY);
    }

    public void setWANColorDepth(ConsoleOptions.WanColorDepth colorDepth) {
        this.consoleOptions.setWanColorDepth(colorDepth);
        getWANColorDepthChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public List<ConsoleOptions.WanDisableEffects> getWANDisableEffects() {
        return consoleOptions.getWanDisableEffects();
    }

    public ConsoleOptions.WanColorDepth getWANColorDepth() {
        return consoleOptions.getWanColorDepth();
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
        Integer port = consoleOptions.getPort();
        return port == null
                ? 0
                : port;
    }

    public void setPort(int port) {
        this.consoleOptions.setPort(port);
    }

    public String getHost() {
        return consoleOptions.getHost();
    }

    public void setHost(String host) {
        this.consoleOptions.setHost(host);
    }

    public boolean isFullScreen() {
        return consoleOptions.isFullScreen();
    }

    public void setFullScreen(boolean fullScreen) {
        this.consoleOptions.setFullScreen(fullScreen);
    }

    public String getPassword() {
        return consoleOptions.getTicket();
    }

    public void setPassword(String password) {
        this.consoleOptions.setTicket(password);
    }

    public int getNumberOfMonitors() {
        return consoleOptions.getNumberOfMonitors();
    }

    public void setNumberOfMonitors(int numberOfMonitors) {
        this.consoleOptions.setNumberOfMonitors(numberOfMonitors);
    }

    public int getUsbListenPort() {
        return consoleOptions.getUsbListenPort();
    }

    public void setUsbListenPort(int usbListenPort) {
        this.consoleOptions.setUsbListenPort(usbListenPort);
    }

    public boolean isAdminConsole() {
        return consoleOptions.isAdminConsole();
    }

    public void setAdminConsole(boolean adminConsole) {
        this.consoleOptions.setAdminConsole(adminConsole);
    }

    public String getGuestHostName() {
        return consoleOptions.getGuestHostName();
    }

    public void setGuestHostName(String guestHostName) {
        this.consoleOptions.setGuestHostName(guestHostName);
    }

    public int getSecurePort() {
        return consoleOptions.getSecurePort() & 0xffff;
    }

    public void setSecurePort(int securePort) {
        this.consoleOptions.setSecurePort(securePort);
    }

    public String getSslChanels() {
        return consoleOptions.getSslChanels();
    }

    public void setSslChanels(String sslChanels) {
        this.consoleOptions.setSslChanels(adjustLegacySecureChannels(sslChanels));
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
        return consoleOptions.getCipherSuite();
    }

    public void setCipherSuite(String cipherSuite) {
        this.consoleOptions.setCipherSuite(cipherSuite);
    }

    public String getHostSubject() {
        return consoleOptions.getHostSubject();
    }

    public void setHostSubject(String hostSubject) {
        this.consoleOptions.setHostSubject(hostSubject);
    }

    public String getTrustStore() {
        return consoleOptions.getTrustStore();
    }

    public void setTrustStore(String trustStore) {
        this.consoleOptions.setTrustStore(trustStore);
    }

    public String getTitle() {
        return consoleOptions.getTitle();
    }

    public void setTitle(String title) {
        this.consoleOptions.setTitle(title);
    }

    public String getToggleFullscreenHotKey() {
        return consoleOptions.getToggleFullscreenHotKey();
    }

    public void setToggleFullscreenHotKey(String toggleFullscreenHotKey) {
        this.consoleOptions.setToggleFullscreenHotKey(toggleFullscreenHotKey);
    }

    public String getReleaseCursorHotKey() {
        return consoleOptions.getReleaseCursorHotKey();
    }

    public void setReleaseCursorHotKey(String releaseCursorHotKey) {
        this.consoleOptions.setReleaseCursorHotKey(releaseCursorHotKey);
    }

    public String[] getLocalizedStrings() {
        return consoleOptions.getLocalizedStrings();
    }

    public void setLocalizedStrings(String[] localizedStrings) {
        this.consoleOptions.setLocalizedStrings(localizedStrings);
    }

    public String getMenu() {
        return consoleOptions.getMenu();
    }

    public void setMenu(String menu) {
        this.consoleOptions.setMenu(menu);
    }

    public String getGuestID() {
        return guestID;
    }

    public void setGuestID(String guestID) {
        this.guestID = guestID;
    }

    public boolean getNoTaskMgrExecution() {
        return consoleOptions.isNoTaskMgrExecution();
    }

    public void setNoTaskMgrExecution(boolean noTaskMgrExecution) {
        this.consoleOptions.setNoTaskMgrExecution(noTaskMgrExecution);
    }

    public boolean isRemapCtrlAltDel() {
        return consoleOptions.isRemapCtrlAltDelete();
    }

    public void setRemapCtrlAltDel(boolean remapCtrlAltDelete) {
        this.consoleOptions.setRemapCtrlAltDelete(remapCtrlAltDelete);
    }

    public boolean getUsbAutoShare() {
        return consoleOptions.isUsbAutoShare();
    }

    public void setUsbAutoShare(boolean usbAutoShare) {
        this.consoleOptions.setUsbAutoShare(usbAutoShare);
        getUsbAutoShareChangedEvent().raise(this, EventArgs.EMPTY);
    }

    public String getUsbFilter() {
        return consoleOptions.getUsbFilter();
    }

    public void setUsbFilter(String usbFilter) {
        consoleOptions.setUsbFilter(usbFilter);
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
        return consoleOptions.isSmartcardEnabled();
    }

    public void setSmartcardEnabled(boolean smartcardEnabled) {
        this.consoleOptions.setSmartcardEnabled(smartcardEnabled);
    }

    protected int colorDepthAsInt() {
        if (getWANColorDepth() != null) {
            return getWANColorDepth().asInt();
        }

        return ConsoleOptions.WanColorDepth.depth16.asInt();
    }

    public boolean isWanOptionsEnabled() {
        return consoleOptions.isWanOptionsEnabled();
    }

    public void setWanOptionsEnabled(boolean wanOptionsEnabled) {
        this.consoleOptions.setWanOptionsEnabled(wanOptionsEnabled);
    }

    public void setOverrideEnabledSmartcard(boolean enabled) {
        this.consoleOptions.setSmartcardEnabledOverridden(enabled);
    }

    /**
     * Returns true if the user has choosen to disable the smartcard even it is by default enabled
     */
    public boolean isSmartcardEnabledOverridden() {
        return this.consoleOptions.isSmartcardEnabledOverridden();
    }

    protected String disableEffectsAsString() {
        StringBuffer disableEffectsBuffer = new StringBuffer("");
        int countdown = getWANDisableEffects().size();
        for (ConsoleOptions.WanDisableEffects disabledEffect : getWANDisableEffects()) {
            disableEffectsBuffer.append(disabledEffect.asString());

            if (countdown != 1) {
                disableEffectsBuffer.append(", "); //$NON-NLS-1$
            }
            countdown--;
        }

        return disableEffectsBuffer.toString();
    }

    public String getSpiceProxy() {
        return consoleOptions.getSpiceProxy();
    }

    public void setSpiceProxy(String spiceProxy) {
        this.consoleOptions.setSpiceProxy(spiceProxy);
    }

    public void setSpiceProxyEnabled(boolean enabled) {
        this.consoleOptions.setSpiceProxyEnabled(enabled);
    }

    public boolean isSpiceProxyEnabled() {
        return consoleOptions.isSpiceProxyEnabled();
    }

    protected String getSecureAttentionMapping() {
        return ConsoleOptions.SECURE_ATTENTION_MAPPING;
    }

    public int getTicketValiditySeconds() {
        return consoleOptions.getTicketValiditySeconds();
    }

    public void setTicketValiditySeconds(int ticketValiditySeconds) {
        this.consoleOptions.setTicketValiditySeconds(ticketValiditySeconds);
    }
}
