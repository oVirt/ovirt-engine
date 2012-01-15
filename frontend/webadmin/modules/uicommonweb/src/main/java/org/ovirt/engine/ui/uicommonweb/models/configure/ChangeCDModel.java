package org.ovirt.engine.ui.uicommonweb.models.configure;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.Model;

@SuppressWarnings("unused")
public class ChangeCDModel extends Model
{

    // public event EventHandler Executed = delegate { };

    public static EventDefinition ExecutedEventDefinition;
    private Event privateExecutedEvent;

    public Event getExecutedEvent()
    {
        return privateExecutedEvent;
    }

    private void setExecutedEvent(Event value)
    {
        privateExecutedEvent = value;
    }

    private UICommand privateDoCommand;

    public UICommand getDoCommand()
    {
        return privateDoCommand;
    }

    private void setDoCommand(UICommand value)
    {
        privateDoCommand = value;
    }

    static
    {
        ExecutedEventDefinition = new EventDefinition("Executed", ChangeCDModel.class);
    }

    public ChangeCDModel()
    {
        setExecutedEvent(new Event(ExecutedEventDefinition));

        setDoCommand(new UICommand("Do", this));
    }

    private void Do()
    {
        getExecutedEvent().raise(this, EventArgs.Empty);
        // Executed(this, EventArgs.Empty);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getDoCommand())
        {
            Do();
        }
    }
}
