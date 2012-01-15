package org.ovirt.engine.ui.uicommonweb.models.storage;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

@SuppressWarnings("unused")
public class SanTargetModel extends EntityModel
{

    public static EventDefinition LoggedInEventDefinition;
    private Event privateLoggedInEvent;

    public Event getLoggedInEvent()
    {
        return privateLoggedInEvent;
    }

    private void setLoggedInEvent(Event value)
    {
        privateLoggedInEvent = value;
    }

    private UICommand privateLoginCommand;

    public UICommand getLoginCommand()
    {
        return privateLoginCommand;
    }

    public void setLoginCommand(UICommand value)
    {
        privateLoginCommand = value;
    }

    private String address;

    public String getAddress()
    {
        return address;
    }

    public void setAddress(String value)
    {
        if (!StringHelper.stringsEqual(address, value))
        {
            address = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Address"));
        }
    }

    private String port;

    public String getPort()
    {
        return port;
    }

    public void setPort(String value)
    {
        if (!StringHelper.stringsEqual(port, value))
        {
            port = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Port"));
        }
    }

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        if (!StringHelper.stringsEqual(name, value))
        {
            name = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Name"));
        }
    }

    private boolean isLoggedIn;

    public boolean getIsLoggedIn()
    {
        return isLoggedIn;
    }

    public void setIsLoggedIn(boolean value)
    {
        if (isLoggedIn != value)
        {
            isLoggedIn = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsLoggedIn"));
        }
    }

    private java.util.List<LunModel> luns;

    public java.util.List<LunModel> getLuns()
    {
        return luns;
    }

    public void setLuns(java.util.List<LunModel> value)
    {
        if (luns != value)
        {
            luns = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Luns"));
            getLunsList().setItems(luns);
        }
    }

    private ListModel lunsList;

    public ListModel getLunsList()
    {
        return lunsList;
    }

    public void setLunsList(ListModel value)
    {
        if (lunsList != value)
        {
            lunsList = value;
            OnPropertyChanged(new PropertyChangedEventArgs("LunsList"));
        }
    }

    static
    {
        LoggedInEventDefinition = new EventDefinition("LoggedIn", SanTargetModel.class);
    }

    public SanTargetModel()
    {
        setLoggedInEvent(new Event(LoggedInEventDefinition));
        setLoginCommand(new UICommand("Login", this));
        setLunsList(new ListModel());
    }

    private void Login()
    {
        getLoggedInEvent().raise(this, EventArgs.Empty);
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getLoginCommand())
        {
            Login();
        }
    }
}
