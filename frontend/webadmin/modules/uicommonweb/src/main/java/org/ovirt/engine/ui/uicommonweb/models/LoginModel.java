package org.ovirt.engine.ui.uicommonweb.models;

import java.util.List;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.common.AboutModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.EventDefinition;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class LoginModel extends Model
{

    public static final String BeginLoginStage = "BeginTest"; //$NON-NLS-1$
    public static final String EndLoginStage = "EndTest"; //$NON-NLS-1$

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
            OnPropertyChanged(new PropertyChangedEventArgs("IsConnecting")); //$NON-NLS-1$
        }
    }

    private VdcUser privateLoggedUser;

    // If true, indicates that the model is in the process of logging in automatically
    private boolean loggingInAutomatically = false;

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
        LoggedInEventDefinition = new EventDefinition("LoggedIn", LoginModel.class); //$NON-NLS-1$
        LoginFailedEventDefinition = new EventDefinition("LoginFailed", LoginModel.class); //$NON-NLS-1$
    }

    public LoginModel()
    {
        setLoggedInEvent(new Event(LoggedInEventDefinition));
        setLoginFailedEvent(new Event(LoginFailedEventDefinition));

        UICommand tempVar = new UICommand("Login", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(false);
        tempVar.setIsDefault(true);
        setLoginCommand(tempVar);
        getCommands().add(tempVar);

        UICommand tempVar2 = new UICommand("About", this); //$NON-NLS-1$
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

        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void OnSuccess(Object model, Object ReturnValue)
            {

                setIsConnecting(false);

                LoginModel loginModel = (LoginModel) model;
                if (ReturnValue == null)
                {
                    loginModel.setMessage(ConstantsManager.getInstance()
                            .getConstants()
                            .couldNotConnectToOvirtEngineServiceMsg());
                    return;
                }

                if (!loggingInAutomatically) {
                    // Don't enable the screen when we are in the process of logging in automatically.
                    // If this happens to be executed before the AutoLogin() is executed,
                    // it is not a problem, as the AutoLogin() will disable the screen by itself.
                    loginModel.getLoginCommand().setIsExecutionAllowed(true);
                    loginModel.getAboutCommand().setIsExecutionAllowed(true);
                    loginModel.getUserName().setIsChangable(true);
                    loginModel.getPassword().setIsChangable(true);
                    loginModel.getDomain().setIsChangable(true);
                }

                List<String> domains = (List<String>) ReturnValue;
                loginModel.getDomain().setSelectedItem(Linq.FirstOrDefault(domains));
                loginModel.getDomain().setItems(domains);

            }
        };
        AsyncDataProvider.GetDomainListViaPublic(_asyncQuery, false);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(EntityModel.EntityChangedEventDefinition) && sender == getUserName())
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

        return new String[] { "", null }; //$NON-NLS-1$
    }

    public void Login()
    {
        if (!Validate())
        {
            return;
        }

        StartProgress(null);
        disableLoginScreen();

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
                        loginModel.getPassword().setEntity(""); //$NON-NLS-1$
                        if (returnValue != null)
                        {
                            loginModel.setMessage(Linq.FirstOrDefault(returnValue.getCanDoActionMessages()));
                        }
                        loginModel.getUserName().setIsChangable(true);
                        loginModel.getPassword().setIsChangable(true);
                        loginModel.getDomain().setIsChangable(true);
                        loginModel.getLoginCommand().setIsExecutionAllowed(true);
                        loginModel.getLoginFailedEvent().raise(this, EventArgs.Empty);
                    }
                    else
                    {
                        raiseLoggedInEvent();
                    }
                    StopProgress();
                }
            }
        };
        Frontend.LoginAsync(fullUserName,
                (String) getPassword().getEntity(),
                StringHelper.isNullOrEmpty(domain) ? (String) getDomain().getSelectedItem() : domain,
                _asyncQuery);
    }

    protected void raiseLoggedInEvent() {
        // Cache all configurations values before logging-in
        AsyncDataProvider.initCache(this);
    }

    public void AutoLogin(VdcUser user)
    {
        loggingInAutomatically = true;
        getUserName().setEntity(user.getUserName());
        getDomain().setSelectedItem(user.getDomainControler());
        disableLoginScreen();
        setLoggedUser(user);
        raiseLoggedInEvent();
    }

    protected void disableLoginScreen() {
        getUserName().setIsChangable(false);
        getPassword().setIsChangable(false);
        getDomain().setIsChangable(false);
        getLoginCommand().setIsExecutionAllowed(false);
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
        else if (StringHelper.stringsEqual(command.getName(), "Cancel")) //$NON-NLS-1$
        {
            Cancel();
        }
    }

    public void About()
    {
        AboutModel model = new AboutModel();
        setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().aboutOVirtEngineTitle());
        model.setHashName("about_rhev_manager"); //$NON-NLS-1$
        model.setShowOnlyVersion(true);

        UICommand tempVar = new UICommand("Cancel", this); //$NON-NLS-1$
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().close());
        tempVar.setIsDefault(true);
        tempVar.setIsCancel(true);
        model.getCommands().add(tempVar);
    }

    public void Cancel()
    {
        setWindow(null);
    }

    public void resetAfterLogout() {
        getUserName().setEntity(null);
        getPassword().setEntity(null);
        getPassword().setIsChangable(true);
        getUserName().setIsChangable(true);
        getDomain().setIsChangable(true);
        getLoginCommand().setIsExecutionAllowed(true);
        loggingInAutomatically = false;
        StopProgress();
    }

}
