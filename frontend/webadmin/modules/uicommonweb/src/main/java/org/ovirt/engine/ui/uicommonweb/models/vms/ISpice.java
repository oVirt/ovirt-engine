package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.Version;

/**
 * Represents an implementor of a Spice. That way we have a bridge between Console model and concrete Spice accessor. In
 * case of WPF there will be direct Spice ActiveX instantiation, while Web implementor of Spice will generate
 * corresponding HTML.
 */
@SuppressWarnings("unused")
public interface ISpice
{
    // event EventHandler<ErrorCodeEventArgs> Disconnected;
    // event EventHandler<SpiceMenuItemEventArgs> MenuItemSelected;

    Event getDisconnectedEvent();

    Event getConnectedEvent();

    Event getMenuItemSelectedEvent();

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

    String getUsbFilter();

    void setUsbFilter(String value);

    void Connect();

    void Install();
}
