package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;

import com.google.gwt.core.client.GWT;

/**
 * Provides configuration values for client side.
 */
@SuppressWarnings("unused")
public class Configurator
{
    public enum GlusterModeEnum {
        ONLY_OVIRT,
        OVIRT_GLUSTER
    }

    private static final String DOCUMENTATION_LANG_PATH = "/en-US/";
    private static final String DOCUMENTATION_LIB_PATH = "html/";

    /**
     * Gets the value indincating whether the model state should be changed asynchronous in response on property change
     * or command execution.
     */
    private boolean privateIsAsync;

    public boolean getIsAsync()
    {
        return privateIsAsync;
    }

    protected void setIsAsync(boolean value)
    {
        privateIsAsync = value;
    }

    /**
     * Gets or sets the value specifying what is the desired Spice version.
     */
    private Version privateSpiceVersion;

    public Version getSpiceVersion()
    {
        return privateSpiceVersion;
    }

    protected void setSpiceVersion(Version value)
    {
        privateSpiceVersion = value;
    }

    private boolean privateIsAdmin;

    public boolean getIsAdmin()
    {
        return privateIsAdmin;
    }

    protected void setIsAdmin(boolean value)
    {
        privateIsAdmin = value;
    }

    private int privateSpiceDefaultUsbPort;

    public int getSpiceDefaultUsbPort()
    {
        return privateSpiceDefaultUsbPort;
    }

    protected void setSpiceDefaultUsbPort(int value)
    {
        privateSpiceDefaultUsbPort = value;
    }

    private int privateSpiceDisableUsbListenPort;

    public int getSpiceDisableUsbListenPort()
    {
        return privateSpiceDisableUsbListenPort;
    }

    protected void setSpiceDisableUsbListenPort(int value)
    {
        privateSpiceDisableUsbListenPort = value;
    }

    private boolean privateIsUsbEnabled;

    public boolean getIsUsbEnabled()
    {
        return privateIsUsbEnabled;
    }

    protected void setIsUsbEnabled(boolean value)
    {
        privateIsUsbEnabled = value;
    }

    private boolean privateSpiceAdminConsole;

    public boolean getSpiceAdminConsole()
    {
        return privateSpiceAdminConsole;
    }

    protected void setSpiceAdminConsole(boolean value)
    {
        privateSpiceAdminConsole = value;
    }

    private boolean privateSpiceFullScreen;

    public boolean getSpiceFullScreen()
    {
        return privateSpiceFullScreen;
    }

    protected void setSpiceFullScreen(boolean value)
    {
        privateSpiceFullScreen = value;
    }

    private ValidateServerCertificateEnum privateValidateServerCertificate = ValidateServerCertificateEnum.values()[0];

    public ValidateServerCertificateEnum getValidateServerCertificate()
    {
        return privateValidateServerCertificate;
    }

    protected void setValidateServerCertificate(ValidateServerCertificateEnum value)
    {
        privateValidateServerCertificate = value;
    }

    private String privateBackendPort;

    public String getBackendPort()
    {
        return privateBackendPort;
    }

    protected void setBackendPort(String value)
    {
        privateBackendPort = value;
    }

    private String privateLogLevel;

    public String getLogLevel()
    {
        return privateLogLevel;
    }

    protected void setLogLevel(String value)
    {
        privateLogLevel = value;
    }

    /**
     * Specifies the interval fronend calls backend to check for updated results for registered queries and searches.
     * Values is in milliseconds.
     */
    private int privatePollingTimerInterval;

    public int getPollingTimerInterval()
    {
        return privatePollingTimerInterval;
    }

    protected void setPollingTimerInterval(int value)
    {
        privatePollingTimerInterval = value;
    }

    private boolean isDocumentationAvailable;

    public boolean isDocumentationAvailable() {
        return isDocumentationAvailable;
    }

    protected void setDocumentationAvailable(boolean isDocumentationAvailable) {
        this.isDocumentationAvailable = isDocumentationAvailable;
    }

    private String documentationBasePath;

    public String getDocumentationBasePath() {
        return documentationBasePath;
    }

    protected void setDocumentationBasePath(String documentationBasePath) {
        this.documentationBasePath = documentationBasePath;
    }

    public String getDocumentationBaseURL() {
        return GWT.getModuleBaseURL().replaceAll(GWT.getModuleName() + "/", "") + getDocumentationBasePath()
                + DOCUMENTATION_LANG_PATH;
    }

    public String getDocumentationLibURL() {
        return getDocumentationBaseURL() + DOCUMENTATION_LIB_PATH;
    }

    public Configurator()
    {
        setSpiceVersion(new Version(4, 4));
        setSpiceDefaultUsbPort(32023);
        setSpiceDisableUsbListenPort(0);
        setBackendPort("8080");
        setLogLevel("INFO");
        setPollingTimerInterval(5000);
    }

    public boolean IsDisplayTypeSupported(DisplayType displayType)
    {
        return true;
    }

    public void Configure(SearchableListModel searchableListModel)
    {
        searchableListModel.setIsAsync(getIsAsync());
        searchableListModel.setGlusterModeEnum(GlusterModeEnum.OVIRT_GLUSTER);
    }

    public void Configure(ISpice spice)
    {
        setIsUsbEnabled(DataProvider.IsUSBEnabledByDefault());
        int usbListenPort = getIsUsbEnabled() ? getSpiceDefaultUsbPort() : getSpiceDisableUsbListenPort();
        spice.setUsbListenPort(usbListenPort);

        spice.setDesiredVersion(getSpiceVersion());
        spice.setAdminConsole(getSpiceAdminConsole());
        spice.setFullScreen(getSpiceFullScreen());
    }
}
