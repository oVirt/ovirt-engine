package org.ovirt.engine.ui.uicommonweb;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ICommand;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

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
    public UICommand setIsExecutionAllowed(boolean value)
    {
        if (isExecutionAllowed != value)
        {
            isExecutionAllowed = value;
            if (value)
            {
                getExecuteProhibitionReasons().clear();
            }

            onPropertyChanged(new PropertyChangedEventArgs("IsExecutionAllowed")); //$NON-NLS-1$
        }

        return this;
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

    public UICommand setIsDefault(boolean value)
    {
        privateIsDefault = value;
        return this;
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

    public UICommand setIsCancel(boolean value)
    {
        privateIsCancel = value;
        return this;
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

    public boolean canExecute(Object parameter)
    {
        return true;
    }

    /**
     * Execute Command with parameters
     *
     * @param parameters
     */
    public void execute(Object... parameters)
    {
        if (!getIsAvailable() || !getIsExecutionAllowed())
        {
            return;
        }

        if (target != null)
        {
            if (parameters == null || parameters.length == 0) {
                target.executeCommand(this);
            } else {
                target.executeCommand(this, parameters);
            }
        }
    }

    /**
     * Execute command with no parameters
     */
    public void execute()
    {
        execute(new Object[0]);
    }

    @Override
    public UICommand setTitle(String value) {
        super.setTitle(value);
        return this;
    }
}
