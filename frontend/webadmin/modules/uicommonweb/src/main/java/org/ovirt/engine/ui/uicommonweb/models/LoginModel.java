package org.ovirt.engine.ui.uicommonweb.models;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
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

    public static final EventDefinition loggedInEventDefinition;
    private Event privateLoggedInEvent;

    public Event getLoggedInEvent()
    {
        return privateLoggedInEvent;
    }

    private void setLoggedInEvent(Event value)
    {
        privateLoggedInEvent = value;
    }

    public static final EventDefinition loginFailedEventDefinition;
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

    private ListModel<String> privateProfile;

    public ListModel<String> getProfile()
    {
        return privateProfile;
    }

    private void setProfile(ListModel<String> value)
    {
        privateProfile = value;
    }

    private EntityModel<String> privateUserName;

    public EntityModel<String> getUserName()
    {
        return privateUserName;
    }

    private void setUserName(EntityModel<String> value)
    {
        privateUserName = value;
    }

    private EntityModel<String> privatePassword;

    public EntityModel<String> getPassword()
    {
        return privatePassword;
    }

    private void setPassword(EntityModel<String> value)
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
            onPropertyChanged(new PropertyChangedEventArgs("IsConnecting")); //$NON-NLS-1$
        }
    }

    private EntityModel<Boolean> createInstanceOnly;

    public EntityModel<Boolean> getCreateInstanceOnly() {
        return createInstanceOnly;
    }

    public void setCreateInstanceOnly(EntityModel<Boolean> createInstanceOnly) {
        this.createInstanceOnly = createInstanceOnly;
    }

    private DbUser privateLoggedUser;

    // If true, indicates that the model is in the process of logging in automatically
    private boolean loggingInAutomatically = false;

    public DbUser getLoggedUser()
    {
        return privateLoggedUser;
    }

    protected void setLoggedUser(DbUser value)
    {
        privateLoggedUser = value;
    }

    static
    {
        loggedInEventDefinition = new EventDefinition("LoggedIn", LoginModel.class); //$NON-NLS-1$
        loginFailedEventDefinition = new EventDefinition("LoginFailed", LoginModel.class); //$NON-NLS-1$
    }

    public LoginModel()
    {
        setLoggedInEvent(new Event(loggedInEventDefinition));
        setLoginFailedEvent(new Event(loginFailedEventDefinition));

        UICommand tempVar = new UICommand("Login", this); //$NON-NLS-1$
        tempVar.setIsExecutionAllowed(false);
        tempVar.setIsDefault(true);
        setLoginCommand(tempVar);
        getCommands().add(tempVar);

        setProfile(new ListModel<String>());
        getProfile().setIsChangable(false);
        setUserName(new EntityModel<String>());
        getUserName().setIsChangable(false);
        getUserName().getEntityChangedEvent().addListener(this);
        setPassword(new EntityModel<String>());
        getPassword().setIsChangable(false);
        setCreateInstanceOnly(new EntityModel<Boolean>(false));

        setIsConnecting(true);

        AsyncQuery _asyncQuery = new AsyncQuery();

        _asyncQuery.setModel(this);
        _asyncQuery.setHandleFailure(true);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object ReturnValue)
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
                    loginModel.getUserName().setIsChangable(true);
                    loginModel.getPassword().setIsChangable(true);
                    loginModel.getProfile().setIsChangable(true);
                }

                List<String> domains = (List<String>) ReturnValue;
                Collections.sort(domains);
                loginModel.getProfile().setItems(domains);

            }
        };
        AsyncDataProvider.getInstance().getAAAProfilesListViaPublic(_asyncQuery);
    }

    @Override
    public void eventRaised(Event ev, Object sender, EventArgs args)
    {
        super.eventRaised(ev, sender, args);

        if (ev.matchesDefinition(EntityModel.entityChangedEventDefinition) && sender == getUserName())
        {
            userName_EntityChanged();
        }
    }

    private void userName_EntityChanged()
    {
        getProfile().setIsChangable(getDomainAvailability());
    }

    private boolean getDomainAvailability()
    {
        // Check whether the user name contains domain part.
        boolean hasDomain = getUserNameParts(getUserName().getEntity())[1] != null;

        return !hasDomain;
    }

    private String[] getUserNameParts(String value)
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

    public void login()
    {
        if (!validate())
        {
            getLoginFailedEvent().raise(this, EventArgs.EMPTY);
            return;
        }

        startProgress(null);
        disableLoginScreen();

        String fullUserName = getUserName().getEntity();
        String[] parts = getUserNameParts(fullUserName);
        String domain = parts[1];
        AsyncQuery _asyncQuery = new AsyncQuery();
        _asyncQuery.setModel(this);
        _asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(Object model, Object result)
            {
                LoginModel loginModel = (LoginModel) model;
                DbUser user = null;
                if (result != null)
                {
                    VdcReturnValueBase returnValue = (VdcReturnValueBase) result;
                    if (returnValue.getSucceeded())
                    {
                        user = (DbUser) returnValue.getActionReturnValue();
                        loginModel.setLoggedUser(user);
                    }
                    if (user == null)
                    {
                        loginModel.getPassword().setEntity(""); //$NON-NLS-1$
                        loginModel.setMessage(Linq.firstOrDefault(returnValue.getCanDoActionMessages()));
                        loginModel.getUserName().setIsChangable(true);
                        loginModel.getPassword().setIsChangable(true);
                        loginModel.getProfile().setIsChangable(true);
                        loginModel.getLoginCommand().setIsExecutionAllowed(true);
                        loginModel.getLoginFailedEvent().raise(this, EventArgs.EMPTY);
                    }
                    else
                    {
                        raiseLoggedInEvent();
                    }
                    stopProgress();
                }
            }
        };
        Frontend.getInstance().loginAsync(fullUserName, getPassword().getEntity(),
                StringHelper.isNullOrEmpty(domain) ? getProfile().getSelectedItem() : domain, true,
                _asyncQuery);
    }

    protected void raiseLoggedInEvent() {
        // Cache all configurations values before logging-in
        AsyncDataProvider.getInstance().initCache(this);
    }

    public void autoLogin(DbUser user)
    {
        loggingInAutomatically = true;
        getUserName().setEntity(user.getLoginName());
        getProfile().setSelectedItem(user.getDomain());
        disableLoginScreen();
        setLoggedUser(user);
        Frontend.getInstance().setLoggedInUser(user);
        raiseLoggedInEvent();
    }

    protected void disableLoginScreen() {
        getUserName().setIsChangable(false);
        getPassword().setIsChangable(false);
        getProfile().setIsChangable(false);
        getLoginCommand().setIsExecutionAllowed(false);
    }

    protected boolean validate()
    {
        getUserName().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getPassword().validateEntity(new IValidation[] { new NotEmptyValidation() });
        getProfile().validateSelectedItem(new IValidation[] { new NotEmptyValidation() });

        return getUserName().getIsValid() && getPassword().getIsValid() && getProfile().getIsValid();
    }

    @Override
    public void executeCommand(UICommand command)
    {
        super.executeCommand(command);

        if (command == getLoginCommand())
        {
            login();
        }
        else if ("Cancel".equals(command.getName())) //$NON-NLS-1$
        {
            cancel();
        }
    }

    public void cancel()
    {
        setWindow(null);
    }

}
