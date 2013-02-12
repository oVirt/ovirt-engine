package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.List;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicompat.Event;

/**
 * Represents an implementor of a Spice. That way we have a bridge between Console model and concrete Spice accessor. In
 * case of WPF there will be direct Spice ActiveX instantiation, while Web implementor of Spice will generate
 * corresponding HTML.
 */
public interface ISpice
{
    // event EventHandler<ErrorCodeEventArgs> Disconnected;
    // event EventHandler<SpiceMenuItemEventArgs> MenuItemSelected;

    Event getDisconnectedEvent();

    Event getConnectedEvent();

    Event getMenuItemSelectedEvent();

    Event getUsbAutoShareChangedEvent();

    Event getWANDisableEffectsChangeEvent();

    Event getWANColorDepthChangedEvent();

    Version getCurrentVersion();

    boolean getIsInstalled();

    Version getDesiredVersion();

    void setDesiredVersion(Version value);

    int getPort();

    void setPort(int value);

    String getHost();

    void setHost(String value);

    boolean getFullScreen();

    void setFullScreen(boolean value);

    String getPassword();

    void setPassword(String value);

    int getNumberOfMonitors();

    void setNumberOfMonitors(int value);

    int getUsbListenPort();

    void setUsbListenPort(int value);

    boolean getAdminConsole();

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

    String getHotKey();

    void setHotKey(String value);

    String[] getLocalizedStrings();

    void setLocalizedStrings(String[] value);

    String getMenu();

    void setMenu(String value);

    String getGuestID();

    void setGuestID(String value);

    boolean getNoTaskMgrExecution();

    void setNoTaskMgrExecution(boolean value);

    boolean getSendCtrlAltDelete();

    void setSendCtrlAltDelete(boolean value);

    boolean getUsbAutoShare();

    void setUsbAutoShare(boolean value);

    void setWANDisableEffects(List<WANDisableEffects> disableEffects);

    List<WANDisableEffects> getWANDisableEffects();

    void setWANColorDepth(WanColorDepth colorDepth);

    WanColorDepth getWANColorDepth();

    String getUsbFilter();

    void setUsbFilter(String value);

    void Connect();

    void Install();

    void setCurrentVersion(Version currentVersion);

    void setSpiceBaseURL(String spiceBaseURL);

    boolean getIsWanOptionsEnabled();

    void setIsWanOptionsEnabled(boolean enabled);

    public void setSmartcardEnabled(boolean enabled);

    public boolean isSmartcardEnabled();

    public void setOverrideEnabledSmartcard(boolean enabled);

    public boolean isSmartcardEnabledOverridden();
}
