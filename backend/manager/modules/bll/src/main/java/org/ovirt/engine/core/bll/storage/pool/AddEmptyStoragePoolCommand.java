package org.ovirt.engine.core.bll.storage.pool;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.AddClusterCommand;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.network.cluster.ManagementNetworkUtil;
import org.ovirt.engine.core.bll.network.cluster.NetworkHelper;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.bll.utils.VersionSupport;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.StoragePoolManagementParameter;
import org.ovirt.engine.core.common.businessentities.ActionGroup;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.StoragePoolStatus;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.network.NetworkFilterDao;

public class AddEmptyStoragePoolCommand<T extends StoragePoolManagementParameter> extends
        StoragePoolManagementCommandBase<T> {

    @Inject
    private ManagementNetworkUtil managementNetworkUtil;

    @Inject
    private NetworkFilterDao networkFilterDao;

    public AddEmptyStoragePoolCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }

    protected void addStoragePoolToDb() {
        getStoragePool().setId(Guid.newGuid());
        getStoragePool().setStatus(StoragePoolStatus.Uninitialized);

        getStoragePool().setMacPoolId(calculateMacPoolIdToUse());
        getStoragePoolDao().save(getStoragePool());
    }

    private Guid calculateMacPoolIdToUse() {
        Guid requestedMacPoolId = getStoragePool() == null ? null : getStoragePool().getMacPoolId();
        return requestedMacPoolId == null
                ? getDbFacade().getMacPoolDao().getDefaultPool().getId()
                : requestedMacPoolId;
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
        setCompatibilityVersion(dc.getCompatibilityVersion().toString());
        setQuotaEnforcementType(dc.getQuotaEnforcementType().name());
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_STORAGE_POOL : AuditLogType.USER_ADD_STORAGE_POOL_FAILED;
    }

    private void addDefaultNetworks() {
        Network net = new Network();
        net.setId(Guid.newGuid());
        net.setName(managementNetworkUtil.getDefaultManagementNetworkName());
        net.setDescription(AddClusterCommand.DefaultNetworkDescription);
        net.setDataCenterId(getStoragePool().getId());
        net.setVmNetwork(true);
        getNetworkDao().save(net);
        NetworkHelper.addPermissionsOnNetwork(getCurrentUser().getId(), net.getId());
        VnicProfile profile = NetworkHelper.createVnicProfile(net, networkFilterDao);
        getVnicProfileDao().save(profile);
        NetworkHelper.addPermissionsOnVnicProfile(getCurrentUser().getId(), profile.getId(), true);
    }

    @Override
    protected void setActionMessageParameters() {
        super.setActionMessageParameters();
        addValidationMessage(EngineMessage.VAR__ACTION__CREATE);
    }

    @Override
    protected boolean validate() {
        boolean result = true;
        // set version to latest supported version if not given
        if (getStoragePool().getCompatibilityVersion().isNotValid()) {
            getStoragePool().setCompatibilityVersion(Version.getLast());
        }
        if (result && !isStoragePoolUnique(getStoragePool().getName())) {
            result = false;
            addValidationMessage(EngineMessage.ACTION_TYPE_FAILED_STORAGE_POOL_NAME_ALREADY_EXIST);
        } else if (!checkStoragePoolNameLengthValid()) {
            result = false;
        } else if (!VersionSupport.checkVersionSupported(getStoragePool().getCompatibilityVersion())) {
            addValidationMessage(VersionSupport.getUnsupportedVersionMessage());
            result = false;
        }
        return result;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        return Arrays.asList(
                new PermissionSubject(Guid.SYSTEM, VdcObjectType.System, getActionType().getActionGroup()),
                new PermissionSubject(calculateMacPoolIdToUse(), VdcObjectType.MacPool, ActionGroup.CONFIGURE_MAC_POOL)
        );
    }

}
