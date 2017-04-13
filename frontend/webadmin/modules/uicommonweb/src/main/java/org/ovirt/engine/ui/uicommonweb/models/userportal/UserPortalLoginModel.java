package org.ovirt.engine.ui.uicommonweb.models.userportal;

import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.cpuProfileOperator;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.diskProfileUser;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.engineUser;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.everyone;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.quotaConsumer;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.userProfileEditor;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.userTemplateBasedVM;
import static org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids.vnicProfileUser;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.Permission;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.common.queries.VdcQueryParametersBase;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.frontend.AsyncCallback;
import org.ovirt.engine.ui.frontend.Frontend;
import org.ovirt.engine.ui.uicommonweb.auth.ApplicationGuids;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.LoginModel;

public class UserPortalLoginModel extends LoginModel {

    private static final ActionGroup ConsumeQuotaActionGroup = ActionGroup.CONSUME_QUOTA; // 901

    private DbUser privateLoggedUser;

    @Override
    public DbUser getLoggedUser() {
        return privateLoggedUser;
    }

    @Override
    protected void setLoggedUser(DbUser value) {
        privateLoggedUser = value;
    }

    private EntityModel<Boolean> privateIsENGINEUser;

    public EntityModel<Boolean> getIsENGINEUser() {
        return privateIsENGINEUser;
    }

    public void setIsENGINEUser(EntityModel<Boolean> value) {
        privateIsENGINEUser = value;
    }

    private ArrayList<ActionGroup> privateLoggedUserActionGroupList;

    public ArrayList<ActionGroup> getLoggedUserActionGroupList() {
        return privateLoggedUserActionGroupList;
    }

    public void setLoggedUserActionGroupList(ArrayList<ActionGroup> value) {
        privateLoggedUserActionGroupList = value;
    }

    private List<ActionGroup> engineUserActionGroupList;

    public List<ActionGroup> getEngineUserActionGroupList() {
        return engineUserActionGroupList;
    }

    public void setEngineUserActionGroupList(List<ActionGroup> value) {
        engineUserActionGroupList = value;
    }

    private int privateRolesCounter;

    public int getRolesCounter() {
        return privateRolesCounter;
    }

    public void setRolesCounter(int value) {
        privateRolesCounter = value;
    }

    public UserPortalLoginModel() {
        EntityModel<Boolean> tempVar = new EntityModel<>();
        tempVar.setEntity(true);
        setIsENGINEUser(tempVar);
    }

    // Update IsENGINEUser flag.
    // Get 'ENGINEUser' role's ActionGroups (and proceed to Step2).
    public void updateIsENGINEUser(DbUser LoggedUser) {
        setEngineUserActionGroupList(new ArrayList<ActionGroup>());
        this.setLoggedUser(LoggedUser);

        AsyncDataProvider.getInstance().getRoleActionGroupsByRoleId(new AsyncQuery<>(
                returnValue -> {

                    setEngineUserActionGroupList(returnValue);
                    // a user 'stays' a user if he has consume quota action group.
                    // so we need to apply the same logic to this ActionGroup as for
                    // engine user role's action group.
                    getEngineUserActionGroupList().add(ConsumeQuotaActionGroup);
                    getUserRoles();

                }), engineUser.asGuid());
    }

    // Get logged user's permissions and create a list of roles associated with the user (and proceed to Step3).
    // Use only as 'Step2' of 'UpdateIsENGINEUser'
    public void getUserRoles() {
        AsyncDataProvider.getInstance().getPermissionsByAdElementId(new AsyncQuery<>(
                new AsyncCallback<List<Permission>>() {
                    @Override
                    public void onSuccess(List<Permission> permissions) {

                        ArrayList<Guid> roleIdList = new ArrayList<>();
                        for (Permission permission : permissions) {

                            // ignore:
                            // ALL Everyone/UserPoralBasedVM permissions and
                            // ALL Everyone/QuotaConsumer persmissions
                            // ALL Everyone/NetworkUser persmissions
                            // ALL Everyone/DiskProfileUser permissions
                            // ALL Everyone/CpuProfileOperator permissions
                            if (isPermissionOf(everyone, userTemplateBasedVM, permission)
                                    || isPermissionOf(everyone, quotaConsumer, permission)
                                    || isPermissionOf(everyone, vnicProfileUser, permission)
                                    || isPermissionOf(everyone, diskProfileUser, permission)
                                    || isPermissionOf(everyone, userProfileEditor, permission)
                                    || isPermissionOf(everyone, cpuProfileOperator, permission)) {
                                continue;
                            }
                            if (!roleIdList.contains(permission.getRoleId())) {
                                roleIdList.add(permission.getRoleId());
                            }
                        }
                        setLoggedUserActionGroupList(new ArrayList<ActionGroup>());
                        if (roleIdList.size() > 0) {
                            setRolesCounter(roleIdList.size());
                            updateUserActionGroups(roleIdList);
                        }
                        else {
                            checkIsENGINEUser();
                        }

                    }

                    private boolean isPermissionOf(ApplicationGuids user, ApplicationGuids role, Permission permission) {
                        return permission.getAdElementId().equals(user.asGuid())
                                && permission.getRoleId().equals(role.asGuid());
                    }
                }), getLoggedUser().getId());
    }

    // Create a list of ActionGroups associated with the user by retrieving each role's ActionGroups (and proceed to
    // Step4).
    // Use only as 'Step3' of 'UpdateIsENGINEUser'
    public void updateUserActionGroups(ArrayList<Guid> roleIdList) {
        ArrayList<VdcQueryParametersBase> queryParamsList = new ArrayList<>();
        ArrayList<VdcQueryType> queryTypeList = new ArrayList<>();
        for (Guid roleId : roleIdList) {
            queryTypeList.add(VdcQueryType.GetRoleActionGroupsByRoleId);
            queryParamsList.add(new IdQueryParameters(roleId));
        }
        Frontend.getInstance().runMultipleQueries(queryTypeList, queryParamsList, result -> {
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
                    checkIsENGINEUser();
                }
            }
        });
    }

    // If 'LoggedUserActionGroupList' contains an ActionGroup that doesn't exist in ENGINEUserActionGroupList
    // the logged user is not 'ENGINEUser' - Update IsENGINEUser to false; Otherwise, true.
    // Raise 'LoggedIn' event after updating the flag.
    // Use only as 'Step4' of 'UpdateIsENGINEUser'
    public void checkIsENGINEUser() {
        getIsENGINEUser().setEntity(null);
        boolean isENGINEUser = true;

        for (ActionGroup actionGroup : getLoggedUserActionGroupList()) {
            if (!getEngineUserActionGroupList().contains(actionGroup)) {
                isENGINEUser = false;
                break;
            }
        }

        if (getLoggedUserActionGroupList().contains(ActionGroup.CREATE_INSTANCE)) {
            getCreateInstanceOnly().setEntity(true);
        } else {
            getCreateInstanceOnly().setEntity(false);
        }

        getIsENGINEUser().setEntity(isENGINEUser);
    }
}
