package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.core.compat.IProvidePropertyChangedEvent;
import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.ProvidePropertyChangedEvent;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.Configurator.GlusterModeEnum;
import org.ovirt.engine.ui.uicommonweb.ICommandTarget;
import org.ovirt.engine.ui.uicommonweb.ILogger;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.common.ProgressModel;
import org.ovirt.engine.ui.uicompat.PropertyChangeNotifier;

@SuppressWarnings("unused")
public class Model extends PropertyChangeNotifier implements IEventListener, ICommandTarget, IProvidePropertyChangedEvent
{

    private Event privatePropertyChangedEvent;

    @Override
    public Event getPropertyChangedEvent()
    {
        return privatePropertyChangedEvent;
    }

    private void setPropertyChangedEvent(Event value)
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
            OnPropertyChanged(new PropertyChangedEventArgs("Window"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("ConfirmWindow"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("WidgetModel"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("Progress"));
        }
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
        setOpenDocumentationCommand(new UICommand("OpenDocumentation", this));
        OnPropertyChanged(new PropertyChangedEventArgs("HashName"));
    }

    /**
     * Gets or sets the title of this model. There is no specific purpose for this property, use it on your convinience.
     */
    private String title;

    public String getTitle()
    {
        return title;
    }

    public void setTitle(String value)
    {
        if (!StringHelper.stringsEqual(title, value))
        {
            title = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Title"));
        }
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsValid"));

            if (isValid)
            {
                getInvalidityReasons().clear();
            }
        }
    }

    private java.util.List<String> privateInvalidityReasons;

    public java.util.List<String> getInvalidityReasons()
    {
        return privateInvalidityReasons;
    }

    public void setInvalidityReasons(java.util.List<String> value)
    {
        privateInvalidityReasons = value;
    }

    private boolean isAvailable;

    public boolean getIsAvailable()
    {
        return isAvailable;
    }

    public void setIsAvailable(boolean value)
    {
        if (isAvailable != value)
        {
            isAvailable = value;
            OnIsAvailableChanged();
            OnPropertyChanged(new PropertyChangedEventArgs("IsAvailable"));
        }
    }

    private boolean isChangable;

    public boolean getIsChangable()
    {
        return isChangable;
    }

    public void setIsChangable(boolean value)
    {
        if (isChangable != value)
        {
            isChangable = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsChangable"));

            if (isChangable)
            {
                getChangeProhibitionReasons().clear();
            }
        }
    }

    private java.util.List<String> privateChangeProhibitionReasons;

    public java.util.List<String> getChangeProhibitionReasons()
    {
        return privateChangeProhibitionReasons;
    }

    private void setChangeProhibitionReasons(java.util.List<String> value)
    {
        privateChangeProhibitionReasons = value;
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsSelected"));
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsSelectable"));
        }
    }

    private java.util.List<UICommand> privateCommands;

    public java.util.List<UICommand> getCommands()
    {
        return privateCommands;
    }

    public void setCommands(java.util.List<UICommand> value)
    {
        privateCommands = value;
    }

    public UICommand getDefaultCommand() {
        java.util.List<UICommand> commands = getCommands();
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
        java.util.List<UICommand> commands = getCommands();
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
        if (!StringHelper.stringsEqual(message, value))
        {
            message = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Message"));
        }
    }

    private String privateinfo;

    private String getinfo()
    {
        return privateinfo;
    }

    private void setinfo(String value)
    {
        privateinfo = value;
    }

    public String getInfo()
    {
        return getinfo();
    }

    public void setInfo(String value)
    {
        setinfo(value);
        OnPropertyChanged(new PropertyChangedEventArgs("Info"));
    }

    private GlusterModeEnum privateGlusterModeEnum;

    public GlusterModeEnum getGlusterModeEnum() {
        return privateGlusterModeEnum;
    }

    public void setGlusterModeEnum(GlusterModeEnum privateGlusterModeEnum) {
        this.privateGlusterModeEnum = privateGlusterModeEnum;
    }

    public Model()
    {
        setPropertyChangedEvent(new Event(ProvidePropertyChangedEvent.Definition));

        // Resolve commonly used types.
        setConfigurator((Configurator) TypeResolver.getInstance().Resolve(Configurator.class));
        setLogger((ILogger) TypeResolver.getInstance().Resolve(ILogger.class));

        setInvalidityReasons(new ObservableCollection<String>());
        setIsValid(true);

        setChangeProhibitionReasons(new ObservableCollection<String>());
        setIsChangable(true);
        setIsAvailable(true);

        setIsSelectable(true);

        setCommands(new ObservableCollection<UICommand>());
    }

    /**
     * Override this method to initialize model, for example populate some properties with data here rather than in
     * constructor. But instantiation still should be done in constructor.
     */
    public void Initialize()
    {
    }

    protected void OnIsAvailableChanged()
    {
    }

    @Override
    protected void OnPropertyChanged(PropertyChangedEventArgs e)
    {
        super.OnPropertyChanged(e);
        getPropertyChangedEvent().raise(this, e);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        setLastExecutedCommand(command);
        if (command == getOpenDocumentationCommand())
        {
            OnPropertyChanged(new PropertyChangedEventArgs("OpenDocumentation"));
        }
    }

    /**
     * Assigns a new instance to the Progress property, indicating start of some operation on the model.
     *
     * @param operation
     */
    public void StartProgress(String operation)
    {
        ProgressModel tempVar = new ProgressModel();
        tempVar.setCurrentOperation(operation);
        setProgress(tempVar);
    }

    /**
     * Assigns null to the Progress property, indicating end of some operation on the model.
     */
    public void StopProgress()
    {
        setProgress(null);
    }
}
