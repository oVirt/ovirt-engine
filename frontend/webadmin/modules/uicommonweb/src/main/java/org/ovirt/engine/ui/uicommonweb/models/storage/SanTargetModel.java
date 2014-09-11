package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.StorageServerConnections;
import org.ovirt.engine.core.common.utils.ObjectUtils;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class SanTargetModel extends EntityModel<StorageServerConnections>
{

    public static final EventDefinition loggedInEventDefinition;
    private Event<EventArgs> privateLoggedInEvent;

    public Event<EventArgs> getLoggedInEvent()
    {
        return privateLoggedInEvent;
    }

    private void setLoggedInEvent(Event<EventArgs> value)
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
        if (!ObjectUtils.objectsEqual(address, value))
        {
            address = value;
            onPropertyChanged(new PropertyChangedEventArgs("Address")); //$NON-NLS-1$
        }
    }

    private String port;

    public String getPort()
    {
        return port;
    }

    public void setPort(String value)
    {
        if (!ObjectUtils.objectsEqual(port, value))
        {
            port = value;
            onPropertyChanged(new PropertyChangedEventArgs("Port")); //$NON-NLS-1$
        }
    }

    private String name;

    public String getName()
    {
        return name;
    }

    public void setName(String value)
    {
        if (!ObjectUtils.objectsEqual(name, value))
        {
            name = value;
            onPropertyChanged(new PropertyChangedEventArgs("Name")); //$NON-NLS-1$
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
            onPropertyChanged(new PropertyChangedEventArgs("IsLoggedIn")); //$NON-NLS-1$
        }
    }

    private List<LunModel> luns;

    public List<LunModel> getLuns()
    {
        return luns;
    }

    public void setLuns(List<LunModel> value)
    {
        if (luns != value)
        {
            luns = value;
            onPropertyChanged(new PropertyChangedEventArgs("Luns")); //$NON-NLS-1$
            getLunsList().setItems(luns);
        }
    }

    private ListModel<LunModel> lunsList;

    public ListModel<LunModel> getLunsList()
    {
        return lunsList;
    }

    public void setLunsList(ListModel<LunModel> value)
    {
        if (lunsList != value)
        {
            lunsList = value;
            onPropertyChanged(new PropertyChangedEventArgs("LunsList")); //$NON-NLS-1$
        }
    }

    static
    {
        loggedInEventDefinition = new EventDefinition("LoggedIn", SanTargetModel.class); //$NON-NLS-1$
    }

    public SanTargetModel()
    {
        setLoggedInEvent(new Event<EventArgs>(loggedInEventDefinition));
        setLoginCommand(new UICommand("", this)); //$NON-NLS-1$
        setLunsList(new ListModel<LunModel>());
    }

    private void login()
    {
        getLoggedInEvent().raise(this, EventArgs.EMPTY);
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getLoginCommand())
        {
            login();
        }
    }
}
