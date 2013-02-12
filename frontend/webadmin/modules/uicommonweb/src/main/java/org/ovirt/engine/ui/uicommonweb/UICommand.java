package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ICommand;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

import java.util.List;

/**
 * Represents a command adapted to use in model-viewmodel pattern + binding.
 */
@SuppressWarnings("unused")
public class UICommand extends Model implements ICommand
{

    private boolean isExecutionAllowed;

    /**
     * Gets or sets the flag indicating whether this command is available but can't be executed from some reasons.
     */
    public boolean getIsExecutionAllowed()
    {
        return isExecutionAllowed;
    }

    /*
     * Call this method after adding execute prohibition reasons.
     */
    public void setIsExecutionAllowed(boolean value)
    {
        if (isExecutionAllowed != value)
        {
            isExecutionAllowed = value;
            if (value)
            {
                getExecuteProhibitionReasons().clear();
            }

            OnPropertyChanged(new PropertyChangedEventArgs("IsExecutionAllowed")); //$NON-NLS-1$
        }
    }

    private List<String> privateExecuteProhibitionReasons;

    public List<String> getExecuteProhibitionReasons()
    {
        return privateExecuteProhibitionReasons;
    }

    public void setExecuteProhibitionReasons(List<String> value)
    {
        privateExecuteProhibitionReasons = value;
    }

    private boolean privateIsDefault;

    public boolean getIsDefault()
    {
        return privateIsDefault;
    }

    public void setIsDefault(boolean value)
    {
        privateIsDefault = value;
    }

    private boolean privateIsVisible = true;

    public boolean getIsVisible()
    {
        return privateIsVisible;
    }

    public void setIsVisible(boolean value)
    {
        privateIsVisible = value;
    }

    private boolean privateIsCancel;

    public boolean getIsCancel()
    {
        return privateIsCancel;
    }

    public void setIsCancel(boolean value)
    {
        privateIsCancel = value;
    }

    private String privateName;

    public String getName()
    {
        return privateName;
    }

    public void setName(String value)
    {
        privateName = value;
    }

    public ICommandTarget getTarget() {
        return target;
    }

    public void setTarget(ICommandTarget target) {
        this.target = target;
    }

    private ICommandTarget target;
    private final boolean autoRefresh;

    public UICommand(String name, ICommandTarget target, boolean autoRefresh)
    {
        this(autoRefresh);
        setName(name);
        setTitle(name);
        this.target = target;
    }

    public UICommand(String name, ICommandTarget target) {
        this(name, target, false);
    }

    private UICommand(boolean autoRefresh)
    {
        setExecuteProhibitionReasons(new ObservableCollection<String>());
        setIsExecutionAllowed(true);
        this.autoRefresh = autoRefresh;
    }

    public boolean isAutoRefresh() {
        return autoRefresh;
    }

    public boolean CanExecute(Object parameter)
    {
        return true;
    }

    /**
     * Execute Command with parameters
     *
     * @param parameters
     */
    public void Execute(Object... parameters)
    {
        if (!getIsAvailable() || !getIsExecutionAllowed())
        {
            return;
        }

        if (target != null)
        {
            if (parameters == null || parameters.length == 0) {
                target.ExecuteCommand(this);
            } else {
                target.ExecuteCommand(this, parameters);
            }
        }
    }

    /**
     * Execute command with no parameters
     */
    public void Execute()
    {
        Execute(new Object[0]);
    }
}
