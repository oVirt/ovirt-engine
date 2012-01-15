package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.EventDefinition;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.common.AboutModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;

@SuppressWarnings("unused")
public class LoginModel extends Model
{

    public static final String BeginLoginStage = "BeginTest";
    public static final String EndLoginStage = "EndTest";

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

    public static EventDefinition LoginFailedEventDefinition;
    private Event privateLoginFailedEvent;

    public Event getLoginFailedEvent()
    {
        return privateLoginFailedEvent;
    }

    private void setLoginFailedEvent(Event value)
    {
        privateLoginFailedEvent = value;
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

    private UICommand privateAboutCommand;

    public UICommand getAboutCommand()
    {
        return privateAboutCommand;
    }

    private void setAboutCommand(UICommand value)
    {
        privateAboutCommand = value;
    }

    private ListModel privateDomain;

    public ListModel getDomain()
    {
        return privateDomain;
    }

    private void setDomain(ListModel value)
    {
        privateDomain = value;
    }

    private EntityModel privateUserName;

    public EntityModel getUserName()
    {
        return privateUserName;
    }

    private void setUserName(EntityModel value)
    {
        privateUserName = value;
    }

    private EntityModel privatePassword;

    public EntityModel getPassword()
    {
        return privatePassword;
    }

    private void setPassword(EntityModel value)
    {
        privatePassword = value;
    }

    private boolean isConnecting;

    public boolean getIsConnecting()
    {
        return isConnecting;
    }

    public void setIsConnecting(boolean value)
    {
        if (isConnecting != value)
        {
            isConnecting = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsConnecting"));
        }
    }

    private VdcUser privateLoggedUser;

    public VdcUser getLoggedUser()
    {
        return privateLoggedUser;
    }

    protected void setLoggedUser(VdcUser value)
    {
        privateLoggedUser = value;
    }

    static
    {
        LoggedInEventDefinition = new EventDefinition("LoggedIn", LoginModel.class);
        LoginFailedEventDefinition = new EventDefinition("LoginFailed", LoginModel.class);
    }

    public LoginModel()
    {
        setLoggedInEvent(new Event(LoggedInEventDefinition));
        setLoginFailedEvent(new Event(LoginFailedEventDefinition));

        UICommand tempVar = new UICommand("Login", this);
        tempVar.setIsExecutionAllowed(false);
        setLoginCommand(tempVar);

        UICommand tempVar2 = new UICommand("About", this);
        tempVar2.setIsExecutionAllowed(false);
        setAboutCommand(tempVar2);

        setDomain(new ListModel());
        getDomain().setIsChangable(false);
        setUserName(new EntityModel());
        getUserName().setIsChangable(false);
        getUserName().getEntityChangedEvent().addListener(this);
        setPassword(new EntityModel());
        getPassword().setIsChangable(false);

        setIsConnecting(true);

        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {
                setIsConnecting(false);

                LoginModel loginModel = (LoginModel) model;
                if (ReturnValue == null)
                {
                    loginModel.setMessage("Could not connect to oVirt Engine Service, please try to refresh the page. If the problem persists contact your System Administrator.");
                    return;
                }
                AsyncQuery _asyncQuery1 = new AsyncQuery();
                _asyncQuery1.setModel(loginModel);
                _asyncQuery1.asyncCallback = new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object model1, Object ReturnValue1)
                    {
                        LoginModel loginModel1 = (LoginModel) model1;

                        loginModel1.getLoginCommand().setIsExecutionAllowed(true);
                        loginModel1.getAboutCommand().setIsExecutionAllowed(true);
                        loginModel1.getUserName().setIsChangable(true);
                        loginModel1.getPassword().setIsChangable(true);
                        loginModel1.getDomain().setIsChangable(true);

                        java.util.List<String> domains = (java.util.List<String>) ReturnValue1;
                        loginModel1.getDomain().setItems(domains);
                        loginModel1.getDomain().setSelectedItem(Linq.FirstOrDefault(domains));
                    }
                };
                AsyncDataProvider.GetDomainListViaPublic(_asyncQuery1, false);
            }
        };
        AsyncDataProvider.IsBackendAvailable(_asyncQuery);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.equals(EntityModel.EntityChangedEventDefinition) && sender == getUserName())
        {
            UserName_EntityChanged();
        }
    }

    private void UserName_EntityChanged()
    {
        getDomain().setIsChangable(GetDomainAvailability());
    }

    private boolean GetDomainAvailability()
    {
        // Check whether the user name contains domain part.
        boolean hasDomain = GetUserNameParts((String) getUserName().getEntity())[1] != null;

        return !hasDomain;
    }

    private String[] GetUserNameParts(String value)
    {
        if (!StringHelper.isNullOrEmpty(value))
        {
            int index = value.indexOf('@');

            // Always return array of two elements representing user name and domain.)
            return new String[] { index > -1 ? value.substring(0, index) : value,
                    index > -1 ? value.substring(index + 1) : null };
        }

        return new String[] { "", null };
    }

    public void Login()
    {
        if (!Validate())
        {
            return;
        }

        getUserName().setIsChangable(false);
        getPassword().setIsChangable(false);
        getDomain().setIsChangable(false);
        getLoginCommand().setIsExecutionAllowed(false);

        // Clear config cache on login (to make sure we don't use old config in a new session)
        DataProvider.ClearConfigCache();

        String fullUserName = (String) getUserName().getEntity();
        String[] parts = GetUserNameParts(fullUserName);
        String domain = parts[1];
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object result)
            {
                LoginModel loginModel = (LoginModel) model;
                VdcUser user = null;
                if (result != null)
                {
                    VdcReturnValueBase returnValue = (VdcReturnValueBase) result;
                    if (returnValue != null && returnValue.getSucceeded())
                    {
                        user = (VdcUser) returnValue.getActionReturnValue();
                        loginModel.setLoggedUser(user);
                    }
                    if (user == null)
                    {
                        loginModel.getPassword().setEntity("");
                        if (returnValue != null)
                        {
                            loginModel.setMessage(Linq.FirstOrDefault(returnValue.getCanDoActionMessages()));
                        }
                        loginModel.getLoginFailedEvent().raise(this, EventArgs.Empty);
                    }
                    else
                    {
                        loginModel.getLoggedInEvent().raise(this, EventArgs.Empty);
                    }
                }
            }
        };
        Frontend.LoginAsync(fullUserName,
                (String) getPassword().getEntity(),
                StringHelper.isNullOrEmpty(domain) ? (String) getDomain().getSelectedItem() : domain,
                _asyncQuery);
    }

    public void AutoLogin(VdcUser user)
    {
        setLoggedUser(user);
        getLoggedInEvent().raise(this, EventArgs.Empty);
    }

    protected boolean Validate()
    {
        getUserName().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
        getDomain().ValidateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getUserName().getIsValid() && getPassword().getIsValid() && getDomain().getIsValid();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getLoginCommand())
        {
            Login();
        }
        else if (command == getAboutCommand())
        {
            About();
        }
        else if (StringHelper.stringsEqual(command.getName(), "Cancel"))
        {
            Cancel();
        }
    }

    public void About()
    {
        AboutModel model = new AboutModel();
        setWindow(model);
        model.setTitle("About oVirt Engine");
        model.setHashName("about_rhev_manager");
        model.setShowOnlyVersion(true);

        UICommand tempVar = new UICommand("Cancel", this);
        tempVar.setTitle("Close");
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    public void Cancel()
    {
        setWindow(null);
    }
}
