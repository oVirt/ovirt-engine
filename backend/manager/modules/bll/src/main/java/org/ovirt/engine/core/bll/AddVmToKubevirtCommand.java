package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.kubevirt.EntityMapper;
import org.ovirt.engine.core.bll.kubevirt.KubevirtMonitoring;
import org.ovirt.engine.core.bll.kubevirt.PVCDisk;
import org.ovirt.engine.core.bll.utils.PermissionSubject;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.AddVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskImageDao;

@DisableInPrepareMode
@NonTransactiveCommandAttribute
public class AddVmToKubevirtCommand<T extends AddVmParameters> extends VmManagementCommandBase<T> {

    @Inject
    private KubevirtMonitoring kubevirtMonitoring;

    @Inject
    private DiskImageDao diskImageDao;

    protected AddVmToKubevirtCommand(Guid commandId) {
        super(commandId);
    }

    public AddVmToKubevirtCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setVmId(parameters.getVmId().equals(Guid.Empty) ? Guid.newGuid() : parameters.getVmId());
        parameters.setVmId(getVmId());
        setVmName(parameters.getVm().getName());
        setVmTemplateId(parameters.getVmStaticData().getVmtGuid());
        setStorageDomainId(getParameters().getStorageDomainId());
    }

    @Override
    protected boolean validate() {
        if (getCluster() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_CLUSTER_CAN_NOT_BE_EMPTY);
        }

        if (getVmTemplate() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_TEMPLATE_DOES_NOT_EXIST);
        }

        if (getParameters().getDisksToAttach().isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_SPECIFIED);
        }

        if (getDiskImages(getRootDisk()).isEmpty()) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_NO_DISKS_IMAGE);
        }

        if (!setAndValidateCpuProfile()) {
            return false;
        }
        return true;
    }

    @Override
    protected void executeVmCommand() {
        VmStatic vm = getParameters().getVmStaticData();
        List<DiskImage> diskImages = getDiskImages(getRootDisk());

        PVCDisk rootDisk = new PVCDisk(diskImages.get(0));
        kubevirtMonitoring.create(vm.getClusterId(), EntityMapper.toKubevirtVm(getVmTemplate(), vm, rootDisk));

        setSucceeded(true);
    }

    @Override
    protected void setActionMessageParameters() {
        addValidationMessage(EngineMessage.VAR__ACTION__ADD);
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        return getSucceeded() ? AuditLogType.USER_ADD_VM_TO_KUBEVIRT_REQUEST :
            AuditLogType.USER_ADD_VM_TO_KUBEVIRT_REQUEST_FAILED;
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.None);
    }

    @Override
    public List<PermissionSubject> getPermissionCheckSubjects() {
        List<PermissionSubject> permissionList = new ArrayList<>();
        permissionList.add(new PermissionSubject(getClusterId(),
                VdcObjectType.Cluster,
                getActionType().getActionGroup()));
        permissionList.add(new PermissionSubject(getVmTemplateId(),
                VdcObjectType.VmTemplate,
                getActionType().getActionGroup()));
        return permissionList;
    }

    @Override
    public Map<String, String> getJobMessageProperties() {
        if (jobProperties == null) {
            jobProperties = super.getJobMessageProperties();
            jobProperties.put(VdcObjectType.VM.name().toLowerCase(),
                    (getVmName() == null) ? "" : getVmName());
        }
        return jobProperties;
    }

    private DiskVmElement getRootDisk() {
        return getParameters().getDisksToAttach()
                .stream()
                .filter(DiskVmElement::isBoot)
                .findFirst()
                .orElse(getParameters().getDisksToAttach().get(0));
    }

    private List<DiskImage> getDiskImages(DiskVmElement rootDisk) {
        return diskImageDao.getAllSnapshotsForImageGroup(rootDisk.getDiskId());
    }
}
