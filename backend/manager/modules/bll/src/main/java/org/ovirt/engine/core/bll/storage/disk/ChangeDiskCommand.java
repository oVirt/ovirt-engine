package org.ovirt.engine.core.bll.storage.disk;

import java.io.File;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.VmOperationCommandBase;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.ChangeDiskCommandParameters;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.ValidationUtils;
import org.ovirt.engine.core.common.utils.VmDeviceCommonUtils;
import org.ovirt.engine.core.common.vdscommands.ChangeDiskVDSCommandParameters;
import org.ovirt.engine.core.common.vdscommands.VDSCommandType;
import org.ovirt.engine.core.compat.Guid;

public class ChangeDiskCommand<T extends ChangeDiskCommandParameters> extends VmOperationCommandBase<T> {

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
        ChangeDiskVDSCommandParameters params;

        if (useCdChangeWithPdiv()) {
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

    /**
     * Determines, if the new way for changing CD, which uses image PDIV instead of path, can be used.
     *
     * To use new change CD, following conditions have to be met:
     *  - host doing CD change has to support it,
     *  - we are able to get image ID from provided parameters and determine image PDIV.
     *
     * Specifying CD image as a path instead of its UUID is the case of images stored on ISO domains.
     * In such case fall back to old CD change. This works, as ISO domain can be only on file-based SD,
     * where old CD change works without problems.
     */
    private boolean useCdChangeWithPdiv() {
        String diskPath = getParameters().getCdImagePath();
        boolean supportsPdiv = StringUtils.isEmpty(diskPath) || diskPath.matches(ValidationUtils.GUID);
        return getVds().isCdChangePdiv() && supportsPdiv;
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
