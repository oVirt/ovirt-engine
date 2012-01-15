package org.ovirt.engine.ui.uicommonweb.models.userportal;

import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ChangeUserPasswordParameters;
import org.ovirt.engine.core.common.action.LoginUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.DataProvider;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicommonweb.validation.IValidation;
import org.ovirt.engine.ui.uicommonweb.validation.NotEmptyValidation;
import org.ovirt.engine.ui.uicompat.FrontendActionAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendActionAsyncCallback;

@SuppressWarnings("unused")
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
            OnPropertyChanged(new PropertyChangedEventArgs("IsChangingPassword"));
        }
    }

    public static final String ENGINEUserRoleId = "00000000-0000-0000-0001-000000000001";
    public Guid Everyone = new Guid("eee00000-0000-0000-0000-123456789eee");
    public Guid Blank = new Guid("00000000-0000-0000-0000-000000000000");
    public Guid UserTemplateBasedVM = new Guid("def00009-0000-0000-0000-def000000009");

    private VdcUser privateLoggedUser;

    @Override
    public VdcUser getLoggedUser()
    {
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

    private java.util.ArrayList<ActionGroup> privateLoggedUserActionGroupList;

    public java.util.ArrayList<ActionGroup> getLoggedUserActionGroupList()
    {
        return privateLoggedUserActionGroupList;
    }

    public void setLoggedUserActionGroupList(java.util.ArrayList<ActionGroup> value)
    {
        privateLoggedUserActionGroupList = value;
    }

    private java.util.ArrayList<ActionGroup> privateENGINEUserActionGroupList;

    public java.util.ArrayList<ActionGroup> getENGINEUserActionGroupList()
    {
        return privateENGINEUserActionGroupList;
    }

    public void setENGINEUserActionGroupList(java.util.ArrayList<ActionGroup> value)
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
        setChangePasswordCommand(new UICommand("ChangePassword", this));

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

        // Clear config cache on login (to make sure we don't use old config in a new session)
        DataProvider.ClearConfigCache();

        Frontend.RunAction(VdcActionType.LoginUser, new LoginUserParameters((String) getUserName().getEntity(),
                (String) getPassword().getEntity(),
                (String) getDomain().getSelectedItem(),
                "",
                "",
                ""),
                new IFrontendActionAsyncCallback() {
                    @Override
                    public void Executed(FrontendActionAsyncResult result) {

                        UserPortalLoginModel model = (UserPortalLoginModel) result.getState();
                        VdcReturnValueBase returnValue = result.getReturnValue();
                        boolean success = returnValue != null && returnValue.getSucceeded();
                        if (success)
                        {
                            model.setLoggedUser((VdcUser) returnValue.getActionReturnValue());
                            model.getLoggedInEvent().raise(this, EventArgs.Empty);
                        }
                        else
                        {
                            model.getPassword().setEntity("");
                            if (returnValue != null)
                            {
                                model.setMessage(Linq.FirstOrDefault(returnValue.getCanDoActionMessages()));
                            }
                            model.getLoginFailedEvent().raise(this, EventArgs.Empty);
                        }

                    }
                }, this);
    }

    private void ChangePassword()
    {
        VdcReturnValueBase returnValue =
                Frontend.RunAction(VdcActionType.ChangeUserPassword,
                        new ChangeUserPasswordParameters((String) getUserName().getEntity(),
                                (String) getPassword().getEntity(),
                                (String) getNewPassword().getEntity(),
                                (String) getDomain().getSelectedItem()));

        if (returnValue != null && returnValue.getSucceeded())
        {
            // TODO:
        }
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
                        .add("TODO: Verify password field doesn't match a new password.");
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
        setENGINEUserActionGroupList(new java.util.ArrayList<ActionGroup>());
        this.setLoggedUser(LoggedUser);

        AsyncDataProvider.GetRoleActionGroupsByRoleId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void OnSuccess(Object target, Object returnValue) {

                        UserPortalLoginModel loginModel = (UserPortalLoginModel) target;
                        loginModel.setENGINEUserActionGroupList((java.util.ArrayList<ActionGroup>) returnValue);
                        loginModel.GetUserRoles(loginModel);

                    }
                }), new Guid(ENGINEUserRoleId));
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

                        java.util.ArrayList<permissions> permissions = (java.util.ArrayList<permissions>) returnValue;
                        java.util.ArrayList<Guid> roleIdList = new java.util.ArrayList<Guid>();
                        boolean everyoneBlankPermission = false;
                        for (permissions permission : permissions)
                        {
                            if (!everyoneBlankPermission)
                            {
                                everyoneBlankPermission =
                                        permission.getad_element_id().getValue().equals(Everyone)
                                                && permission.getObjectId().getValue().equals(Blank)
                                                && permission.getrole_id().getValue().equals(UserTemplateBasedVM)
                                                && permission.getObjectType().equals(VdcObjectType.VmTemplate);
                                if (everyoneBlankPermission)
                                {
                                    continue;
                                }
                            }
                            if (!roleIdList.contains(permission.getrole_id()))
                            {
                                roleIdList.add(permission.getrole_id());
                            }
                        }
                        UserPortalLoginModel loginModel1 = (UserPortalLoginModel) target;
                        loginModel1.setLoggedUserActionGroupList(new java.util.ArrayList<ActionGroup>());
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
                }), loginModel.getLoggedUser().getUserId());
    }

    // Create a list of ActionGroups associated with the user by retrieving each role's ActionGroups (and proceed to
    // Step4).
    // Use only as 'Step3' of 'UpdateIsENGINEUser'
    public void UpdateUserActionGroups(Object targetObject, java.util.ArrayList<Guid> roleIdList)
    {
        for (Guid roleID : roleIdList)
        {
            AsyncDataProvider.GetRoleActionGroupsByRoleId(new AsyncQuery(targetObject,
                    new INewAsyncCallback() {
                        @Override
                        public void OnSuccess(Object target, Object returnValue) {

                            UserPortalLoginModel loginModel = (UserPortalLoginModel) target;
                            java.util.ArrayList<ActionGroup> roleActionGroupList =
                                    (java.util.ArrayList<ActionGroup>) returnValue;
                            for (ActionGroup actionGroup : roleActionGroupList)
                            {
                                if (!loginModel.getLoggedUserActionGroupList().contains(actionGroup))
                                {
                                    loginModel.getLoggedUserActionGroupList().add(actionGroup);
                                }
                            }
                            loginModel.setRolesCounter(loginModel.getRolesCounter() - 1);
                            if (loginModel.getRolesCounter() == 0)
                            {
                                CheckIsENGINEUser(loginModel);
                            }

                        }
                    }), roleID);
        }
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
}
