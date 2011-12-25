package org.ovirt.engine.ui.userportal.client.uicommonext;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommon.Configurator;
import org.ovirt.engine.ui.uicommon.DataProvider;
import org.ovirt.engine.ui.uicommon.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommon.models.vms.ISpice;
import org.ovirt.engine.ui.userportal.client.Masthead;
import org.ovirt.engine.ui.userportal.client.common.UserPortalMode;
import org.ovirt.engine.ui.userportal.client.util.ClientAgentType;

import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.Response;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestCallback;
import com.google.gwt.http.client.RequestException;

public class UserPortalConfigurator extends Configurator implements IEventListener {
    
    private static UserPortalConfigurator instance = null;
    
    public EventDefinition spiceVersionFileFetchedEvent_Definition = new EventDefinition("spiceVersionFileFetched", UserPortalConfigurator.class);
    public Event spiceVersionFileFetchedEvent = new Event(spiceVersionFileFetchedEvent_Definition);
    
    public EventDefinition usbFilterFileFetchedEvent_Definition = new EventDefinition("usbFilterFileFetched", UserPortalConfigurator.class);
    public Event usbFilterFileFetchedEvent = new Event(usbFilterFileFetchedEvent_Definition);

    private final String DEFAULT_USB_FILTER = "-1,-1,-1,-1,0";
    private String usbFilter = DEFAULT_USB_FILTER;
    
    private boolean isInitialized;
    
    private UserPortalConfigurator()
    {       
        // Add event listeners
        spiceVersionFileFetchedEvent.addListener(this);
        usbFilterFileFetchedEvent.addListener(this);
        
        // Update spice version from the text files which are located on the server.
        // If can't update spice version - leave the default value from 'Configurator'. 
        ClientAgentType cat = new ClientAgentType();
        if (cat.getPlatform().equalsIgnoreCase("win32")) {
            updateSpiceVersion();
        } else if (cat.getPlatform().equalsIgnoreCase("win64")) {
            updateSpice64Version();
        }
        
        // Update USB filters
        updateUsbFilter();
    }
    
    public static UserPortalConfigurator getInstance() {
        if (instance == null) {
            instance = new UserPortalConfigurator();
        }
        return instance;
    }
    
    protected void setUsbFilter(String usbFilter) {
        this.usbFilter = usbFilter;
    }

    public String getUsbFilter() {
        return usbFilter;
    }
    
    public void updateSpiceVersion() {        
        fetchFile("SpiceVersion.txt", spiceVersionFileFetchedEvent);
    }
    
    public void updateSpice64Version() {        
        fetchFile("SpiceVersion_x64.txt", spiceVersionFileFetchedEvent);
    }
    
    public void updateUsbFilter() {        
        fetchFile("consoles/spice/usbfilter.txt", usbFilterFileFetchedEvent);
    }
    
    public void updateIsUsbEnabled(final ISpice spice) {
        // Get 'EnableUSBAsDefault' value from database
        AsyncDataProvider.IsUSBEnabledByDefault(new AsyncQuery(this,
            new INewAsyncCallback() {
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
                public void onError(Request request, Throwable exception) {                    
                }

                public void onResponseReceived(Request request, Response response) {
                    String result = response.getText();
                    onFetched.raise(this, new FileFetchEventArgs(result));
                }
            });
        } catch (RequestException e) {
        }
    }
    
    // Create a Version object from string
    public Version parseVersion(String versionStr){
        return new Version(versionStr.replace(',', '.').replace("\n", ""));
    }
    
    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {   
        if (ev.equals(spiceVersionFileFetchedEvent_Definition))
        {
            Version spiceVersion = parseVersion(((FileFetchEventArgs)args).getFileContent());            
            setSpiceVersion(spiceVersion);
        }
        else if (ev.equals(usbFilterFileFetchedEvent_Definition))
        {
            String usbFilter = ((FileFetchEventArgs)args).getFileContent();            
            setUsbFilter(usbFilter);
        }
    }
    
    public final class FileFetchEventArgs extends EventArgs
    {
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

    @Override
    public boolean getSpiceFullScreen()
	{
		return Masthead.getInstance().getUserPortalMode() == UserPortalMode.BASIC;
	}

    @Override
    public void Configure(ISpice spice)
    {
		spice.setDesiredVersion(getSpiceVersion());
		spice.setAdminConsole(getSpiceAdminConsole());
		spice.setFullScreen(getSpiceFullScreen());
		spice.setUsbFilter(usbFilter);
		
		if (!isInitialized) {
	        updateIsUsbEnabled(spice);
	        isInitialized = true;
		}
    }
}