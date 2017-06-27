package org.ovirt.engine.ui.webadmin.uicommon;

import java.util.ArrayList;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.console.ConsoleOptions.WanDisableEffects;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.searchbackend.ISyntaxChecker;
import org.ovirt.engine.core.searchbackend.SyntaxCheckerFactory;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.uicommon.ContextSensitiveHelpManager;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.models.vms.ConsoleClient;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class WebAdminConfigurator extends Configurator implements IEventListener<Configurator.FileFetchEventArgs> {

    public static final String APPLICATION_NAME = "webadmin"; //$NON-NLS-1$

    public EventDefinition spiceVersionFileFetchedEvent_Definition =
            new EventDefinition("spiceVersionFileFetched", WebAdminConfigurator.class); //$NON-NLS-1$
    public Event<FileFetchEventArgs> spiceVersionFileFetchedEvent = new Event<>(spiceVersionFileFetchedEvent_Definition);

    public EventDefinition documentationFileFetchedEvent_Definition =
        new EventDefinition("documentationFileFetched", WebAdminConfigurator.class); //$NON-NLS-1$
    public Event<FileFetchEventArgs> documentationFileFetchedEvent = new Event<>(documentationFileFetchedEvent_Definition);

    private final ClientAgentType clientAgentType;

    @Inject
    public WebAdminConfigurator(EventBus eventBus, ClientAgentType clientAgentType) {
        super();
        this.clientAgentType = clientAgentType;

        prepareContextSensitiveHelp();

        // This means that this is WebAdmin application.
        setIsAdmin(true);
        setSpiceAdminConsole(true);

        // Add event listeners
        spiceVersionFileFetchedEvent.addListener(this);
        documentationFileFetchedEvent.addListener(this);

        // Update Spice version if needed
        updateSpiceVersion();
    }

    protected void prepareContextSensitiveHelp() {
        fetchFile(getCshMappingUrl(APPLICATION_NAME), documentationFileFetchedEvent);
        // async callback calls ContextSensitiveHelpManager.init
    }

    @Override
    public void eventRaised(Event<? extends FileFetchEventArgs> ev, Object sender, FileFetchEventArgs args) {
        if (ev.matchesDefinition(spiceVersionFileFetchedEvent_Definition)) {
            Version spiceVersion = parseVersion(args.getFileContent());
            setSpiceVersion(spiceVersion);
        } else if (ev.matchesDefinition(documentationFileFetchedEvent_Definition)) {
            String cshMapping = args.getFileContent();
            ContextSensitiveHelpManager.init(cshMapping);
        }
    }

    @Override
    public void configure(ConsoleClient spice) {
        super.configure(spice);
        spice.getOptions().setWanDisableEffects(new ArrayList<WanDisableEffects>());
        spice.getOptions().setWanOptionsEnabled(false);
    }

    @Override
    protected Event<FileFetchEventArgs> getSpiceVersionFileFetchedEvent() {
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

    @Override
    public ISyntaxChecker getSyntaxChecker() {
        return SyntaxCheckerFactory.createUISyntaxChecker("LDAP"); //$NON-NLS-1$
    }

    @Override
    protected ConfigValues spiceFullScreenConfigKey() {
        return ConfigValues.FullScreenWebadminDefault;
    }
}
