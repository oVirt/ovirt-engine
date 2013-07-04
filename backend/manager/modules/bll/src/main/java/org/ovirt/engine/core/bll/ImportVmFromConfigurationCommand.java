package org.ovirt.engine.core.bll;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachDettachVmDiskParameters;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.Disk;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;

@LockIdNameAttribute
@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromConfigurationCommand<T extends ImportVmParameters> extends ImportVmCommand<T> {

    private static final Log log = LogFactory.getLog(ImportVmFromConfigurationCommand.class);
    private Collection<Disk> vmDisksToAttach;

    protected ImportVmFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmFromConfigurationCommand(T parameters) {
        super(parameters);
        setCommandShouldBeLogged(false);
    }

    @Override
    protected void init(T parameters) {
        VM vmFromConfiguration = getParameters().getVm();
        if (vmFromConfiguration != null) {
            vmFromConfiguration.getStaticData().setVdsGroupId(getParameters().getVdsGroupId());
            vmDisksToAttach = vmFromConfiguration.getDiskMap().values();
            clearVmDisks(vmFromConfiguration);
            getParameters().setCopyCollapse(true);
            getParameters().setContainerId(vmFromConfiguration.getId());
        }

        setVdsGroupId(getParameters().getVdsGroupId());
        getParameters().setStoragePoolId(getVdsGroup().getStoragePoolId());
        super.init(parameters);
    }

    @Override
    public void executeCommand() {
        super.executeCommand();

        if (getSucceeded() && !vmDisksToAttach.isEmpty()) {
            AuditLogDirector.log(this, attemptToAttachDisksToImportedVm(vmDisksToAttach));
        }

        setActionReturnValue(getVm().getId());
    }

    private void clearVmDisks(VM vm) {
        vm.setDiskMap(Collections.<Guid, Disk> emptyMap());
        vm.getImages().clear();
        vm.getDiskList().clear();
    }

    private AuditLogType attemptToAttachDisksToImportedVm(Collection<Disk> disks){
        List<String> failedDisks = new LinkedList<>();
        for (Disk disk : disks) {
            AttachDettachVmDiskParameters params = new AttachDettachVmDiskParameters(getVm().getId(), disk.getId(), disk.getPlugged());
            VdcReturnValueBase returnVal = getBackend().runInternalAction(VdcActionType.AttachDiskToVm, params);
            if (!returnVal.getSucceeded()) {
                failedDisks.add(disk.getDiskAlias());
            }
        }

        if (!failedDisks.isEmpty()) {
            this.addCustomValue("DiskAliases", StringUtils.join(failedDisks, ","));
            return AuditLogType.VM_IMPORT_FROM_CONFIGURATION_ATTACH_DISKS_FAILED;
        }

        return AuditLogType.VM_IMPORT_FROM_CONFIGURATION_EXECUTED_SUCCESSFULLY;
    }

}
