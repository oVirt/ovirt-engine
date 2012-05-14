package org.ovirt.engine.ui.webadmin.uicommon;

import org.ovirt.engine.core.common.businessentities.DisplayType;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.uicommon.DocumentationPathTranslator;
import org.ovirt.engine.ui.common.uicommon.SpiceInterfaceImpl;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent;
import org.ovirt.engine.ui.common.uicommon.model.UiCommonInitEvent.UiCommonInitHandler;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.http.client.Response;
import com.google.inject.Inject;

public class WebAdminConfigurator extends Configurator implements IEventListener, UiCommonInitHandler {

    // Temporarily save the locations of webadmin and userportal.
    // TODO: create a new SPICE RPM for webadmin
    private static final String WEBADMIN_ROOT_FOLDER = "/webadmin/webadmin/"; //$NON-NLS-1$
    private static final String USERPORTAL_ROOT_FOLDER = "/UserPortal/org.ovirt.engine.ui.userportal.UserPortal/"; //$NON-NLS-1$

    public static final String DOCUMENTATION_GUIDE_PATH = "Administration_Guide/index.html"; //$NON-NLS-1$

    public EventDefinition spiceVersionFileFetchedEvent_Definition =
            new EventDefinition("spiceVersionFileFetched", WebAdminConfigurator.class); //$NON-NLS-1$

    public Event spiceVersionFileFetchedEvent = new Event(spiceVersionFileFetchedEvent_Definition);

    public EventDefinition documentationFileFetchedEvent_Definition = new EventDefinition("documentationFileFetched", //$NON-NLS-1$
            WebAdminConfigurator.class);
    public Event documentationFileFetchedEvent = new Event(documentationFileFetchedEvent_Definition);

    private boolean isInitialized;

    @Inject
    public WebAdminConfigurator(EventBus eventBus)
    {
        eventBus.addHandler(UiCommonInitEvent.getType(), this);

        // Set default configuration values
        setIsAdmin(true);
        setSpiceAdminConsole(true);
        setSpiceFullScreen(false);

        // Add event listeners
        spiceVersionFileFetchedEvent.addListener(this);
        documentationFileFetchedEvent.addListener(this);

        // Update Spice version if needed
        updateSpiceVersion();
    }

    private void updateSpiceVersion() {
        // Update spice version from the text files which are located on the server.
        // If can't update spice version - leave the default value from the Configurator.
        ClientAgentType cat = new ClientAgentType();
        if ((cat.os.equalsIgnoreCase("Windows")) && (cat.browser.equalsIgnoreCase("Explorer"))) { //$NON-NLS-1$ //$NON-NLS-2$
            if (cat.getPlatform().equalsIgnoreCase("win32")) { //$NON-NLS-1$
                updateSpice32Version();
            } else if (cat.getPlatform().equalsIgnoreCase("win64")) { //$NON-NLS-1$
                updateSpice64Version();
            }
        }
    }

    public void updateSpice32Version() {
        fetchFile(getSpiceBaseURL() + "SpiceVersion.txt", spiceVersionFileFetchedEvent); //$NON-NLS-1$
    }

    public void updateSpice64Version() {
        fetchFile(getSpiceBaseURL() + "SpiceVersion_x64.txt", spiceVersionFileFetchedEvent); //$NON-NLS-1$
    }

    public void updateDocumentationPathFile() {
        fetchFile(getDocumentationBaseURL() + "DocumentationPath.csv", documentationFileFetchedEvent); //$NON-NLS-1$
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

    public void updateDocumentationBaseURL() {
        // Get 'EnableUSBAsDefault' value from database
        AsyncDataProvider.GetDocumentationBaseURL(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {
                        String documentationBaseURL = (String) returnValue;
                        boolean isDocumentationAvailable = !documentationBaseURL.equals(""); //$NON-NLS-1$

                        setDocumentationAvailable(isDocumentationAvailable);
                        setDocumentationBasePath(documentationBaseURL);
                        updateDocumentationPathFile();
                    }
                }));
    }

    public String getSpiceBaseURL() {
        return GWT.getModuleBaseURL().replace(WEBADMIN_ROOT_FOLDER, USERPORTAL_ROOT_FOLDER);
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

    // Create a Version object from string
    public Version parseVersion(String versionStr) {
        return new Version(versionStr.replace(',', '.').replace("\n", "")); //$NON-NLS-1$ //$NON-NLS-2$
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        if (ev.equals(spiceVersionFileFetchedEvent_Definition)) {
            Version spiceVersion = parseVersion(((FileFetchEventArgs) args).getFileContent());
            setSpiceVersion(spiceVersion);
        }
        else if (ev.equals(documentationFileFetchedEvent_Definition))
        {
            String documentationPathFileContent = ((FileFetchEventArgs) args).getFileContent();
            DocumentationPathTranslator.init(documentationPathFileContent);
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

    // Check whether the specified displayType is currently supported
    @Override
    public boolean IsDisplayTypeSupported(DisplayType displayType) {
        switch (displayType) {
        case vnc:
            return false;

        case qxl:
            ClientAgentType cat = new ClientAgentType();
            return (cat.os.equalsIgnoreCase("Windows") && cat.browser.equalsIgnoreCase("Explorer")) || //$NON-NLS-1$ //$NON-NLS-2$
                    (cat.os.equalsIgnoreCase("Linux") && cat.browser.equalsIgnoreCase("Firefox")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return false;
    }

    @Override
    public void Configure(ISpice spiceImpl) {
        SpiceInterfaceImpl spice = (SpiceInterfaceImpl) spiceImpl;
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

    @Override
    public void onUiCommonInit(UiCommonInitEvent event) {
        updateDocumentationBaseURL();
    }
}
