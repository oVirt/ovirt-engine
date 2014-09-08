package org.ovirt.engine.ui.uicommonweb.models;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.common.ProgressModel;
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

public class Model implements IEventListener, ICommandTarget, IProvidePropertyChangedEvent, HasHandlers
{
    /**
     * The GWT event bus.
     */
    private EventBus eventBus;

    /**
     * Set of invalid tabs, empty if the model doesn't support tabs.
     */
    private final Set<TabName> invalidTabs = new HashSet<TabName>();

    private final List<HandlerRegistration> handlerRegistrations = new ArrayList<HandlerRegistration>();

    public static final String CANCEL_COMMAND = "Cancel"; //$NON-NLS-1$

    private Event<PropertyChangedEventArgs> privatePropertyChangedEvent;

    @Override
    public Event<PropertyChangedEventArgs> getPropertyChangedEvent()
    {
        return privatePropertyChangedEvent;
    }

    private void setPropertyChangedEvent(Event<PropertyChangedEventArgs> value)
    {
        privatePropertyChangedEvent = value;
    }

    private Model window;

    public Model getWindow()
    {
        return window;
    }

    public void setWindow(Model value)
    {
        if (window != value)
        {
            window = value;
            onPropertyChanged(new PropertyChangedEventArgs("Window")); //$NON-NLS-1$
        }
    }

    private Model confirmWindow;

    public Model getConfirmWindow()
    {
        return confirmWindow;
    }

    public void setConfirmWindow(Model value)
    {
        if (confirmWindow != value)
        {
            confirmWindow = value;
            onPropertyChanged(new PropertyChangedEventArgs("ConfirmWindow")); //$NON-NLS-1$
        }
    }

    private Model widgetModel;

    public Model getWidgetModel()
    {
        return widgetModel;
    }

    public void setWidgetModel(Model value)
    {
        if (widgetModel != value)
        {
            widgetModel = value;
            onPropertyChanged(new PropertyChangedEventArgs("WidgetModel")); //$NON-NLS-1$
        }
    }

    private Configurator privateConfigurator;

    public Configurator getConfigurator()
    {
        return privateConfigurator;
    }

    private void setConfigurator(Configurator value)
    {
        privateConfigurator = value;
    }

    private ILogger privateLogger;

    protected ILogger getLogger()
    {
        return privateLogger;
    }

    private void setLogger(ILogger value)
    {
        privateLogger = value;
    }

    private UICommand privateLastExecutedCommand;

    public UICommand getLastExecutedCommand()
    {
        return privateLastExecutedCommand;
    }

    private void setLastExecutedCommand(UICommand value)
    {
        privateLastExecutedCommand = value;
    }

    private ProgressModel progress;

    /**
     * Represents a progress operation on the model.
     */
    public ProgressModel getProgress()
    {
        return progress;
    }

    private void setProgress(ProgressModel value)
    {
        if (progress != value)
        {
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

    private String privatehashName;

    private String gethashName()
    {
        return privatehashName;
    }

    private void sethashName(String value)
    {
        privatehashName = value;
    }

    public String getHashName()
    {
        return gethashName();
    }

    public void setHashName(String value)
    {
        sethashName(value);
        setOpenDocumentationCommand(new UICommand("OpenDocumentation", this)); //$NON-NLS-1$
        onPropertyChanged(new PropertyChangedEventArgs("HashName")); //$NON-NLS-1$
    }

    private HelpTag helpTag;

    /**
     * Set the help tag for this model. This is used to connect context-sensitive help to a model/dialog.
     * <b>IMPORTANT</b>: only use values from {@code HelpTag}.
     * @param helpTag unique id from {@code HelpTag}
     */
    public void setHelpTag(HelpTag helpTag)
    {
        this.helpTag = helpTag;
    }

    public HelpTag getHelpTag()
    {
        return helpTag;
    }

    /**
     * Gets or sets the title of this model. There is no specific purpose for this property, use it on your convenience.
     */
    private String title;

    public String getTitle()
    {
        return title;
    }

    public Model setTitle(String value)
    {
        if (!ObjectUtils.objectsEqual(title, value))
        {
            title = value;
            onPropertyChanged(new PropertyChangedEventArgs("Title")); //$NON-NLS-1$
        }
        return this;
    }

    private boolean isValid;

    public boolean getIsValid()
    {
        return isValid;
    }

    public void setIsValid(boolean value)
    {
        if (isValid != value)
        {
            isValid = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsValid")); //$NON-NLS-1$

            if (isValid)
            {
                getInvalidityReasons().clear();
            }
        }
    }

    private List<String> privateInvalidityReasons;

    public List<String> getInvalidityReasons()
    {
        return privateInvalidityReasons;
    }

    public void setInvalidityReasons(List<String> value)
    {
        privateInvalidityReasons = value;
    }

    private int availableInModes;

    public int getAvailableInModes()
    {
        return availableInModes;
    }

    public void setAvailableInModes(int value)
    {
        if (availableInModes != value)
        {
            availableInModes = value;
            onPropertyChanged(new PropertyChangedEventArgs("AvailableInModes")); //$NON-NLS-1$
        }
    }

    public void setAvailableInModes(ApplicationMode uiMode)
    {
        int value = uiMode.getValue();
        setAvailableInModes(value);
    }

    private boolean isAvailable;

    public boolean getIsAvailable()
    {
        return isAvailable && ApplicationModeHelper.isAvailableInMode(getAvailableInModes());
    }

    public void setIsAvailable(boolean value)
    {
        if (isAvailable != value)
        {
            isAvailable = value;
            onIsAvailableChanged();
            onPropertyChanged(new PropertyChangedEventArgs("IsAvailable")); //$NON-NLS-1$
        }
    }

    private boolean isChangable;

    public boolean getIsChangable()
    {
        return isChangable;
    }

    public Model setIsChangable(boolean value)
    {
        if (isChangable != value)
        {
            isChangable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsChangable")); //$NON-NLS-1$

            if (isChangable)
            {
                setChangeProhibitionReason(null);
            }
        }
        return this;
    }

    private String privateChangeProhibitionReason;

    public String getChangeProhibitionReason()
    {
        return privateChangeProhibitionReason;
    }

    public void setChangeProhibitionReason(String value)
    {
        if (!ObjectUtils.objectsEqual(privateChangeProhibitionReason, value)) {
            privateChangeProhibitionReason = value;
            onPropertyChanged(new PropertyChangedEventArgs("ChangeProhibitionReason")); //$NON-NLS-1$
        }
    }

    private boolean isSelected;

    public boolean getIsSelected()
    {
        return isSelected;
    }

    public void setIsSelected(boolean value)
    {
        if (isSelected != value)
        {
            isSelected = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSelected")); //$NON-NLS-1$
        }
    }

    private boolean isSelectable;

    public boolean getIsSelectable()
    {
        return isSelectable;
    }

    public void setIsSelectable(boolean value)
    {
        if (isSelectable != value)
        {
            isSelectable = value;
            onPropertyChanged(new PropertyChangedEventArgs("IsSelectable")); //$NON-NLS-1$
        }
    }

    private List<UICommand> privateCommands;

    public List<UICommand> getCommands()
    {
        return privateCommands;
    }

    public void setCommands(List<UICommand> value)
    {
        privateCommands = value;
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

    private UICommand privateOpenDocumentationCommand;

    public UICommand getOpenDocumentationCommand()
    {
        return privateOpenDocumentationCommand;
    }

    public void setOpenDocumentationCommand(UICommand value)
    {
        privateOpenDocumentationCommand = value;
    }

    private String message;

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String value)
    {
        if (!ObjectUtils.objectsEqual(message, value))
        {
            message = value;
            onPropertyChanged(new PropertyChangedEventArgs("Message")); //$NON-NLS-1$
        }
    }

    public Model()
    {
        setPropertyChangedEvent(new Event<PropertyChangedEventArgs>(ProvidePropertyChangedEvent.definition));

        // Resolve commonly used types.
        setConfigurator(lookupConfigurator());
        setLogger(lookupLogger());

        setInvalidityReasons(new ObservableCollection<String>());
        setIsValid(true);

        setIsChangable(true);
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
    public void initialize()
    {
    }

    protected void onIsAvailableChanged()
    {
    }

    protected void onPropertyChanged(PropertyChangedEventArgs e)
    {
        getPropertyChangedEvent().raise(this, e);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
    }

    @Override
    public void executeCommand(UICommand command)
    {
        setLastExecutedCommand(command);
        if (command == getOpenDocumentationCommand())
        {
            onPropertyChanged(new PropertyChangedEventArgs("OpenDocumentation")); //$NON-NLS-1$
        }
    }

    @Override
    public void executeCommand(UICommand uiCommand, Object... parameters) {
        executeCommand(uiCommand);
    }

    /**
     * Assigns a new instance to the Progress property, indicating start of some operation on the model.
     *
     * @param operation
     */
    public void startProgress(String operation)
    {
        ProgressModel tempVar = new ProgressModel();
        tempVar.setCurrentOperation(operation);
        setProgress(tempVar);
    }

    /**
     * Assigns null to the Progress property, indicating end of some operation on the model.
     */
    public void stopProgress()
    {
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
    public final void setEventBus(EventBus eventBus) {
        assert eventBus != null : "EventBus cannot be null"; //$NON-NLS-1$
        assert this.eventBus == null : "EventBus is already set"; //$NON-NLS-1$
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
        getEventBus().fireEvent(event);
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

    public boolean isValidTab(TabName tab) {
        return !invalidTabs.contains(tab);
    }

    public boolean allTabsValid() {
        return invalidTabs.isEmpty();
    }
}
