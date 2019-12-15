package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatus;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.Pair;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDao;

/**
 * This class adds a thinly provisioned VM based on disks list.
 */
@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class AddVmFromScratchCommand<T extends AddVmParameters> extends AddVmCommand<T> {

    @Inject
    private StorageDomainDao storageDomainDao;

    public AddVmFromScratchCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
    }


    protected AddVmFromScratchCommand(Guid commandId) {
        super(commandId);
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected Map<String, Pair<String, String>> getSharedLocks() {
        return null;
    }

    @Override
    public Guid getStorageDomainId() {
        Guid storageDomainId = super.getStorageDomainId();
        if (Guid.Empty.equals(storageDomainId) || storageDomainId == null) {
            storageDomainId =
                    storageDomainDao.getAllForStoragePool(getStoragePoolId()).stream().filter(
                            a -> !a.getStorageDomainType().isIsoOrImportExportDomain()
                                    && (a.getStatus() == StorageDomainStatus.Active))
                            .map(StorageDomain::getId).findFirst().orElse(Guid.Empty);

            getParameters().setStorageDomainId(storageDomainId);
            setStorageDomainId(storageDomainId);
        }
        return storageDomainId;
    }

    @Override
    protected boolean checkTemplateImages() {
        return true;
    }

    @Override
    protected void addVmImages() {
        // no-op
    }

    @Override
    protected boolean validate() {
        if (getCluster() == null && Guid.Empty.equals(super.getStorageDomainId())) {
            return failValidation(EngineMessage.VM_CLUSTER_IS_NOT_VALID);
        }

        return super.validate();
    }

    @Override
    protected boolean isDisksVolumeFormatValid() {
        return true;
    }

    @Override
    protected List<DiskVmElement> getDiskVmElements() {
        return Collections.emptyList();
    }

    @Override
    protected ActionType getChildActionType() {
        return ActionType.AddImageFromScratch;
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
        addPermissionSubjectForAdminLevelProperties(permissionList);
        return permissionList;
    }
}
