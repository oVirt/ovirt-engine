package org.ovirt.engine.core.bll.network.dc;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.PredefinedRoles;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddNetworkStoragePoolParameters;
import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.permissions;
import org.ovirt.engine.core.common.validation.group.CreateEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

public class AddNetworkCommand<T extends AddNetworkStoragePoolParameters> extends NetworkCommon<T> {
    public AddNetworkCommand(T parameters) {
        super(parameters);
    }

    @Override
    protected void executeCommand() {
        getParameters().getNetwork().setId(Guid.NewGuid());
        DbFacade.getInstance().getNetworkDao().save(getParameters().getNetwork());
        addPermissions();
        getReturnValue().setActionReturnValue(getParameters().getNetwork().getId());
        setSucceeded(true);
    }

    @Override
    protected boolean canDoAction() {
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__ADD);
        addCanDoActionMessage(VdcBllMessages.VAR__TYPE__NETWORK);

        if (!validateVmNetwork()) {
            return false;
        }

        if (!validateStpProperty()) {
            return false;
        }

        if (!validateMTUOverrideSupport()) {
            return false;
        }

        // check that network name not start with 'bond'
        if (getParameters().getNetwork().getname().toLowerCase().startsWith("bond")) {
            addCanDoActionMessage(VdcBllMessages.NETWORK_CANNOT_CONTAIN_BOND_NAME);
            return false;
        }

        // we return ok only if the network not exists
        List<Network> all;
        if (getParameters().getNetwork().getstorage_pool_id() != null
                && !getParameters().getNetwork().getstorage_pool_id().getValue().equals(Guid.Empty)) {
            all = DbFacade.getInstance().getNetworkDao().getAllForDataCenter(
                    getParameters().getNetwork().getstorage_pool_id().getValue());
        } else {
            all = DbFacade.getInstance().getNetworkDao().getAll();
        }
        boolean exists = null != LinqUtils.firstOrNull(all, new Predicate<Network>() {
            @Override
            public boolean eval(Network net) {
                return net.getname().equals(getParameters().getNetwork().getname());
            }
        });
        if (exists) {
            getReturnValue().getCanDoActionMessages()
                    .add(VdcBllMessages.NETWORK_NAME_ALREADY_EXISTS.toString());
            return false;
        }

        if (!validateVlanId(all)) {
            return false;
        }

        return true;
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.NETWORK_ADD_NETWORK : AuditLogType.NETWORK_ADD_NETWORK_FAILED;
    }

    @Override
    protected List<Class<?>> getValidationGroups() {
        addValidationGroup(CreateEntity.class);
        return super.getValidationGroups();
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(getStoragePoolId() == null ? null
                : getStoragePoolId().getValue(),
                VdcObjectType.StoragePool, getActionType().getActionGroup()));
    }

    private void addPermissions() {
        addPermissionOnNetwork(getCurrentUser().getUserId(), PredefinedRoles.NETWORK_ADMIN);

        // if the Network is for public use, set EVERYONE as a NETWORK_USER.
        if (getParameters().isPublicUse()) {
            addPermissionOnNetwork(MultiLevelAdministrationHandler.EVERYONE_OBJECT_ID, PredefinedRoles.NETWORK_USER);
        }
    }

    private void addPermissionOnNetwork(Guid userId, PredefinedRoles role) {
        permissions perms = new permissions();
        perms.setad_element_id(userId);
        perms.setObjectType(VdcObjectType.Network);
        perms.setObjectId(getParameters().getNetwork().getId());
        perms.setrole_id(role.getId());
        MultiLevelAdministrationHandler.addPermission(perms);
    }
}
