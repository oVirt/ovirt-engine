package org.ovirt.engine.ui.uicommonweb;

import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ICommand;

/**
 * Represents a command adapted to use in model-viewmodel pattern + binding.
 */
@SuppressWarnings("unused")
public class UICommand extends Model implements ICommand
{

    private boolean isExecutionAllowed;

    /**
     * Gets or sets the flag indincating whether this command is available but can't be executed from some reasons.
     */
    public boolean getIsExecutionAllowed()
    {
        return isExecutionAllowed;
    }

    public void setIsExecutionAllowed(boolean value)
    {
        if (isExecutionAllowed != value)
        {
            isExecutionAllowed = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsExecutionAllowed"));

            if (getIsExecutionAllowed())
            {
                getExecuteProhibitionReasons().clear();
            }
        }
    }

    private java.util.List<String> privateExecuteProhibitionReasons;

    public java.util.List<String> getExecuteProhibitionReasons()
    {
        return privateExecuteProhibitionReasons;
    }

    public void setExecuteProhibitionReasons(java.util.List<String> value)
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

    private ICommandTarget target;
    private final boolean autoRefresh;

    public UICommand(String name, ICommandTarget target, boolean autoRefresh)
    {
        this(autoRefresh);
        setName(name);
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

    // C# TO JAVA CONVERTER TODO TASK: Events are not available in Java:
    // public event EventHandler CanExecuteChanged;

    public void Execute(Object parameter)
    {
        if (!getIsAvailable() || !getIsExecutionAllowed())
        {
            return;
        }

        if (target != null)
        {
            target.ExecuteCommand(this);
        }
    }

    public void Execute()
    {
        Execute(null);
    }
}
