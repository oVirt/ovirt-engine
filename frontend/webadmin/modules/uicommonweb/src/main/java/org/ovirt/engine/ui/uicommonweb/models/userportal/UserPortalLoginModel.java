package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.ChangeUserPasswordParameters;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.queries.MultilevelAdministrationByRoleIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class UserPortalLoginModel extends LoginModel
{

    private UICommand privateChangePasswordCommand;

    public UICommand getChangePasswordCommand()
    {
        return privateChangePasswordCommand;
    }

    public void setChangePasswordCommand(UICommand value)
    {
        privateChangePasswordCommand = value;
    }

    private EntityModel privateNewPassword;

    public EntityModel getNewPassword()
    {
        return privateNewPassword;
    }

    private void setNewPassword(EntityModel value)
    {
        privateNewPassword = value;
    }

    private EntityModel privateVerifyPassword;

    public EntityModel getVerifyPassword()
    {
        return privateVerifyPassword;
    }

    private void setVerifyPassword(EntityModel value)
    {
        privateVerifyPassword = value;
    }

    private EntityModel privateIsAutoConnect;

    public EntityModel getIsAutoConnect()
    {
        return privateIsAutoConnect;
    }

    private void setIsAutoConnect(EntityModel value)
    {
        privateIsAutoConnect = value;
    }

    private boolean isChangingPassword;

    public boolean getIsChangingPassword()
    {
        return isChangingPassword;
    }

    public void setIsChangingPassword(boolean value)
    {
        if (isChangingPassword != value)
        {
            isChangingPassword = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsChangingPassword")); //$NON-NLS-1$
        }
    }

    private VdcUser privateLoggedUser;

    @Override
    public VdcUser getLoggedUser()
    {
        privateLoggedUser.setPassword((String) getPassword().getEntity());
        return privateLoggedUser;
    }

    @Override
    protected void setLoggedUser(VdcUser value)
    {
        privateLoggedUser = value;
    }

    private EntityModel privateIsENGINEUser;

    public EntityModel getIsENGINEUser()
    {
        return privateIsENGINEUser;
    }

    public void setIsENGINEUser(EntityModel value)
    {
        privateIsENGINEUser = value;
    }

    private ArrayList<ActionGroup> privateLoggedUserActionGroupList;

    public ArrayList<ActionGroup> getLoggedUserActionGroupList()
    {
        return privateLoggedUserActionGroupList;
    }

    public void setLoggedUserActionGroupList(ArrayList<ActionGroup> value)
    {
        privateLoggedUserActionGroupList = value;
    }

    private ArrayList<ActionGroup> privateENGINEUserActionGroupList;

    public ArrayList<ActionGroup> getENGINEUserActionGroupList()
    {
        return privateENGINEUserActionGroupList;
    }

    public void setENGINEUserActionGroupList(ArrayList<ActionGroup> value)
    {
        privateENGINEUserActionGroupList = value;
    }

    private int privateRolesCounter;

    public int getRolesCounter()
    {
        return privateRolesCounter;
    }

    public void setRolesCounter(int value)
    {
        privateRolesCounter = value;
    }

    public UserPortalLoginModel()
    {
        setChangePasswordCommand(new UICommand("ChangePassword", this)); //$NON-NLS-1$

        setNewPassword(new EntityModel());
        setVerifyPassword(new EntityModel());

        EntityModel tempVar = new EntityModel();
        tempVar.setEntity(true);
        setIsENGINEUser(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setEntity(true);
        setIsAutoConnect(tempVar2);
    }

    @Override
    public void Login()
    {
        // Completely override the base class functionality.
        if (!Validate())
        {
            return;
        }

        StartProgress(null);

        getUserName().setIsChangable(false);
        getPassword().setIsChangable(false);
        getDomain().setIsChangable(false);
        getLoginCommand().setIsExecutionAllowed(false);
        getIsAutoConnect().setIsChangable(false);

        Frontend.RunAction(VdcActionType.LoginUser, new LoginUserParameters((String) getUserName().getEntity(),
                (String) getPassword().getEntity(),
                (String) getDomain().getSelectedItem(), "", //$NON-NLS-1$
                "", //$NON-NLS-1$
                ""), //$NON-NLS-1$
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        UserPortalLoginModel model = (UserPortalLoginModel) result.getState();
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        boolean success = returnValue != null && returnValue.getSucceeded();
                        if (success)
                        {
                            model.setLoggedUser((VdcUser) returnValue.getActionReturnValue());
                            model.raiseLoggedInEvent();
                        }
                        else
                        {
                            model.getPassword().setEntity(""); //$NON-NLS-1$
                            if (returnValue != null)
                            {
                                model.setMessage(Linq.FirstOrDefault(returnValue.getCanDoActionMessages()));
                            }
                            model.getUserName().setIsChangable(true);
                            model.getPassword().setIsChangable(true);
                            model.getDomain().setIsChangable(true);
                            model.getLoginCommand().setIsExecutionAllowed(true);
                            getIsAutoConnect().setIsChangable(true);
                            model.getLoginFailedEvent().raise(this, EventArgs.Empty);
                        }
                        StopProgress();
                    }
                },
                this);
    }

    private void ChangePassword()
    {
        // TODO: Invoke the async query and handle failure correctly
        Frontend.RunAction(VdcActionType.ChangeUserPassword,
                        new ChangeUserPasswordParameters((String) getUserName().getEntity(),
                                (String) getPassword().getEntity(),
                                (String) getNewPassword().getEntity(),
                                (String) getDomain().getSelectedItem()));
    }

    @Override
    protected boolean Validate()
    {
        boolean baseValidation = super.Validate();

        if (getIsChangingPassword())
        {
            getNewPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });
            getVerifyPassword().ValidateEntity(new IValidation[] { new NotEmptyValidation() });

            // Check that the verify password field matches new password.
            if (!((String) getNewPassword().getEntity()).equals(getVerifyPassword().getEntity()))
            {
                getVerifyPassword().setIsValid(false);
                getVerifyPassword().getInvalidityReasons()
                        .add("TODO: Verify password field doesn't match a new password."); //$NON-NLS-1$
            }
        }

        return baseValidation && getNewPassword().getIsValid() && getVerifyPassword().getIsValid();
    }

    @Override
    public void ExecuteCommand(UICommand command)
    {
        super.ExecuteCommand(command);

        if (command == getChangePasswordCommand())
        {
            ChangePassword();
        }
    }

    // Update IsENGINEUser flag.
    // Get 'ENGINEUser' role's ActionGroups (and proceed to Step2).
    public void UpdateIsENGINEUser(VdcUser LoggedUser)
    {
        setENGINEUserActionGroupList(new ArrayList<ActionGroup>());
        this.setLoggedUser(LoggedUser);

        AsyncDataProvider.GetRoleActionGroupsByRoleId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UserPortalLoginModel loginModel = (UserPortalLoginModel) target;
                        loginModel.setENGINEUserActionGroupList((ArrayList<ActionGroup>) returnValue);
                        loginModel.GetUserRoles(loginModel);

                    }
                }), ApplicationGuids.engineUser.asGuid());
    }

    // Get logged user's permissions and create a list of roles associated with the user (and proceed to Step3).
    // Use only as 'Step2' of 'UpdateIsENGINEUser'
    public void GetUserRoles(Object targetObject)
    {
        UserPortalLoginModel loginModel = (UserPortalLoginModel) targetObject;
        AsyncDataProvider.GetPermissionsByAdElementId(new AsyncQuery(targetObject,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        ArrayList<permissions> permissions = (ArrayList<permissions>) returnValue;
                        ArrayList<Guid> roleIdList = new ArrayList<Guid>();
                        for (permissions permission : permissions) {

                            // ignore:
                            // ALL Everyone/UserPoralBasedVM permissions and
                            // ALL Everyone/QuotaConsumer persmissions
                            if (isEveyoneUserPortalBasedVmPermission(permission)
                                    || isEveryoneQuotaConsumerPermission(permission)) {
                                continue;
                            }
                            if (!roleIdList.contains(permission.getrole_id()))
                            {
                                roleIdList.add(permission.getrole_id());
                            }
                        }
                        UserPortalLoginModel loginModel1 = (UserPortalLoginModel) target;
                        loginModel1.setLoggedUserActionGroupList(new ArrayList<ActionGroup>());
                        if (roleIdList.size() > 0)
                        {
                            loginModel1.setRolesCounter(roleIdList.size());
                            loginModel1.UpdateUserActionGroups(loginModel1, roleIdList);
                        }
                        else
                        {
                            CheckIsENGINEUser(loginModel1);
                        }

                    }

                    private boolean isEveyoneUserPortalBasedVmPermission(permissions permission) {
                        return permission.getad_element_id().getValue().equals(ApplicationGuids.everyone.asGuid())
                                &&
                                permission.getrole_id()
                                        .getValue()
                                        .equals(ApplicationGuids.userTemplateBasedVM.asGuid());
                    }

                    private boolean isEveryoneQuotaConsumerPermission(permissions permission) {
                        return permission.getad_element_id().getValue().equals(ApplicationGuids.everyone.asGuid()) &&
                                permission.getrole_id().getValue().equals(ApplicationGuids.quotaConsumer.asGuid());
                    }
                }), loginModel.getLoggedUser().getUserId());
    }

    // Create a list of ActionGroups associated with the user by retrieving each role's ActionGroups (and proceed to
    // Step4).
    // Use only as 'Step3' of 'UpdateIsENGINEUser'
    public void UpdateUserActionGroups(Object targetObject, ArrayList<Guid> roleIdList)
    {
        ArrayList<VdcQueryParametersBase> queryParamsList =
                new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        for (Guid roleId : roleIdList) {
            queryTypeList.add(VdcQueryType.GetRoleActionGroupsByRoleId);
            queryParamsList.add(new MultilevelAdministrationByRoleIdParameters(roleId));
        }
        Frontend.RunMultipleQueries(queryTypeList, queryParamsList, new IFrontendMultipleQueryAsyncCallback() {

            @Override
            public void Executed(FrontendMultipleQueryAsyncResult result) {
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    VdcQueryReturnValue retVal = result.getReturnValues().get(i);
                    ArrayList<ActionGroup> roleActionGroupList =
                            (ArrayList<ActionGroup>) retVal.getReturnValue();
                    for (ActionGroup actionGroup : roleActionGroupList) {
                        if (!UserPortalLoginModel.this.getLoggedUserActionGroupList().contains(actionGroup)) {
                            UserPortalLoginModel.this.getLoggedUserActionGroupList().add(actionGroup);
                        }
                    }
                    UserPortalLoginModel.this.setRolesCounter(UserPortalLoginModel.this.getRolesCounter() - 1);
                    if (UserPortalLoginModel.this.getRolesCounter() == 0) {
                        CheckIsENGINEUser(UserPortalLoginModel.this);
                    }
                }
            }
        });
    }

    // If 'LoggedUserActionGroupList' contains an ActionGroup that doesn't exist in ENGINEUserActionGroupList
    // the logged user is not 'ENGINEUser' - Update IsENGINEUser to false; Otherwise, true.
    // Raise 'LoggedIn' event after updating the flag.
    // Use only as 'Step4' of 'UpdateIsENGINEUser'
    public void CheckIsENGINEUser(Object targetObject)
    {
        UserPortalLoginModel loginModel = (UserPortalLoginModel) targetObject;
        loginModel.getIsENGINEUser().setEntity(null);
        boolean isENGINEUser = true;

        for (ActionGroup actionGroup : loginModel.getLoggedUserActionGroupList())
        {
            if (!loginModel.getENGINEUserActionGroupList().contains(actionGroup))
            {
                isENGINEUser = false;
                break;
            }
        }

        loginModel.getIsENGINEUser().setEntity(isENGINEUser);
    }

    @Override
    public void resetAfterLogout() {
        super.resetAfterLogout();
        getIsAutoConnect().setIsChangable(true);
    }

}
