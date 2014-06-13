package org.ovirt.engine.core.bll.storage;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.core.bll.AddVdsGroupCommand;
import org.ovirt.engine.core.bll.MultiLevelAdministrationHandler;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.utils.NetworkUtils;

public class AddEmptyStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T> {
    public AddEmptyStoragePoolCommand(T parameters) {
        super(parameters);
    }

    protected void addStoragePoolToDb() {
        getStoragePool().setId(Guid.newGuid());
        getStoragePool().setStatus(StoragePoolStatus.Uninitialized);

        Guid requestedMacPoolId = getStoragePool().getMacPoolId();
        Guid macPoolIdToUse = requestedMacPoolId == null
                ? getDbFacade().getMacPoolDao().getDefaultPool().getId()
                : requestedMacPoolId;

        getStoragePool().setMacPoolId(macPoolIdToUse);
        getStoragePoolDAO().save(getStoragePool());
    }

    @Override
    protected void executeCommand() {
        setDataCenterDetails();
        addStoragePoolToDb();
        getReturnValue().setActionReturnValue(getStoragePool().getId());
        addDefaultNetworks();
        setSucceeded(true);
    }

    private void setDataCenterDetails() {
        StoragePool dc = getParameters().getStoragePool();
        setCompatibilityVersion(dc.getcompatibility_version().toString());
        setQuotaEnforcementType(dc.getQuotaEnforcementType().name());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_POOL : AuditLogType.USER_ADD_STORAGE_POOL_FAILED;
    }

    private void addDefaultNetworks() {
        Network net = new Network();
        net.setId(Guid.newGuid());
        net.setName(NetworkUtils.getEngineNetwork());
        net.setDescription(AddVdsGroupCommand.DefaultNetworkDescription);
        net.setDataCenterId(getStoragePool().getId());
        net.setVmNetwork(true);
        getNetworkDAO().save(net);
        NetworkHelper.addPermissionsOnNetwork(getCurrentUser().getId(), net.getId());
        VnicProfile profile = NetworkHelper.createVnicProfile(net);
        getVnicProfileDao().save(profile);
        NetworkHelper.addPermissionsOnVnicProfile(getCurrentUser().getId(), profile.getId(), true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addCanDoActionMessage(VdcBllMessages.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean canDoAction() {
        boolean result = true;
        // set version to latest supported version if not given
        if (getStoragePool().getcompatibility_version().isNotValid()) {
            getStoragePool().setcompatibility_version(Version.getLast());
        }
        if (result && !(isStoragePoolUnique(getStoragePool().getName()))) {
            result = false;
            addCanDoActionMessage(VdcBllMessages.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        } else if (!checkStoragePoolNameLengthValid()) {
            result = false;
        } else if (!VersionSupport.checkVersionSupported(getStoragePool().getcompatibility_version())) {
            addCanDoActionMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        }
        return result;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Collections.singletonList(new PermissionSubject(MultiLevelAdministrationHandler.SYSTEM_OBJECT_ID,
                VdcObjectType.System,
                getActionType().getActionGroup()));
    }

}
