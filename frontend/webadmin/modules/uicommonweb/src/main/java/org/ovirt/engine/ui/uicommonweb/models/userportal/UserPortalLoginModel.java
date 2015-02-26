package org.ovirt.engine.ui.uicommonweb.models.userportal;

import java.util.ArrayList;

import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.FrontendMultipleQueryAsyncResult;
import org.ovirt.engine.ui.uicompat.IFrontendMultipleQueryAsyncCallback;

public class UserPortalLoginModel extends LoginModel
{

    private static final ActionGroup ConsumeQuotaActionGroup = ActionGroup.CONSUME_QUOTA; // 901

    private EntityModel<Boolean> privateIsAutoConnect;

    public EntityModel<Boolean> getIsAutoConnect()
    {
        return privateIsAutoConnect;
    }

    private void setIsAutoConnect(EntityModel<Boolean> value)
    {
        privateIsAutoConnect = value;
    }

    private DbUser privateLoggedUser;

    @Override
    public DbUser getLoggedUser()
    {
        return privateLoggedUser;
    }

    @Override
    protected void setLoggedUser(DbUser value)
    {
        privateLoggedUser = value;
    }

    private EntityModel<Boolean> privateIsENGINEUser;

    public EntityModel<Boolean> getIsENGINEUser()
    {
        return privateIsENGINEUser;
    }

    public void setIsENGINEUser(EntityModel<Boolean> value)
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
        EntityModel<Boolean> tempVar = new EntityModel<Boolean>();
        tempVar.setEntity(true);
        setIsENGINEUser(tempVar);
        EntityModel<Boolean> tempVar2 = new EntityModel<Boolean>();
        tempVar2.setEntity(true);
        setIsAutoConnect(tempVar2);
    }

    @Override
    public void login()
    {
        // Completely override the base class functionality.
        if (!validate())
        {
            getLoginFailedEvent().raise(this, EventArgs.EMPTY);
            return;
        }

        startProgress(null);

        getUserName().setIsChangable(false);
        getPassword().setIsChangable(false);
        getProfile().setIsChangable(false);
        getLoginCommand().setIsExecutionAllowed(false);
        getIsAutoConnect().setIsChangable(false);

        AsyncQuery asyncQuery = new AsyncQuery();
        asyncQuery.setModel(this);
        asyncQuery.asyncCallback = new INewAsyncCallback() {
            @Override
            public void onSuccess(final Object model, final Object result) {
                UserPortalLoginModel loginModel = (UserPortalLoginModel) model;
                VdcReturnValueBase returnValue = (VdcReturnValueBase) result;
                boolean success = returnValue != null && returnValue.getSucceeded();
                if (success) {
                    loginModel.setLoggedUser((DbUser) returnValue.getActionReturnValue());
                    loginModel.raiseLoggedInEvent();
                } else {
                    loginModel.getPassword().setEntity(""); //$NON-NLS-1$
                    if (returnValue != null) {
                        loginModel.setMessages(returnValue.getCanDoActionMessages());
                    }
                    loginModel.getUserName().setIsChangable(true);
                    loginModel.getPassword().setIsChangable(true);
                    loginModel.getProfile().setIsChangable(true);
                    loginModel.getLoginCommand().setIsExecutionAllowed(true);
                    getIsAutoConnect().setIsChangable(true);
                    loginModel.getLoginFailedEvent().raise(this, EventArgs.EMPTY);
                }
                stopProgress();
            }
        };
        Frontend.getInstance().loginAsync(getUserName().getEntity(), getPassword().getEntity(),
                                          getProfile().getSelectedItem(), false, asyncQuery);
    }

    // Update IsENGINEUser flag.
    // Get 'ENGINEUser' role's ActionGroups (and proceed to Step2).
    public void updateIsENGINEUser(DbUser LoggedUser)
    {
        setENGINEUserActionGroupList(new ArrayList<ActionGroup>());
        this.setLoggedUser(LoggedUser);

        AsyncDataProvider.getInstance().getRoleActionGroupsByRoleId(new AsyncQuery(this,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        UserPortalLoginModel loginModel = (UserPortalLoginModel) target;
                        loginModel.setENGINEUserActionGroupList((ArrayList<ActionGroup>) returnValue);
                        // a user 'stays' a user if he has consume quota action group.
                        // so we need to apply the same logic to this ActionGroup as for
                        // engine user role's action group.
                        loginModel.getENGINEUserActionGroupList().add(ConsumeQuotaActionGroup);
                        loginModel.getUserRoles(loginModel);

                    }
                }), ApplicationGuids.engineUser.asGuid());
    }

    // Get logged user's permissions and create a list of roles associated with the user (and proceed to Step3).
    // Use only as 'Step2' of 'UpdateIsENGINEUser'
    public void getUserRoles(UserPortalLoginModel loginModel)
    {
        AsyncDataProvider.getInstance().getPermissionsByAdElementId(new AsyncQuery(loginModel,
                new INewAsyncCallback() {
                    @Override
                    public void onSuccess(Object target, Object returnValue) {

                        ArrayList<Permission> permissions = (ArrayList<Permission>) returnValue;
                        ArrayList<Guid> roleIdList = new ArrayList<Guid>();
                        for (Permission permission : permissions) {

                            // ignore:
                            // ALL Everyone/UserPoralBasedVM permissions and
                            // ALL Everyone/QuotaConsumer persmissions
                            // ALL Everyone/NetworkUser persmissions
                            if (isEveryoneUserPortalBasedVmPermission(permission)
                                    || isEveryoneQuotaConsumerPermission(permission)
                                    || isEveryoneVnicProfileUserPermission(permission)) {
                                continue;
                            }
                            if (!roleIdList.contains(permission.getRoleId()))
                            {
                                roleIdList.add(permission.getRoleId());
                            }
                        }
                        UserPortalLoginModel loginModel1 = (UserPortalLoginModel) target;
                        loginModel1.setLoggedUserActionGroupList(new ArrayList<ActionGroup>());
                        if (roleIdList.size() > 0)
                        {
                            loginModel1.setRolesCounter(roleIdList.size());
                            loginModel1.updateUserActionGroups(loginModel1, roleIdList);
                        }
                        else
                        {
                            checkIsENGINEUser(loginModel1);
                        }

                    }

                    private boolean isEveryoneVnicProfileUserPermission(Permission permission) {
                        return permission.getAdElementId().equals(ApplicationGuids.everyone.asGuid()) &&
                                permission.getRoleId().equals(ApplicationGuids.vnicProfileUser.asGuid());
                    }

                    private boolean isEveryoneUserPortalBasedVmPermission(Permission permission) {
                        return permission.getAdElementId().equals(ApplicationGuids.everyone.asGuid())
                                &&
                                permission.getRoleId()
                                        .equals(ApplicationGuids.userTemplateBasedVM.asGuid());
                    }

                    private boolean isEveryoneQuotaConsumerPermission(Permission permission) {
                        return permission.getAdElementId().equals(ApplicationGuids.everyone.asGuid()) &&
                                permission.getRoleId().equals(ApplicationGuids.quotaConsumer.asGuid());
                    }
                }), loginModel.getLoggedUser().getId());
    }

    // Create a list of ActionGroups associated with the user by retrieving each role's ActionGroups (and proceed to
    // Step4).
    // Use only as 'Step3' of 'UpdateIsENGINEUser'
    public void updateUserActionGroups(UserPortalLoginModel targetObject, ArrayList<Guid> roleIdList)
    {
        ArrayList<VdcQueryParametersBase> queryParamsList =
                new ArrayList<VdcQueryParametersBase>();
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<VdcQueryType>();
        for (Guid roleId : roleIdList) {
            queryTypeList.add(VdcQueryType.GetRoleActionGroupsByRoleId);
            queryParamsList.add(new IdQueryParameters(roleId));
        }
        Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamsList, new IFrontendMultipleQueryAsyncCallback() {

            @Override
            public void executed(FrontendMultipleQueryAsyncResult result) {
                for (int i = 0; i < result.getReturnValues().size(); i++) {
                    VdcQueryReturnValue retVal = result.getReturnValues().get(i);
                    ArrayList<ActionGroup> roleActionGroupList = retVal.getReturnValue();
                    for (ActionGroup actionGroup : roleActionGroupList) {
                        if (!UserPortalLoginModel.this.getLoggedUserActionGroupList().contains(actionGroup)) {
                            UserPortalLoginModel.this.getLoggedUserActionGroupList().add(actionGroup);
                        }
                    }
                    UserPortalLoginModel.this.setRolesCounter(UserPortalLoginModel.this.getRolesCounter() - 1);
                    if (UserPortalLoginModel.this.getRolesCounter() == 0) {
                        checkIsENGINEUser(UserPortalLoginModel.this);
                    }
                }
            }
        });
    }

    // If 'LoggedUserActionGroupList' contains an ActionGroup that doesn't exist in ENGINEUserActionGroupList
    // the logged user is not 'ENGINEUser' - Update IsENGINEUser to false; Otherwise, true.
    // Raise 'LoggedIn' event after updating the flag.
    // Use only as 'Step4' of 'UpdateIsENGINEUser'
    public void checkIsENGINEUser(UserPortalLoginModel loginModel)
    {
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

        if (loginModel.getLoggedUserActionGroupList().contains(ActionGroup.CREATE_INSTANCE)) {
            loginModel.getCreateInstanceOnly().setEntity(true);
        } else {
            loginModel.getCreateInstanceOnly().setEntity(false);
        }

        loginModel.getIsENGINEUser().setEntity(isENGINEUser);
    }
}
