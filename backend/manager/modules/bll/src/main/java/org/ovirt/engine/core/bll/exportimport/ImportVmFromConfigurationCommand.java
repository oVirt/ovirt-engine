package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.storage.ovfstore.DrMappingHelper;
import org.ovirt.engine.core.bll.storage.ovfstore.OvfHelper;
import org.ovirt.engine.core.bll.validator.ImportValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.VdcObjectType;
import org.ovirt.engine.core.common.action.ActionReturnValue;
import org.ovirt.engine.core.common.action.ActionType;
import org.ovirt.engine.core.common.action.AttachDetachVmDiskParameters;
import org.ovirt.engine.core.common.action.ImportVmFromConfParameters;
import org.ovirt.engine.core.common.action.LockProperties;
import org.ovirt.engine.core.common.action.LockProperties.Scope;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromConfigurationCommand<T extends ImportVmFromConfParameters> extends ImportVmCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ImportVmFromConfigurationCommand.class);
    private Collection<Disk> vmDisksToAttach;
    private OvfEntityData ovfEntityData;
    private VM vmFromConfiguration;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfHelper ovfHelper;

    @Inject
    private DrMappingHelper drMappingHelper;

    @Inject
    private ExternalVnicProfileMappingValidator externalVnicProfileMappingValidator;

    @Inject
    private ImportedNetworkInfoUpdater importedNetworkInfoUpdater;
    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;

    public ImportVmFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmFromConfigurationCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }

    @Override
    protected boolean validate() {
        if (!super.validate()) {
            return false;
        }
        if (isImagesAlreadyOnTarget()) {
            if (!validateExternalVnicProfileMapping()) {
                return false;
            }

            ImportValidator importValidator = getImportValidator();
            if (!validate(importValidator.validateUnregisteredEntity(ovfEntityData))) {
                return false;
            }
            if (!validate(importValidator.validateDiskNotAlreadyExistOnDB(getImages(),
                    getParameters().isAllowPartialImport(),
                    imageToDestinationDomainMap,
                    failedDisksToImportForAuditLog))) {
                return false;
            }
            if (!validate(importValidator.validateStorageExistForUnregisteredEntity(getImages(),
                    getParameters().isAllowPartialImport(),
                    imageToDestinationDomainMap,
                    failedDisksToImportForAuditLog))) {
                return false;
            }
            if (!validate(importValidator.validateStorageExistsForMemoryDisks(getVm().getSnapshots(),
                    getParameters().isAllowPartialImport(),
                    failedDisksToImportForAuditLog))) {
                return false;
            }
            setImagesWithStoragePoolId(getParameters().getStoragePoolId(), getVm().getImages());
        }
        return true;
    }

    private boolean validateExternalVnicProfileMapping() {
        final ValidationResult validationResult =
                externalVnicProfileMappingValidator.validateExternalVnicProfileMapping(
                        getParameters().getExternalVnicProfileMappings(),
                        getParameters().getClusterId());
        return validate(validationResult);
    }

    @Override
    protected boolean isExternalMacsToBeReported() {
        return !getParameters().isReassignBadMacs();
    }

    @Override
    protected LockProperties applyLockProperties(LockProperties lockProperties) {
        return lockProperties.withScope(Scope.Execution);
    }

    @Override
    protected void init() {
        VM vmFromConfiguration = getParameters().getVm();
        if (vmFromConfiguration != null) {
            vmFromConfiguration.getStaticData().setClusterId(getParameters().getClusterId());
            if (!isImagesAlreadyOnTarget()) {
                setDisksToBeAttached(vmFromConfiguration);
            }
            getParameters().setContainerId(vmFromConfiguration.getId());
        } else {
            initUnregisteredVM();
        }

        if (Guid.Empty.equals(getParameters().getVmId()) && getParameters().getVm() != null) {
            getParameters().setVmId(getParameters().getVm().getId());
        }
        setClusterId(getParameters().getClusterId());
        if (getCluster() != null) {
            getParameters().setStoragePoolId(getCluster().getStoragePoolId());
        }
        super.init();
    }

    private void initUnregisteredVM() {
        List<OvfEntityData> ovfEntityDataList =
                unregisteredOVFDataDao.getByEntityIdAndStorageDomain(getParameters().getContainerId(),
                        getParameters().getStorageDomainId());
        if (!ovfEntityDataList.isEmpty()) {
            try {
                // We should get only one entity, since we fetched the entity with a specific Storage Domain
                ovfEntityData = ovfEntityDataList.get(0);
                FullEntityOvfData fullEntityOvfData = ovfHelper.readVmFromOvf(ovfEntityData.getOvfData());
                vmFromConfiguration = fullEntityOvfData.getVm();
                if (Guid.isNullOrEmpty(getParameters().getClusterId())) {
                    Cluster cluster =
                            drMappingHelper.getMappedCluster(fullEntityOvfData.getClusterName(),
                                    vmFromConfiguration.getId(),
                                    getParameters().getClusterMap());
                    if (cluster != null) {
                        getParameters().setClusterId(cluster.getId());
                    }
                }
                vmFromConfiguration.setClusterId(getParameters().getClusterId());
                mapVnicProfiles(vmFromConfiguration.getInterfaces());
                getParameters().setVm(vmFromConfiguration);
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setAffinityGroups(fullEntityOvfData.getAffinityGroups());
                getParameters().setAffinityLabels(fullEntityOvfData.getAffinityLabels());
                getParameters().setDbUsers(fullEntityOvfData.getDbUsers());
                getParameters().setUserToRoles(fullEntityOvfData.getUserToRoles());

                // For quota, update disks when required
                if (getParameters().getDiskMap() != null) {
                    vmFromConfiguration.setDiskMap(getParameters().getDiskMap());
                    vmFromConfiguration.setImages(getDiskImageListFromDiskMap(getParameters().getDiskMap()));
                }
                // Note: The VM's OVF does not preserve the username and password for the LUN's connection.
                // Therefore to achieve a simple VM registration the iSCSI storage server should not use
                // credentials, although if the user will use the mapping attribute, one can set the credentials through
                // there.
                drMappingHelper.mapExternalLunDisks(DisksFilter.filterLunDisks(vmFromConfiguration.getDiskMap().values()),
                        getParameters().getExternalLunMap());
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
            }
        }
    }

    private void mapVnicProfiles(List<VmNetworkInterface> vnics) {
        vnics.forEach(vnic ->
                importedNetworkInfoUpdater.updateNetworkInfo(vnic, getParameters().getExternalVnicProfileMappings()));
    }

    protected void mapDbUsers() {
        drMappingHelper.mapDbUsers(getParameters().getDomainMap(),
                getParameters().getDbUsers(),
                getParameters().getUserToRoles(),
                getVmId(),
                VdcObjectType.VM,
                getParameters().getRoleMap());
    }

    @Override
    public void addVmToAffinityGroups() {
        drMappingHelper.addVmToAffinityGroups(getParameters().getClusterId(),
                getParameters().getVmId(),
                getParameters().getAffinityGroupMap(),
                getParameters().getAffinityGroups());
    }

    @Override
    public void addVmToAffinityLabels() {
        drMappingHelper.addVmToAffinityLabels(getParameters().getAffinityLabelMap(),
                getParameters().getVm(),
                getParameters().getAffinityLabels());
    }

    private static ArrayList<DiskImage> getDiskImageListFromDiskMap(Map<Guid, Disk> diskMap) {
        return diskMap.values().stream().map(disk -> (DiskImage) disk).collect(Collectors.toCollection(ArrayList::new));
    }

    private void setDisksToBeAttached(VM vmFromConfiguration) {
        vmDisksToAttach = vmFromConfiguration.getDiskMap().values();
        clearVmDisks(vmFromConfiguration);
        getParameters().setCopyCollapse(true);
    }

    @Override
    public void executeVmCommand() {
        addAuditLogForPartialVMs();
        super.executeVmCommand();
        if (getSucceeded()) {
            if (isImagesAlreadyOnTarget()) {
                getImages().stream().forEach(diskImage -> {
                    initQcowVersionForDisks(diskImage.getId());
                });
                unregisteredOVFDataDao.removeEntity(ovfEntityData.getEntityId(), null);
                unregisteredDisksDao.removeUnregisteredDiskRelatedToVM(ovfEntityData.getEntityId(), null);
                auditLogDirector.log(this, AuditLogType.VM_IMPORT_FROM_CONFIGURATION_EXECUTED_SUCCESSFULLY);
            } else if (!vmDisksToAttach.isEmpty()) {
                auditLogDirector.log(this, attemptToAttachDisksToImportedVm(vmDisksToAttach));
            }
        }
        setActionReturnValue(getVm().getId());
    }

    private void addAuditLogForPartialVMs() {
        if (getParameters().isAllowPartialImport() && !failedDisksToImportForAuditLog.isEmpty()) {
            addCustomValue("DiskAliases", StringUtils.join(failedDisksToImportForAuditLog.values(), ", "));
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_PARTIAL_VM_DISKS_NOT_EXISTS);
        }
    }

    private static void clearVmDisks(VM vm) {
        vm.setDiskMap(Collections.emptyMap());
        vm.getImages().clear();
        vm.getDiskList().clear();
    }

    private AuditLogType attemptToAttachDisksToImportedVm(Collection<Disk> disks) {
        List<String> failedDisks = new LinkedList<>();
        for (Disk disk : disks) {
            DiskVmElement dve = disk.getDiskVmElements().iterator().next();
            AttachDetachVmDiskParameters params = new AttachDetachVmDiskParameters(
                    dve, dve.isPlugged());
            ActionReturnValue
                    returnVal = runInternalAction(ActionType.AttachDiskToVm, params, cloneContextAndDetachFromParent());
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
    protected boolean validateAndSetVmFromExportDomain() {
        // We have the VM configuration so there is no need to get it from the export domain.
        return true;
    }

    @Override
    protected Guid getSourceDomainId(DiskImage image) {
        return image.getStorageIds().get(0);
    }
}
