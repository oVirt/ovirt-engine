package org.ovirt.engine.core.bll.exportimport;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.NonTransactiveCommandAttribute;
import org.ovirt.engine.core.bll.context.CommandContext;
import org.ovirt.engine.core.bll.scheduling.arem.AffinityRulesUtils;
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
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.Label;
import org.ovirt.engine.core.common.businessentities.OriginType;
import org.ovirt.engine.core.common.businessentities.OvfEntityData;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmStatic;
import org.ovirt.engine.core.common.businessentities.aaa.DbUser;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.FullEntityOvfData;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.scheduling.AffinityGroup;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.LabelDao;
import org.ovirt.engine.core.dao.RoleDao;
import org.ovirt.engine.core.dao.UnregisteredDisksDao;
import org.ovirt.engine.core.dao.UnregisteredOVFDataDao;
import org.ovirt.engine.core.dao.scheduling.AffinityGroupDao;
import org.ovirt.engine.core.utils.ovf.OvfReaderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NonTransactiveCommandAttribute(forceCompensation = true)
public class ImportVmFromConfigurationCommand<T extends ImportVmFromConfParameters> extends ImportVmCommand<T> {

    private static final Logger log = LoggerFactory.getLogger(ImportVmFromConfigurationCommand.class);
    private Collection<Disk> vmDisksToAttach;
    private OvfEntityData ovfEntityData;
    private boolean ovfCanBeParsed = true;

    private List<String> missingAffinityGroups = new ArrayList<>();
    private List<String> missingAffinityLabels = new ArrayList<>();
    private List<String> missingUsers = new ArrayList<>();
    private List<String> missingRoles = new ArrayList<>();
    private List<String> missingVnicMappings = new ArrayList<>();

    private List<AffinityGroup> cachedAffinityGroups = new ArrayList<>();
    private List<Label> cachedAffinityLabels = new ArrayList<>();

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private OvfHelper ovfHelper;

    @Inject
    private DrMappingHelper drMappingHelper;

    @Inject
    private UnregisteredOVFDataDao unregisteredOVFDataDao;
    @Inject
    private UnregisteredDisksDao unregisteredDisksDao;
    @Inject
    private AffinityGroupDao affinityGroupDao;
    @Inject
    private LabelDao labelDao;
    @Inject
    private RoleDao roleDao;

    public ImportVmFromConfigurationCommand(Guid commandId) {
        super(commandId);
    }

    public ImportVmFromConfigurationCommand(T parameters, CommandContext commandContext) {
        super(parameters, commandContext);
        setCommandShouldBeLogged(false);
    }

    @Override
    protected boolean validate() {
        if (!ovfCanBeParsed) {
            return failValidation(EngineMessage.ACTION_TYPE_FAILED_OVF_CONFIGURATION_NOT_SUPPORTED);
        }

        // We first must filter out all the invalid disks from imageToDestinationDomainMap in case we import using
        // allow_partial, so that when a VM will be imported the disks which do not exist on the storage domain will
        // be ignored.
        if (!isValidDisks()) {
            return false;
        }

        // We call super validate only after the invalid disks has been removed from imageToDestinationDomainMap.
        if (!super.validate()) {
            return false;
        }

        // Validate all the rest of the properties including affinity groups/labels/vnic_profile/users and roles
        if (!validateEntityPropertiesWhenImagesOnTarget()) {
            return false;
        }
        return true;
    }
    private boolean validateEntityPropertiesWhenImagesOnTarget() {
        ImportValidator importValidator = getImportValidator();
        if (isImagesAlreadyOnTarget()) {
            if (!validate(importValidator.validateUnregisteredEntity(ovfEntityData))) {
                return false;
            }
            removeInvalidAffinityGroups(importValidator);
            removeInvalidAffinityLabels(importValidator);
            removeInavlidUsers(importValidator);
            removeInavlidRoles(importValidator);

            setImagesWithStoragePoolId(getParameters().getStoragePoolId(), getVm().getImages());
        }
        return true;
    }

    private boolean isValidDisks() {
        ImportValidator importValidator = getImportValidator();
        if (isImagesAlreadyOnTarget()) {
            if (!validate(importValidator.validateDiskNotAlreadyExistOnDB(getImages(),
                    getParameters().isAllowPartialImport(),
                    imageToDestinationDomainMap,
                    failedDisksToImportForAuditLog))) {
                return false;
            }
            if (getCluster() != null
                    && !validate(importValidator.validateStorageExistForUnregisteredEntity(getImages(),
                            getParameters().isAllowPartialImport(),
                            imageToDestinationDomainMap,
                            failedDisksToImportForAuditLog))) {
                return false;
            }
            if (getVm() != null
                    && !validate(importValidator.validateStorageExistsForMemoryDisks(getVm().getSnapshots(),
                    getParameters().isAllowPartialImport(),
                    failedDisksToImportForAuditLog))) {
                return false;
            }
        }
        return true;
    }

    private void updateVnicsFromMapping() {
        if (isImagesAlreadyOnTarget()) {
            // mapping exists only in non-OVA import
            missingVnicMappings = drMappingHelper.updateVnicsFromMappings(getParameters().getClusterId(), getParameters().getVm().getName(),
                    getParameters().getVm().getInterfaces(), getParameters().getExternalVnicProfileMappings());
        }
    }

    @Override
    protected boolean isExternalMacsToBeReported() {
        return !getParameters().isReassignBadMacs();
    }

    @Override
    protected void init() {
        VM vmFromConfiguration = getParameters().getVm();
        if (vmFromConfiguration != null) {
            vmFromConfiguration.setClusterId(getParameters().getClusterId());
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
                VM vmFromConfiguration = fullEntityOvfData.getVm();
                if (Guid.isNullOrEmpty(getParameters().getClusterId())) {
                    Cluster cluster =
                            drMappingHelper.getMappedCluster(fullEntityOvfData.getClusterName(),
                                    vmFromConfiguration.getId(),
                                    getParameters().getClusterMap());
                    if (cluster != null) {
                        getParameters().setClusterId(cluster.getId());
                    }
                }

                setClusterId(getParameters().getClusterId());
                if (getCluster() != null && getCluster().getBiosType() != null) {
                    vmFromConfiguration.setClusterBiosType(getCluster().getBiosType());
                }

                vmFromConfiguration.setClusterId(getParameters().getClusterId());
                getParameters().setVm(vmFromConfiguration);
                if (getParameters().getName() != null) {
                    getParameters().getVm().setName(getParameters().getName());
                }
                getParameters().setDestDomainId(ovfEntityData.getStorageDomainId());
                getParameters().setSourceDomainId(ovfEntityData.getStorageDomainId());
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
                mapEntities(fullEntityOvfData);
            } catch (OvfReaderException e) {
                log.error("Failed to parse a given ovf configuration: {}:\n{}",
                        e.getMessage(),
                        ovfEntityData.getOvfData());
                log.debug("Exception", e);
                ovfEntityData = null;
                ovfCanBeParsed = false;
            }
        }
    }

    private void mapEntities(FullEntityOvfData fullEntityOvfData) {
        if (getParameters().getAffinityGroupMap() != null) {
            getParameters().setAffinityGroups(drMappingHelper.mapAffinityGroups(getParameters().getAffinityGroupMap(),
                    fullEntityOvfData.getAffinityGroups(),
                    getParameters().getVm().getName()));
        } else {
            getParameters().setAffinityGroups(fullEntityOvfData.getAffinityGroups());
        }
        if (getParameters().getAffinityLabelMap() != null) {
            getParameters().setAffinityLabels(drMappingHelper.mapAffinityLabels(getParameters().getAffinityLabelMap(),
                    getParameters().getVm().getName(),
                    fullEntityOvfData.getAffinityLabels()));
        } else {
            getParameters().setAffinityLabels(fullEntityOvfData.getAffinityLabels());
        }
        if (getParameters().getDomainMap() != null) {
            getParameters().setDbUsers(drMappingHelper.mapDbUsers(fullEntityOvfData.getDbUsers(),
                    getParameters().getDomainMap()));
        } else {
            getParameters().setDbUsers(fullEntityOvfData.getDbUsers());
        }
        if (getParameters().getRoleMap() != null) {
            getParameters().setUserToRoles(drMappingHelper.mapRoles(getParameters().getRoleMap(),
                    getParameters().getUserToRoles()));
        } else {
            getParameters().setUserToRoles(fullEntityOvfData.getUserToRoles());
        }
    }

    @Override
    protected void addPermissionsToDB() {
        if (isImagesAlreadyOnTarget()) {
            drMappingHelper.addPermissions(getParameters().getDbUsers(),
                    getParameters().getUserToRoles(),
                    getVmId(),
                    VdcObjectType.VM,
                    getParameters().getRoleMap());
        }
    }

    @Override
    public void addVmToAffinityGroups() {
        cachedAffinityGroups.forEach(affinityGroup -> affinityGroupDao.update(affinityGroup));
    }

    @Override
    public void addVmToAffinityLabels() {
        cachedAffinityLabels.forEach(affinityLabel -> {
            affinityLabel.addVm(getParameters().getVm());
            labelDao.update(affinityLabel);
        });
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
        updateVnicsFromMapping();
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
            updateBiosType();
        }
        setActionReturnValue(getVm().getId());
    }

    private void addAuditLogForPartialVMs() {
        StringBuilder missingEntities = new StringBuilder();
        if (getParameters().isAllowPartialImport() && !failedDisksToImportForAuditLog.isEmpty()) {
            missingEntities.append("Disks: ");
            missingEntities.append(StringUtils.join(failedDisksToImportForAuditLog.values(), ", ") + " ");
        }
        if (!missingAffinityGroups.isEmpty()) {
            missingEntities.append("Affinity groups: ");
            missingEntities.append(StringUtils.join(missingAffinityGroups, ", ") + " ");
        }
        if (!missingAffinityLabels.isEmpty()) {
            missingEntities.append("Affinity labels: ");
            missingEntities.append(StringUtils.join(missingAffinityLabels, ", ") + " ");
        }
        if (!missingUsers.isEmpty()) {
            missingEntities.append("Users: ");
            missingEntities.append(StringUtils.join(missingUsers, ", ") + " ");
        }
        if (!missingRoles.isEmpty()) {
            missingEntities.append("Roles: ");
            missingEntities.append(StringUtils.join(missingRoles, ", ") + " ");
        }
        if (!missingVnicMappings.isEmpty()) {
            missingEntities.append("Vnic Mappings: ");
            missingEntities.append(StringUtils.join(missingVnicMappings, ", ") + " ");
        }

        if (missingEntities.length() > 0) {
            addCustomValue("MissingEntities", missingEntities.toString());
            auditLogDirector.log(this, AuditLogType.IMPORTEXPORT_PARTIAL_VM_MISSING_ENTITIES);
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

    private void removeInvalidAffinityGroups(ImportValidator importValidator) {
        if (getParameters().getAffinityGroups() == null || getParameters().getAffinityGroups().isEmpty()) {
            return;
        }

        List<String> affinityGroupsToAdd = getParameters()
                .getAffinityGroups()
                .stream()
                .map(AffinityGroup::getName)
                .collect(Collectors.toList());
        log.info("Checking for invalid affinity groups");
        missingAffinityGroups = importValidator.findMissingEntities(affinityGroupsToAdd,
                val -> affinityGroupDao.getByName(val));
        affinityGroupsToAdd.removeAll(missingAffinityGroups);
        affinityGroupsToAdd.forEach(affinityGroup -> cachedAffinityGroups.add(affinityGroupDao.getByName(affinityGroup)));

        // Add the VM to the affinity group so it can be checked for conflicts
        cachedAffinityGroups.forEach(affinityGroup -> {
            Set<Guid> vmIds = new HashSet<>(affinityGroup.getVmIds());
            vmIds.add(getVmId());
            affinityGroup.setVmIds(new ArrayList<>(vmIds));
        });

        // Remove all conflicted affinity groups so the VM won't be added to them
        cachedAffinityGroups.removeAll(getConflictedAffinityGroups(cachedAffinityGroups));

        List<AffinityGroup> faultyAffinityGroups =
                importValidator.findFaultyAffinityGroups(cachedAffinityGroups, getClusterId());
        cachedAffinityGroups.removeAll(faultyAffinityGroups);
    }

    private List<AffinityGroup> getConflictedAffinityGroups(List<AffinityGroup> affinityGroups) {
        List<AffinityRulesUtils.AffinityGroupConflicts> conflicts =
                AffinityRulesUtils.checkForAffinityGroupHostsConflict(affinityGroups);

        return conflicts
                .stream()
                .filter(conflict -> !affinityGroupConflictFilter(conflict))
                .flatMap(conflict -> conflict.getAffinityGroups().stream())
                .collect(Collectors.toList());
    }

    private boolean affinityGroupConflictFilter(AffinityRulesUtils.AffinityGroupConflicts conflict) {
        boolean canBeSaved = conflict.getType().canBeSaved();
        if (canBeSaved) {
            log.warn("Affinity groups '{}' have a conflict, but can be added",
                    conflict
                            .getAffinityGroups()
                            .stream()
                            .map(AffinityGroup::getName)
                            .collect(Collectors.joining(", ")));
        } else {
            if (conflict.isVmToVmAffinity()) {
                log.warn(conflict.getType().getMessage(),
                        conflict.getVms()
                                .stream()
                                .map(id -> id.toString())
                                .collect(Collectors.joining(",")),
                        AffinityRulesUtils.getAffinityGroupsNames(conflict.getAffinityGroups()),
                        conflict.getNegativeVms()
                                .stream()
                                .map(id -> id.toString())
                                .collect(Collectors.joining(",")));
            } else {
                log.warn(conflict.getType().getMessage(),
                        AffinityRulesUtils.getAffinityGroupsNames(conflict.getAffinityGroups()),
                        conflict.getHosts()
                                .stream()
                                .map(id -> id.toString())
                                .collect(Collectors.joining(",")),
                        conflict.getVms()
                                .stream()
                                .map(id -> id.toString())
                                .collect(Collectors.joining(",")));
            }
        }

        return canBeSaved;
    }

    private void removeInvalidAffinityLabels(ImportValidator importValidator) {
        if (getParameters().getAffinityLabels() == null || getParameters().getAffinityLabels().isEmpty()) {
            return;
        }
        List<String> candidateAffinityLabels = getParameters().getAffinityLabels()
                .stream()
                .map(Label::getName)
                .collect(Collectors.toList());
        log.info("Checking for missing affinity labels");
        missingAffinityLabels = importValidator.findMissingEntities(candidateAffinityLabels,
                val -> labelDao.getByName(val));
        candidateAffinityLabels.removeAll(missingAffinityLabels);
        candidateAffinityLabels.forEach(label -> cachedAffinityLabels.add(labelDao.getByName(label)));
    }

    private void removeInavlidUsers(ImportValidator importValidator) {
        if (getParameters().getDbUsers() == null || getParameters().getDbUsers().isEmpty()) {
            return;
        }

        log.info("Checking for missing users");
        List<DbUser> dbMissingUsers = importValidator.findMissingUsers(getParameters().getDbUsers());
        missingUsers = dbMissingUsers
            .stream()
            .map(dbUser -> String.format("%s@%s", dbUser.getLoginName(), dbUser.getDomain()))
            .collect(Collectors.toList());
        getParameters().getDbUsers().removeAll(dbMissingUsers);
    }

    private void removeInavlidRoles(ImportValidator importValidator) {
        if (MapUtils.isEmpty(getParameters().getUserToRoles())) {
            return;
        }

        log.info("Checking for missing roles");
        Set<String> candidateRoles = getParameters().getUserToRoles().entrySet()
                .stream()
                .flatMap(userToRoles -> userToRoles.getValue().stream())
                .collect(Collectors.toSet());
        missingRoles = importValidator.findMissingEntities(candidateRoles, val -> roleDao.getByName(val));
        getParameters().getUserToRoles().forEach((k, v) -> v.removeAll(missingRoles));
    }

    private void updateBiosType() {
        if (getVm().getOrigin() != OriginType.VMWARE && getVm().getOrigin() != OriginType.XEN) {
            return;
        }

        VmStatic oldVm = getVm().getStaticData();
        VmStatic newVm = new VmStatic(oldVm);

        vmHandler.updateToQ35(oldVm,
                newVm,
                getCluster(),
                null);
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
