package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.common.console.ConsoleOptions.WanColorDepth;
import org.ovirt.engine.core.common.console.ConsoleOptions.WanDisableEffects;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

/**
 * Represents an implementor of a Spice. That way we have a bridge between Console model and concrete Spice accessor. In
 * case of WPF there will be direct Spice ActiveX instantiation, while Web implementor of Spice will generate
 * corresponding HTML.
 */
public interface ISpice {

    Event<EventArgs> getDisconnectedEvent();

    Event<EventArgs> getConnectedEvent();

    Event<EventArgs> getMenuItemSelectedEvent();

    Event<EventArgs> getUsbAutoShareChangedEvent();

    Event<EventArgs> getWANDisableEffectsChangeEvent();

    Event<EventArgs> getWANColorDepthChangedEvent();

    Version getCurrentVersion();

    Version getDesiredVersion();

    void setDesiredVersion(Version value);

    int getPort();

    void setPort(int value);

    String getHost();

    void setHost(String value);

    boolean isFullScreen();

    void setFullScreen(boolean value);

    String getPassword();

    void setPassword(String value);

    int getNumberOfMonitors();

    void setNumberOfMonitors(int value);

    int getUsbListenPort();

    void setUsbListenPort(int value);

    boolean isAdminConsole();

    void setAdminConsole(boolean value);

    String getGuestHostName();

    void setGuestHostName(String value);

    int getSecurePort();

    void setSecurePort(int value);

    String getSslChanels();

    void setSslChanels(String value);

    String getCipherSuite();

    void setCipherSuite(String value);

    String getHostSubject();

    void setHostSubject(String value);

    String getTrustStore();

    void setTrustStore(String value);

    String getTitle();

    void setTitle(String value);

    String getToggleFullscreenHotKey();

    void setToggleFullscreenHotKey(String toggleFullscreenHotKey);

    String getReleaseCursorHotKey();

    void setReleaseCursorHotKey(String releaseCursorHotKey);

    String[] getLocalizedStrings();

    void setLocalizedStrings(String[] value);

    String getMenu();

    void setMenu(String value);

    String getGuestID();

    void setGuestID(String value);

    boolean getNoTaskMgrExecution();

    void setNoTaskMgrExecution(boolean value);

    boolean isRemapCtrlAltDel();

    void setRemapCtrlAltDel(boolean value);

    boolean getUsbAutoShare();

    void setUsbAutoShare(boolean value);

    void setWANDisableEffects(List<WanDisableEffects> disableEffects);

    List<WanDisableEffects> getWANDisableEffects();

    void setWANColorDepth(WanColorDepth colorDepth);

    WanColorDepth getWANColorDepth();

    String getUsbFilter();

    void setUsbFilter(String value);

    void connect();

    void setCurrentVersion(Version currentVersion);

    void setSpiceBaseURL(String spiceBaseURL);

    boolean isWanOptionsEnabled();

    void setWanOptionsEnabled(boolean enabled);

    public void setSmartcardEnabled(boolean enabled);

    boolean isSmartcardEnabled();

    void setOverrideEnabledSmartcard(boolean enabled);

    boolean isSmartcardEnabledOverridden();

    void setSpiceProxy(String spiceProxy);

    void setSpiceProxyEnabled(boolean enabled);

    boolean isSpiceProxyEnabled();

    void setTicketValiditySeconds(int seconds);

    int getTicketValiditySeconds();

}
