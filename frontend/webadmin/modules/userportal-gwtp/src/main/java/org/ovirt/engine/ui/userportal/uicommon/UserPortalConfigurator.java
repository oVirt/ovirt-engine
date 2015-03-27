package org.ovirt.engine.ui.userportal.uicommon;

import java.util.List;

import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.uicommon.ContextSensitiveHelpManager;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.frontend.utils.BaseContextPathData;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.ISpice;
import org.ovirt.engine.ui.uicommonweb.models.vms.WANDisableEffects;
import org.ovirt.engine.ui.uicommonweb.models.vms.WanColorDepth;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.userportal.place.UserPortalPlaceManager;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class UserPortalConfigurator extends Configurator implements IEventListener {

    public static final String APPLICATION_NAME = "userportal"; //$NON-NLS-1$

    public EventDefinition spiceVersionFileFetchedEvent_Definition =
            new EventDefinition("spiceVersionFileFetched", UserPortalConfigurator.class); //$NON-NLS-1$
    public Event spiceVersionFileFetchedEvent = new Event(spiceVersionFileFetchedEvent_Definition);

    public EventDefinition documentationFileFetchedEvent_Definition =
        new EventDefinition("documentationFileFetched", UserPortalConfigurator.class); //$NON-NLS-1$
    public Event documentationFileFetchedEvent = new Event(documentationFileFetchedEvent_Definition);

    public EventDefinition usbFilterFileFetchedEvent_Definition =
            new EventDefinition("usbFilterFileFetched", UserPortalConfigurator.class); //$NON-NLS-1$
    public Event usbFilterFileFetchedEvent = new Event(usbFilterFileFetchedEvent_Definition);

    private final UserPortalPlaceManager placeManager;
    private final ClientAgentType clientAgentType;

    @Inject
    public UserPortalConfigurator(UserPortalPlaceManager placeManager,
            EventBus eventBus, ClientAgentType clientAgentType) {
        super();
        this.placeManager = placeManager;
        this.clientAgentType = clientAgentType;

        prepareContextSensitiveHelp();

        // This means that it is UserPortal application.
        setIsAdmin(false);
        setSpiceAdminConsole(false);

        // Add event listeners
        spiceVersionFileFetchedEvent.addListener(this);
        documentationFileFetchedEvent.addListener(this);
        usbFilterFileFetchedEvent.addListener(this);

        // Update USB filters
        updateUsbFilter();

        // Update Spice version if needed
        updateSpiceVersion();
    }

    protected void prepareContextSensitiveHelp() {
        fetchFile(getCshMappingUrl(APPLICATION_NAME), documentationFileFetchedEvent);
        // async callback calls ContextSensitiveHelpManager.init
    }

    public void updateUsbFilter() {
        fetchFile(BaseContextPathData.getInstance().getPath()
                + "services/files/usbfilter.txt", usbFilterFileFetchedEvent); //$NON-NLS-1$
    }

    @Override
    public void configure(ISpice spice) {
        super.configure(spice);

        updateWanColorDepthOptions(spice);
        updateWANDisableEffects(spice);
    }

    private void updateWANDisableEffects(final ISpice spice) {
        AsyncDataProvider.getWANDisableEffects(new AsyncQuery(this, new INewAsyncCallback() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSuccess(Object target, Object returnValue) {
                spice.setWANDisableEffects((List<WANDisableEffects>) returnValue);
            }
        }));
    }

    private void updateWanColorDepthOptions(final ISpice spice) {
        AsyncDataProvider.getWANColorDepth(new AsyncQuery(this, new INewAsyncCallback() {
            @Override
            public void onSuccess(Object target, Object returnValue) {
                spice.setWANColorDepth((WanColorDepth) returnValue);
            }
        }));
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args) {
        if (ev.matchesDefinition(spiceVersionFileFetchedEvent_Definition)) {
            Version spiceVersion = parseVersion(((FileFetchEventArgs) args).getFileContent());
            setSpiceVersion(spiceVersion);
        } else if (ev.matchesDefinition(documentationFileFetchedEvent_Definition)) {
            String cshMapping = ((FileFetchEventArgs) args).getFileContent();
            ContextSensitiveHelpManager.init(cshMapping);
        } else if (ev.matchesDefinition(usbFilterFileFetchedEvent_Definition)) {
            String usbFilter = ((FileFetchEventArgs) args).getFileContent();
            setUsbFilter(usbFilter);
        }
    }

    /**
     * Returns true if the basic view is shown, else returns false
     */
    @Override
    public boolean getSpiceFullScreen() {
        return placeManager.isMainSectionBasicPlaceVisible();
    }

    @Override
    protected Event getSpiceVersionFileFetchedEvent() {
        return spiceVersionFileFetchedEvent;
    }

    @Override
    protected String clientBrowserType() {
        return clientAgentType.browser;
    }

    @Override
    protected String clientOsType() {
        return clientAgentType.os;
    }

    @Override
    protected String clientPlatformType() {
        return clientAgentType.getPlatform();
    }

    @Override
    public Float clientBrowserVersion() {
        return clientAgentType.version;
    }
}
