package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.frontend.utils.FrontendUrlUtils;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleClient;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;

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

    private static final String DOCS_HTML_DIR = "html"; //$NON-NLS-1$
    private static final String DOCS_ROOT = BaseContextPathData.getRelativePath() + "docs"; //$NON-NLS-1$
    private static final String CSH_ROOT = BaseContextPathData.getRelativePath() + "docs/csh"; //$NON-NLS-1$
    private static final String JSON = ".json"; //$NON-NLS-1$

    private static String localeDir;

    public Configurator() {
        // Set default configuration values

        String locale = LocaleInfo.getCurrentLocale().getLocaleName();
        // doc package uses hyphens in the locale name dirs
        localeDir = locale.replaceAll("_", "-"); //$NON-NLS-1$ //$NON-NLS-2$

        setSpiceVersion(new Version(4, 4));
        setBackendPort("8700"); //$NON-NLS-1$
        setLogLevel("INFO"); //$NON-NLS-1$
        setPollingTimerInterval(10000);
    }

    protected static final String DEFAULT_USB_FILTER = "-1,-1,-1,-1,0"; //$NON-NLS-1$
    protected String usbFilter = DEFAULT_USB_FILTER;

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

    private boolean privateSpiceAdminConsole;

    public boolean getSpiceAdminConsole() {
        return privateSpiceAdminConsole;
    }

    protected void setSpiceAdminConsole(boolean value) {
        privateSpiceAdminConsole = value;
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

    /**
     * Returns the base URL for serving documentation.
     * <p>
     * Example: <code>https://&lt;ovirt-engine&gt;/docs/</code>
     *
     * @return Documentation base URL, including the trailing slash.
     */
    public String getDocsBaseUrl() {
        return FrontendUrlUtils.getRootURL() + DOCS_ROOT + "/"; //$NON-NLS-1$
    }

    /**
     * Returns the URL for serving the csh mapping file.
     * <p>
     * Example: <code>https://&lt;ovirt-engine&gt;/docs/csh/webadmin.json</code>
     *
     * @return the url
     */
    public String getCshMappingUrl(String application) {
        return FrontendUrlUtils.getRootURL() + CSH_ROOT + "/" + application + JSON; //$NON-NLS-1$
    }

    /**
     * Returns the base URL for retrieving Spice-related resources.
     */
    public static String getSpiceBaseURL() {
        return FrontendUrlUtils.getRootURL() + BaseContextPathData.getRelativePath()
                + "services/files/spice/"; //$NON-NLS-1$
    }

    protected void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    public String getUsbFilter() {
        return usbFilter;
    }

    // Fetch file from a specified path
    public void fetchFile(final String filePath, final Event onFetched) {
        RequestBuilder requestBuilder = new RequestBuilder(RequestBuilder.GET, filePath);
        try {
            requestBuilder.sendRequest(null, new RequestCallback() {
                @Override
                public void onError(Request request, Throwable exception) {
                    GWT.log("Error while requesting " + filePath, exception); //$NON-NLS-1$
                }

                @Override
                public void onResponseReceived(Request request, Response response) {
                    if (response.getStatusCode() == Response.SC_OK) {
                        String result = response.getText();
                        onFetched.raise(this, new FileFetchEventArgs(result));
                    }
                }
            });
        } catch (RequestException ignore) {
        }
    }

    public boolean isWebSocketProxyDefined() {
        String wsConfig = (String) AsyncDataProvider.getInstance().getConfigValuePreConverted(ConfigValues.WebSocketProxy);
        return wsConfig != null && !"".equals(wsConfig) && !"Off".equalsIgnoreCase(wsConfig); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static final class FileFetchEventArgs extends EventArgs {
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

    public void configure(ConsoleClient spice) {
        spice.getOptions().setAdminConsole(getSpiceAdminConsole());
        spice.getOptions().setUsbFilter(getUsbFilter());
        updateSpiceUsbAutoShare(spice);
        updateSpiceFullScreenDefault(spice);
    }

    private void updateSpiceUsbAutoShare(final ConsoleClient spice) {
        AsyncDataProvider.getInstance().getSpiceUsbAutoShare(new AsyncQuery<>(returnValue -> spice.getOptions().setUsbAutoShare(returnValue)));
    }

    protected abstract ConfigValues spiceFullScreenConfigKey();

    private void updateSpiceFullScreenDefault(final ConsoleClient spice) {
        AsyncDataProvider.getInstance().getConfigurationValueBoolean(new AsyncQuery<>(
                returnValue -> spice.getOptions().setFullScreen(returnValue)), spiceFullScreenConfigKey());
    }

    public void updateSpice32Version() {
        fetchFile(getSpiceBaseURL() + "SpiceVersion.txt", getSpiceVersionFileFetchedEvent()); //$NON-NLS-1$
    }

    public void updateSpice64Version() {
        fetchFile(getSpiceBaseURL() + "SpiceVersion_x64.txt", getSpiceVersionFileFetchedEvent()); //$NON-NLS-1$
    }

    protected void updateSpiceVersion() {
        // Update spice version from the text files which are located on the server.
        // If can't update spice version - leave the default value from the Configurator.
        if (clientPlatformType().equalsIgnoreCase("win32")) { //$NON-NLS-1$
            updateSpice32Version();
        } else if (clientPlatformType().equalsIgnoreCase("win64")) { //$NON-NLS-1$
            updateSpice64Version();
        }
    }

    public boolean isClientLinuxFirefox() {
        return clientOsType().equalsIgnoreCase("Linux") && clientBrowserType().equalsIgnoreCase("Firefox"); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public boolean isClientWindowsExplorer() {
        return isClientWindows() && clientBrowserType().equalsIgnoreCase("Explorer"); //$NON-NLS-1$
    }

    public boolean isClientWindows() {
        return clientOsType().equalsIgnoreCase("Windows"); //$NON-NLS-1$
    }

    // Create a Version object from string
    public Version parseVersion(String versionStr) {
        return new Version(versionStr.replace(',', '.').replace("\n", "")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    protected abstract Event<FileFetchEventArgs> getSpiceVersionFileFetchedEvent();

    protected abstract String clientOsType();

    protected abstract String clientBrowserType();

    protected abstract String clientPlatformType();

    public abstract Float clientBrowserVersion();

    /**
     * Returns the UI syntax checker instance or {@code null} if not available.
     */
    public abstract ISyntaxChecker getSyntaxChecker();

}
