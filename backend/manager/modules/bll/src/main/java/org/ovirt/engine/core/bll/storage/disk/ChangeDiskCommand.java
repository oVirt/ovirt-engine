package org.ovirt.engine.core.bll.storage.disk;

import java.io.File;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmOperationCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.domain.IsoDomainListSynchronizer;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public class ChangeDiskCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {

    @Inject
    private IsoDomainListSynchronizer isoDomainListSynchronizer;

    @Inject
    private VdsDao vdsDao;

    private String cdImagePath;

    public ChangeDiskCommand(T parameters, CommandContext cmdContext) {
        super(parameters, cmdContext);
        cdImagePath = getParameters().getCdImagePath();
    }

    public String getDiskName() {
        return new File(cdImagePath).getName();
    }

    @Override
    protected void setActionMessageParameters() {
        // An empty 'cdImagePath' means eject CD
        if (!StringUtils.isEmpty(cdImagePath)) {
            addValidationMessage(EngineMessage.VAR__ACTION__CHANGE_CD);
        } else {
            addValidationMessage(EngineMessage.VAR__ACTION__EJECT_CD);
        }
        addValidationMessage(EngineMessage.VAR__TYPE__VM);
    }

    @Override
    protected boolean validate() {
        if (shouldSkipCommandExecutionCached()) {
            return true;
        }

        if (getVm() == null) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_VM_NOT_FOUND);
        }

        if (!canRunActionOnNonManagedVm()) {
            return false;
        }

        if (!getVm().isRunningOrPaused()) {
            return failVmStatusIllegal();
        }

        if (StringUtils.isNotEmpty(cdImagePath) && !(StringUtils.endsWithIgnoreCase(cdImagePath, ValidationUtils.ISO_SUFFIX)
                || cdImagePath.matches(ValidationUtils.GUID))) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_INVALID_CDROM_DISK_FORMAT);
        }

        return true;
    }

    @Override
    protected boolean shouldSkipCommandExecution() {
        if (getVm() == null) {
            return false;
        }

        return StringUtils.equals(getVm().getCurrentCd(), getParameters().getCdImagePath());
    }

    @Override
    protected void perform() {
        String iface = getVmDeviceUtils().getCdInterface(getVm());
        int index = VmDeviceCommonUtils.getCdDeviceIndex(iface);
        cdImagePath = cdPathWindowsToLinux(
                getParameters().getCdImagePath(),
                getVm().getStoragePoolId(),
                getVm().getRunOnVds());
        VDS currentHost = vdsDao.get(getVm().getRunOnVds());
        ChangeDiskVDSCommandParameters params;

        if (currentHost.isCdChangePdiv()) {
            String diskPath = getParameters().getCdImagePath();
            // An empty 'diskPath' means eject CD.
            Guid diskGuid = StringUtils.isEmpty(diskPath) ? null : Guid.createGuidFromString(diskPath);
            Map<String, String> driveSpec = buildCdPdivFromPath(diskGuid);

            if (driveSpec != null) {
                driveSpec.put("device", "cdrom");
            }

            params = new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getId(), iface, index, driveSpec);
        } else {
            params = new ChangeDiskVDSCommandParameters(getVdsId(), getVm().getId(), iface, index, cdImagePath);
        }

        setActionReturnValue(runVdsCommand(VDSCommandType.ChangeDisk, params).getReturnValue());
        vmHandler.updateCurrentCd(getVm(), getParameters().getCdImagePath());
        setSucceeded(true);
    }

    @Override
    public AuditLogType getAuditLogTypeValue() {
        if (shouldSkipCommandExecutionCached()) {
            return "".equals(cdImagePath) ? AuditLogType.VM_DISK_ALREADY_EJECTED
                    : AuditLogType.VM_DISK_ALREADY_CHANGED;
        }

        if (!getSucceeded()) {
            return AuditLogType.USER_FAILED_CHANGE_DISK_VM;
        }

        return "".equals(cdImagePath) ? AuditLogType.USER_EJECT_VM_DISK
                : AuditLogType.USER_CHANGE_DISK_VM;
    }
}
