package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.common.ProgressModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.IProvidePropertyChangedEvent;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicompat.ProvidePropertyChangedEvent;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.event.shared.HasHandlers;
import com.google.inject.Inject;

public class Model implements IEventListener<EventArgs>, ICommandTarget, IProvidePropertyChangedEvent, HasHandlers, IModel {

    public static final String PROP_WINDOW = "Window"; //$NON-NLS-1$
    public static final String PROP_CONFIRM_WINDOW = "ConfirmWindow"; //$NON-NLS-1$

    /**
     * The GWT event bus.
     */
    private EventBus eventBus;

    /**
     * Set of invalid tabs, empty if the model doesn't support tabs.
     */
    private final Set<TabName> invalidTabs = new HashSet<>();

    private final List<HandlerRegistration> handlerRegistrations = new ArrayList<>();

    public static final String CANCEL_COMMAND = "Cancel"; //$NON-NLS-1$

    private Event<PropertyChangedEventArgs> propertyChangedEvent;

    @Override
    public Event<PropertyChangedEventArgs> getPropertyChangedEvent() {
        return propertyChangedEvent;
    }

    private void setPropertyChangedEvent(Event<PropertyChangedEventArgs> value) {
        propertyChangedEvent = value;
    }

    @Override
    public void cleanup() {
        if (hasEventBusSet()) {
            unsetEventBus();
        }

        cleanupEvents(getPropertyChangedEvent());

        for (UICommand command : getCommands()) {
            command.cleanup();
        }

        clearTabsValidity();
    }

    protected void cleanupEvents(Event<?>... events) {
        for (Event<?> event : events) {
            if (event != null) {
                event.clearListeners();
            }
        }
    }

    private Model window;

    @Override
    public Model getWindow() {
        return window;
    }

    @Override
    public void setWindow(Model value) {
        if (window != value) {
            window = value;
            onPropertyChanged(new PropertyChangedEventArgs(PROP_WINDOW));
        }
    }

    private Model confirmWindow;

    @Override
    public Model getConfirmWindow() {
        return confirmWindow;
    }

    @Override
    public void setConfirmWindow(Model value) {
        if (confirmWindow != value) {
            confirmWindow = value;
            onPropertyChanged(new PropertyChangedEventArgs(PROP_CONFIRM_WINDOW));
        }
    }

    private Model widgetModel;

    @Override
    public Model getWidgetModel() {
        return widgetModel;
    }

    public void setWidgetModel(Model value) {
        if (widgetModel != value) {
            widgetModel = value;
            onPropertyChanged(new PropertyChangedEventArgs("WidgetModel")); //$NON-NLS-1$
        }
    }

    private Configurator configurator;

    public Configurator getConfigurator() {
        return configurator;
    }

    private void setConfigurator(Configurator value) {
        configurator = value;
    }

    private ILogger logger;

    protected ILogger getLogger() {
        return logger;
    }

    private void setLogger(ILogger value) {
        logger = value;
    }

    private UICommand lastExecutedCommand;

    @Override
    public UICommand getLastExecutedCommand() {
        return lastExecutedCommand;
    }

    private void setLastExecutedCommand(UICommand value) {
        lastExecutedCommand = value;
    }

    private ProgressModel progress;

    /**
     * Represents a progress operation on the model.
     */
    @Override
    public ProgressModel getProgress() {
        return progress;
    }

    private void setProgress(ProgressModel value) {
        if (progress != value) {
            progress = value;
            onPropertyChanged(new PropertyChangedEventArgs(PropertyChangedEventArgs.PROGRESS));
        }
    }

    private EntityModel<Boolean> advancedMode;

    public EntityModel<Boolean> getAdvancedMode() {
        return advancedMode;
    }

    public void setAdvancedMode(EntityModel<Boolean> advancedMode) {
        this.advancedMode = advancedMode;
    }

    private String hashName;

    @Override
    public String getHashName() {
        return hashName;
    }

    public void setHashName(String value) {
        hashName = value;
        onPropertyChanged(new PropertyChangedEventArgs("HashName")); //$NON-NLS-1$
    }

    private HelpTag helpTag;

    /**
     * Set the help tag for this model. This is used to connect context-sensitive help to a model/dialog.
     * <b>IMPORTANT</b>: only use values from {@code HelpTag}.
     * @param helpTag unique id from {@code HelpTag}
     */
    public void setHelpTag(HelpTag helpTag) {
        this.helpTag = helpTag;
        setOpenDocumentationCommand(new UICommand("OpenDocumentation", this)); //$NON-NLS-1$
        onPropertyChanged(new PropertyChangedEventArgs("HelpTag")); //$NON-NLS-1$
    }

    public HelpTag getHelpTag() {
        return helpTag;
    }

    private String applicationPlace;

    /**
     * Returns the logical application place associated with this model or {@code null} if this model
     * has no such place associated.
     */
    public String getApplicationPlace() {
        return applicationPlace;
    }

    protected void setApplicationPlace(String applicationPlace) {
        this.applicationPlace = applicationPlace;
    }

    /**
     * Gets or sets the title of this model. There is no specific purpose for this property, use it on your convenience.
     */
    private String title;

    public String getTitle() {
        return title;
    }

    public Model setTitle(String value) {
        if (!Objects.equals(title, value)) {
            title = value;
            onPropertyChanged(new PropertyChangedEventArgs("Title")); //$NON-NLS-1$
        }
        return this;
    }

    private boolean isValid;

    public boolean getIsValid() {
        return isValid;
    }

    public void setIsValid(boolean value) {
        if (isValid != value) {
            isValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsValid")); //$NON-NLS-1$

            if (isValid) {
                getInvalidityReasons().clear();
            }
        }
    }

    public void setIsValid(boolean value, String invalidityReason) {
        if (!value) {
            getInvalidityReasons().add(invalidityReason);
        }
        setIsValid(value);
    }

    /**
     * The invalidity reasons need to be set before {@code #setIsValid(boolean)}
     * in order to be displayed correctly.
     *
     * You can add the invalidity reason like this:
     * model.getInvalidityReasons().add("My reason");
     */
    private List<String> invalidityReasons;

    public List<String> getInvalidityReasons() {
        return invalidityReasons;
    }

    public void setInvalidityReasons(List<String> value) {
        invalidityReasons = value;
    }

    private int availableInModes;

    public int getAvailableInModes() {
        return availableInModes;
    }

    public void setAvailableInModes(int value) {
        if (availableInModes != value) {
            availableInModes = value;
            onPropertyChanged(new PropertyChangedEventArgs("AvailableInModes")); //$NON-NLS-1$
        }
    }

    public void setAvailableInModes(ApplicationMode uiMode) {
        int value = uiMode.getValue();
        setAvailableInModes(value);
    }

    private boolean isAvailable;

    @Override
    public boolean getIsAvailable() {
        return isAvailable && ApplicationModeHelper.isAvailableInMode(getAvailableInModes());
    }

    public void setIsAvailable(boolean value) {
        if (isAvailable != value) {
            isAvailable = value;
            onIsAvailableChanged();
            onPropertyChanged(new PropertyChangedEventArgs("IsAvailable")); //$NON-NLS-1$
        }
    }

    private boolean isChangable;

    public boolean getIsChangable() {
        return isChangable;
    }

    public Model setIsChangeable(boolean value) {
        return setIsChangeable(value, null);
    }

    /**
     * If {@code value} parameter is true, {@code reason} parameter is ignored and prohibition reason is set to
     * {@code null}.
     */
    public Model setIsChangeable(boolean value, String reason) {
        if (isChangable != value) {
            isChangable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsChangable")); //$NON-NLS-1$
        }
        setChangeProhibitionReason(value ? null : reason);
        return this;
    }

    private String changeProhibitionReason;

    public String getChangeProhibitionReason() {
        return changeProhibitionReason;
    }

    public void setChangeProhibitionReason(String value) {
        if (!Objects.equals(changeProhibitionReason, value)) {
            changeProhibitionReason = value;
            onPropertyChanged(new PropertyChangedEventArgs("ChangeProhibitionReason")); //$NON-NLS-1$
        }
    }

    private boolean isSelected;

    public boolean getIsSelected() {
        return isSelected;
    }

    public void setIsSelected(boolean value) {
        if (isSelected != value) {
            isSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSelected")); //$NON-NLS-1$
        }
    }

    private boolean isSelectable;

    public boolean getIsSelectable() {
        return isSelectable;
    }

    public void setIsSelectable(boolean value) {
        if (isSelectable != value) {
            isSelectable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSelectable")); //$NON-NLS-1$
        }
    }

    private List<UICommand> commands;

    public List<UICommand> getCommands() {
        return commands;
    }

    public void setCommands(List<UICommand> value) {
        commands = value;
    }

    public UICommand getDefaultCommand() {
        List<UICommand> commands = getCommands();
        if (commands != null) {
            for (UICommand c : commands) {
                if (c.getIsDefault()) {
                    return c;
                }
            }
        }
        return null;
    }

    public UICommand getCancelCommand() {
        List<UICommand> commands = getCommands();
        if (commands != null) {
            for (UICommand c : commands) {
                if (c.getIsCancel()) {
                    return c;
                }
            }
        }
        return null;
    }

    private UICommand openDocumentationCommand;

    public UICommand getOpenDocumentationCommand() {
        return openDocumentationCommand;
    }

    public void setOpenDocumentationCommand(UICommand value) {
        openDocumentationCommand = value;
    }

    private String message;

    public String getMessage() {
        return message;
    }

    public void setMessage(String value) {
        if (!Objects.equals(message, value)) {
            message = value;
            onPropertyChanged(new PropertyChangedEventArgs("Message")); //$NON-NLS-1$
        }
    }

    public Model() {
        setPropertyChangedEvent(new Event<PropertyChangedEventArgs>(ProvidePropertyChangedEvent.definition));

        // Resolve commonly used types.
        setConfigurator(lookupConfigurator());
        setLogger(lookupLogger());

        setInvalidityReasons(new ObservableCollection<String>());
        setIsValid(true);

        setIsChangeable(true);
        setAvailableInModes(ApplicationMode.AllModes);
        setIsAvailable(true);

        setIsSelectable(true);

        setCommands(new ObservableCollection<UICommand>());

    }

    protected ILogger lookupLogger() {
        return (ILogger) TypeResolver.getInstance().resolve(ILogger.class);
    }

    protected Configurator lookupConfigurator() {
        return (Configurator) TypeResolver.getInstance().resolve(Configurator.class);
    }

    /**
     * Override this method to initialize model, for example populate some properties with data here rather than in
     * constructor. But instantiation still should be done in constructor.
     */
    public void initialize() {
    }

    protected void onIsAvailableChanged() {
    }

    // public modifier so that the view can force redraw by firing this event
    public void onPropertyChanged(String name) {
        onPropertyChanged(new PropertyChangedEventArgs(name));
    }

    protected void onPropertyChanged(PropertyChangedEventArgs e) {
        getPropertyChangedEvent().raise(this, e);
    }

    @Override
    public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
    }

    @Override
    public void executeCommand(UICommand command) {
        setLastExecutedCommand(command);
        if (command == getOpenDocumentationCommand()) {
            onPropertyChanged(new PropertyChangedEventArgs("OpenDocumentation")); //$NON-NLS-1$
        }
    }

    @Override
    public void executeCommand(UICommand uiCommand, Object... parameters) {
        executeCommand(uiCommand);
    }

    public void startProgress() {
        startProgress(null);
    }

    /**
     * Assigns a new instance to the Progress property, indicating start of some operation on the model.
     */
    public void startProgress(String operation) {
        ProgressModel tempVar = new ProgressModel();
        tempVar.setCurrentOperation(operation);
        setProgress(tempVar);
    }

    /**
     * Assigns null to the Progress property, indicating end of some operation on the model.
     */
    public void stopProgress() {
        setProgress(null);
    }

    /**
     * Get the GWT event bus.
     * @return The {@code EventBus}, can be null.
     */
    protected final EventBus getEventBus() {
        return eventBus;
    }

    /**
     * Set the GWT event bus.
     * @param eventBus The {@code EventBus}, can be null.
     */
    @Inject
    public final void setEventBus(EventBus eventBus) {
        this.eventBus = eventBus;
        registerHandlers();
    }

    /**
     * Unset the GWT event bus, use this when cleaning up models.
     */
    public final void unsetEventBus() {
        unregisterHandlers();
        this.eventBus = null;
    }

    /**
     * Allows one to check if the event bus has been set.
     * @return {@code true} if the event bus is set already, {@code false} otherwise.
     */
    public final boolean hasEventBusSet() {
        return this.eventBus != null;
    }

    /**
     * Register handlers after the {@code EventBus} has been set.
     * <p>
     * Make sure to use {@link #registerHandler} to ensure proper
     * handler cleanup when {@link #unsetEventBus} is called.
     */
    protected void registerHandlers() {
        // No-op, override as necessary
    }

    /**
     * Register a handler.
     * @param reg The {@code HandlerRegistration} returned from registering a handler.
     */
    public final void registerHandler(HandlerRegistration reg) {
        if (reg != null && !handlerRegistrations.contains(reg)) {
            handlerRegistrations.add(reg);
        }
    }

    /**
     * Unregister all registered handlers.
     */
    public final void unregisterHandlers() {
        for (HandlerRegistration reg: handlerRegistrations) {
            reg.removeHandler(); // can't call unregisterHandler(reg) as that would modify the list during iteration
        }
        handlerRegistrations.clear();
    }

    /**
     * Unregister a specific handler using its {@code HandlerRegistration}.
     * @param reg The {@code HandlerRegistration} to use to remove the handler.
     */
    public final void unregisterHandler(HandlerRegistration reg) {
        if (reg != null) {
            reg.removeHandler();
            handlerRegistrations.remove(reg);
        }
    }

    @Override
    public void fireEvent(GwtEvent<?> event) {
        getEventBus().fireEventFromSource(event, this);
    }

    public Set<TabName> getInvalidTabs() {
        return invalidTabs;
    }

    public void setValidTab(TabName tab, boolean value) {
        if (value) {
            invalidTabs.remove(tab);
        } else {
            invalidTabs.add(tab);
        }
    }

    public void clearTabsValidity() {
        invalidTabs.clear();
    }

    public boolean isValidTab(TabName tab) {
        return !invalidTabs.contains(tab);
    }

    public boolean allTabsValid() {
        return invalidTabs.isEmpty();
    }

    /**
     * Update Model's changeability based on the support of given feature in given version.
     *
     * @param feature {@link ConfigValues} [SomeFeature]Supported value
     * @param version compatibility version to check the feature against
     */
    public void updateChangeability(ConfigValues feature, Version version) {
        boolean featureSupported = (Boolean) AsyncDataProvider.getInstance().getConfigValuePreConverted(feature, version.getValue());

        setIsChangeable(featureSupported);
        setChangeProhibitionReason(ConstantsManager.getInstance().getMessages().optionNotSupportedClusterVersionTooOld(version.getValue()));
    }

    public class AsyncQuery<T> extends org.ovirt.engine.ui.frontend.AsyncQuery<T> {
        public AsyncQuery(){
        }

        public AsyncQuery(AsyncCallback<T> callback) {
            super(Model.this, callback);
        }

        public AsyncQuery(AsyncCallback<T> callback, boolean handleFailure) {
            super(Model.this, callback, handleFailure);
        }
    }

    public <T> AsyncQuery<T> asyncQuery(AsyncCallback<T> callback) {
        return new AsyncQuery<>(callback);
    }

    @Override
    public Map<String, Model> getWindowProperties() {
        Map<String, Model> map = new HashMap<>();
        map.put(PROP_WINDOW, getWindow());
        return map;
    }

    @Override
    public void setWindowProperty(String propName, Model value) {
        if (PROP_WINDOW.equals(propName)) {
            setWindow(value);
        }
    }

    @Override
    public Map<String, Model> getConfirmWindowProperties() {
        Map<String, Model> map = new HashMap<>();
        map.put(PROP_CONFIRM_WINDOW, getConfirmWindow());
        return map;
    }

    @Override
    public void setConfirmWindowProperty(String propName, Model value) {
        if (PROP_CONFIRM_WINDOW.equals(propName)) {
            setConfirmWindow(value);
        }
    }

}
