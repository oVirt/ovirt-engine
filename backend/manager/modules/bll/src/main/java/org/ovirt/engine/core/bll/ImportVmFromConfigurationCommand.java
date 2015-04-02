package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ImportVmParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.action.VdcReturnValueBase;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.Function;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromConfigurationCommand<T extends ImportVmParameters> extends ImportVmCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ImportVmFromConfigurationCommand.class);
    private Collection<Disk> vmDisksToAttach;
    private OvfEntityData ovfEntityData;
    VM vmFromConfiguration;

    protected ImportVmFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmFromConfigurationCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }

    public ImportVmFromConfigurationCommand(T parameters) {
        this(parameters, null);
    }

    @Override
    protected boolean canDoAction() {
        if (isImagesAlreadyOnTarget()) {
            ImportValidator importValidator = getImportValidator();
            if (!validate(importValidator.validateUnregisteredEntity(vmFromConfiguration, ovfEntityData, getImages()))) {
                return false;
            }
            setImagesWithStoragePoolId(getParameters().getStoragePoolId(), getVm().getImages());
        }
        return super.canDoAction();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    @PostConstruct
    protected void postConstruct() {
        VM vmFromConfiguration = getParameters().getVm();
        if (vmFromConfiguration != null) {
            vmFromConfiguration.getStaticData().setVdsGroupId(getParameters().getVdsGroupId());
            if (!isImagesAlreadyOnTarget()) {
                setDisksToBeAttached(vmFromConfiguration);
            }
            getParameters().setContainerId(vmFromConfiguration.getId());
        } else {
            initUnregisteredVM();
        }

        setVdsGroupId(getParameters().getVdsGroupId());
        getParameters().setStoragePoolId(getVdsGroup().getStoragePoolId());
        super.postConstruct();
    }

    private void initUnregisteredVM() {
        OvfHelper ovfHelper = new OvfHelper();
        List<OvfEntityData> ovfEntityDataList =
                getUnregisteredOVFDataDao().getByEntityIdAndStorageDomain(getParameters().getContainerId(),
                        getParameters().getStorageDomainId());
        if (!ovfEntityDataList.isEmpty()) {
            try {
                // We should get only one entity, since we fetched the entity with a specific Storage Domain
                ovfEntityData = ovfEntityDataList.get(0);
                vmFromConfiguration = ovfHelper.readVmFromOvf(ovfEntityData.getOvfData());
                vmFromConfiguration.setVdsGroupId(getParameters().getVdsGroupId());
                getParameters().setVm(vmFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());

                // For quota, update disks when required
                if (getParameters().getDiskMap() != null) {
                    vmFromConfiguration.setDiskMap(getParameters().getDiskMap());
                    vmFromConfiguration.setImages(getDiskImageListFromDiskMap(getParameters().getDiskMap()));
                }
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
            }
        }
    }

    private static ArrayList<DiskImage> getDiskImageListFromDiskMap(Map<Guid, Disk> diskMap) {
        return new ArrayList<>(LinqUtils.transformToList(diskMap.values(), new Function<Disk, DiskImage>() {
            @Override
            public DiskImage eval(Disk disk) {
                return (DiskImage) disk;
            }
        }));
    }

    private void setDisksToBeAttached(VM vmFromConfiguration) {
        vmDisksToAttach = vmFromConfiguration.getDiskMap().values();
        clearVmDisks(vmFromConfiguration);
        getParameters().setCopyCollapse(true);
    }

    @Override
    public void executeCommand() {
        super.executeCommand();
        if (getSucceeded()) {
            if (isImagesAlreadyOnTarget()) {
                getUnregisteredOVFDataDao().removeEntity(ovfEntityData.getEntityId(), null);
                auditLogDirector.log(this, AuditLogType.VM_IMPORT_FROM_CONFIGURATION_EXECUTED_SUCCESSFULLY);
            } else if (!vmDisksToAttach.isEmpty()) {
                auditLogDirector.log(this, attemptToAttachDisksToImportedVm(vmDisksToAttach));
            }
        }
        setActionReturnValue(getVm().getId());
    }

    private static void clearVmDisks(VM vm) {
        vm.setDiskMap(Collections.<Guid, Disk> emptyMap());
        vm.getImages().clear();
        vm.getDiskList().clear();
    }

    private AuditLogType attemptToAttachDisksToImportedVm(Collection<Disk> disks) {
        List<String> failedDisks = new LinkedList<>();
        for (Disk disk : disks) {
            AttachDetachVmDiskParameters params = new AttachDetachVmDiskParameters(getVm().getId(),
                    disk.getId(), disk.getPlugged(), disk.getReadOnly());
            VdcReturnValueBase returnVal = runInternalAction(VdcActionType.AttachDiskToVm, params, cloneContextAndDetachFromParent());
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

    @Override
    protected Guid getSourceDomainId(DiskImage image) {
        return image.getStorageIds().get(0);
    }
}
