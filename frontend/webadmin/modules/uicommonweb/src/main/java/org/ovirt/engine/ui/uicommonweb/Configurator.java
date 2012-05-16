package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.gwt.i18n.client.LocaleInfo;

/**
 * Provides configuration values for client side.
 */
public abstract class Configurator {

    private static final String DOCUMENTATION_LIB_PATH = "html/"; //$NON-NLS-1$

    // Temporarily save the locations of webadmin and userportal.
    // TODO: create a new SPICE RPM for webadmin
    public static final String WEBADMIN_ROOT_FOLDER = "/webadmin/webadmin/"; //$NON-NLS-1$
    public static final String USERPORTAL_ROOT_FOLDER = "/UserPortal/org.ovirt.engine.ui.userportal.UserPortal/"; //$NON-NLS-1$

    private static String documentationLangPath;

    public static String getDocumentationLangPath() {
        return documentationLangPath;
    }

    public Configurator() {
        // Set default configuration values
        setIsAdmin(true);
        setSpiceAdminConsole(true);
        setSpiceFullScreen(false);

        String currentLocale = LocaleInfo.getCurrentLocale().getLocaleName();

        documentationLangPath = (currentLocale.equals("default") ? "en-US" : currentLocale); //$NON-NLS-1$ //$NON-NLS-2$
        documentationLangPath = "/" + documentationLangPath + "/"; //$NON-NLS-1$ //$NON-NLS-2$
        setSpiceVersion(new Version(4, 4));
        setSpiceDefaultUsbPort(32023);
        setSpiceDisableUsbListenPort(0);
        setBackendPort("8080"); //$NON-NLS-1$
        setLogLevel("INFO"); //$NON-NLS-1$
        setPollingTimerInterval(5000);

        // Update Spice version if needed
        updateSpiceVersion();
    }

    protected static final String DEFAULT_USB_FILTER = "-1,-1,-1,-1,0"; //$NON-NLS-1$
    protected String usbFilter = DEFAULT_USB_FILTER;

    private boolean isInitialized;

    /**
     * Gets the value indicating whether the model state should be changed asynchronous in response on property change
     * or command execution.
     */
    private boolean privateIsAsync;

    public boolean getIsAsync() {
        return privateIsAsync;
    }

    protected void setIsAsync(boolean value) {
        privateIsAsync = value;
    }

    /**
     * Gets or sets the value specifying what is the desired Spice version.
     */
    private Version privateSpiceVersion;

    public Version getSpiceVersion() {
        return privateSpiceVersion;
    }

    protected void setSpiceVersion(Version value) {
        privateSpiceVersion = value;
    }

    private boolean privateIsAdmin;

    public boolean getIsAdmin() {
        return privateIsAdmin;
    }

    protected void setIsAdmin(boolean value) {
        privateIsAdmin = value;
    }

    private int privateSpiceDefaultUsbPort;

    public int getSpiceDefaultUsbPort() {
        return privateSpiceDefaultUsbPort;
    }

    protected void setSpiceDefaultUsbPort(int value) {
        privateSpiceDefaultUsbPort = value;
    }

    private int privateSpiceDisableUsbListenPort;

    public int getSpiceDisableUsbListenPort() {
        return privateSpiceDisableUsbListenPort;
    }

    protected void setSpiceDisableUsbListenPort(int value) {
        privateSpiceDisableUsbListenPort = value;
    }

    private boolean privateIsUsbEnabled;

    public boolean getIsUsbEnabled() {
        return privateIsUsbEnabled;
    }

    protected void setIsUsbEnabled(boolean value) {
        privateIsUsbEnabled = value;
    }

    private boolean privateSpiceAdminConsole;

    public boolean getSpiceAdminConsole() {
        return privateSpiceAdminConsole;
    }

    protected void setSpiceAdminConsole(boolean value) {
        privateSpiceAdminConsole = value;
    }

    private boolean privateSpiceFullScreen;

    public boolean getSpiceFullScreen() {
        return privateSpiceFullScreen;
    }

    protected void setSpiceFullScreen(boolean value) {
        privateSpiceFullScreen = value;
    }

    private ValidateServerCertificateEnum privateValidateServerCertificate = ValidateServerCertificateEnum.values()[0];

    public ValidateServerCertificateEnum getValidateServerCertificate() {
        return privateValidateServerCertificate;
    }

    protected void setValidateServerCertificate(ValidateServerCertificateEnum value) {
        privateValidateServerCertificate = value;
    }

    private String privateBackendPort;

    public String getBackendPort() {
        return privateBackendPort;
    }

    protected void setBackendPort(String value) {
        privateBackendPort = value;
    }

    private String privateLogLevel;

    public String getLogLevel() {
        return privateLogLevel;
    }

    protected void setLogLevel(String value) {
        privateLogLevel = value;
    }

    /**
     * Specifies the interval fronend calls backend to check for updated results for registered queries and searches.
     * Values is in milliseconds.
     */
    private int privatePollingTimerInterval;

    public int getPollingTimerInterval() {
        return privatePollingTimerInterval;
    }

    protected void setPollingTimerInterval(int value) {
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
        return removeModulName(GWT.getModuleBaseURL()) + getDocumentationBasePath()
                + documentationLangPath;
    }

    public String getDocumentationLibURL() {
        return getDocumentationBaseURL() + DOCUMENTATION_LIB_PATH;
    }

    protected String removeModulName(String moduleName) {
        return GWT.getModuleBaseURL().replaceAll(GWT.getModuleName() + "/", ""); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    public String getUsbFilter() {
        return usbFilter;
    }

    public void Configure(SearchableListModel searchableListModel) {
        searchableListModel.setIsAsync(getIsAsync());
    }

    public void updateDocumentationBaseURL() {
        AsyncDataProvider.GetDocumentationBaseURL(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        String documentationBaseURL = (String) returnValue;
                        boolean isDocumentationAvailable = !documentationBaseURL.equals(""); //$NON-NLS-1$

                        setDocumentationAvailable(isDocumentationAvailable);
                        setDocumentationBasePath(documentationBaseURL);
                        onUpdateDocumentationBaseURL();
                    }
                }));
    }

    public void updateIsUsbEnabled(final ISpice spice) {
        // Get 'EnableUSBAsDefault' value from database
        AsyncDataProvider.IsUSBEnabledByDefault(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object target, Object returnValue) {
                // Update IsUsbEnabled value
                setIsUsbEnabled((Boolean) returnValue);
            }
        }));
    }

    // Fetch file from a specified path
    public void fetchFile(String filePath, final Event onFetched) {

        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, filePath);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    String result = response.getText();
                    onFetched.raise(this, new FileFetchEventArgs(result));
                }
            });
        } catch (RequestException e) {
        }
    }

    public final class FileFetchEventArgs extends EventArgs {
        private String fileContent;

        public String getFileContent() {
            return fileContent;
        }

        public void setFileContent(String fileContent) {
            this.fileContent = fileContent;
        }

        public FileFetchEventArgs(String fileContent) {
            setFileContent(fileContent);
        }
    }

    public static String getSpiceBaseURL() {
        return GWT.getModuleBaseURL().replace(WEBADMIN_ROOT_FOLDER, USERPORTAL_ROOT_FOLDER);
    }

    public void Configure(ISpice spice) {
        spice.setDesiredVersion(getSpiceVersion());
        spice.setCurrentVersion(getSpiceVersion());
        spice.setAdminConsole(getSpiceAdminConsole());
        spice.setFullScreen(getSpiceFullScreen());
        spice.setSpiceBaseURL(getSpiceBaseURL());
        spice.setUsbFilter(getUsbFilter());

        if (!isInitialized) {
            updateIsUsbEnabled(spice);
            isInitialized = true;
        }
    }

    public void updateSpice32Version() {
        fetchFile(getSpiceBaseURL() + "SpiceVersion.txt", getSpiceVersionFileFetchedEvent()); //$NON-NLS-1$
    }

    public void updateSpice64Version() {
        fetchFile(getSpiceBaseURL() + "SpiceVersion_x64.txt", getSpiceVersionFileFetchedEvent()); //$NON-NLS-1$
    }

    private void updateSpiceVersion() {
        // Update spice version from the text files which are located on the server.
        // If can't update spice version - leave the default value from the Configurator.
        if ((clientOsType().equalsIgnoreCase("Windows")) && (clientBrowserType().equalsIgnoreCase("Explorer"))) { //$NON-NLS-1$ //$NON-NLS-2$
            if (clientPlatformType().equalsIgnoreCase("win32")) { //$NON-NLS-1$
                updateSpice32Version();
            } else if (clientPlatformType().equalsIgnoreCase("win64")) { //$NON-NLS-1$
                updateSpice64Version();
            }
        }
    }

    public boolean IsDisplayTypeSupported(DisplayType displayType) {
        switch (displayType) {
        case vnc:
            return false;

        case qxl:
            return (clientOsType().equalsIgnoreCase("Windows") && clientBrowserType().equalsIgnoreCase("Explorer")) || //$NON-NLS-1$ //$NON-NLS-2$
                    (clientOsType().equalsIgnoreCase("Linux") && clientBrowserType().equalsIgnoreCase("Firefox")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return false;
    }

    // Create a Version object from string
    public Version parseVersion(String versionStr) {
        return new Version(versionStr.replace(',', '.').replace("\n", "")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected abstract Event getSpiceVersionFileFetchedEvent();

    protected abstract String clientOsType();

    protected abstract String clientBrowserType();

    protected abstract String clientPlatformType();

    protected void onUpdateDocumentationBaseURL() {
        // no-op. Override if needed
    }

}
