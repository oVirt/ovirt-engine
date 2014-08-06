package org.ovirt.engine.ui.uicommonweb.models;

import java.util.List;

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

public class Model implements IEventListener, ICommandTarget, IProvidePropertyChangedEvent
{

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

}
