package org.ovirt.engine.core.bll.storage.utils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.storage.disk.DiskHandler;
import org.ovirt.engine.core.bll.storage.disk.image.DisksFilter;
import org.ovirt.engine.core.bll.validator.storage.MultipleDiskVmElementValidator;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.DiskImageDao;
import org.ovirt.engine.core.dao.DiskVmElementDao;
import org.ovirt.engine.core.dao.StorageDomainDao;
import org.ovirt.engine.core.di.Injector;

@Singleton
public class BlockStorageDiscardFunctionalityHelper {

    @Inject
    private DiskDao diskDao;

    @Inject
    private DiskImageDao diskImageDao;

    @Inject
    private DiskVmElementDao diskVmElementDao;

    @Inject
    private StorageDomainDao storageDomainDao;

    @Inject
    private AuditLogDirector auditLogDirector;

    @Inject
    private DiskHandler diskHandler;

    public ValidationResult isExistingDiscardFunctionalityPreserved(Collection<LUNs> lunsToAdd,
            StorageDomain storageDomain) {
        if (!isExistingPassDiscardFunctionalityPreserved(lunsToAdd, storageDomain)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_LUN_BREAKS_STORAGE_DOMAIN_PASS_DISCARD_SUPPORT,
                    getStorageDomainNameVariableReplacement(storageDomain));
        }
        if (!isExistingDiscardAfterDeleteFunctionalityPreserved(lunsToAdd, storageDomain)) {
            return new ValidationResult(EngineMessage.ACTION_TYPE_FAILED_LUN_BREAKS_STORAGE_DOMAIN_DISCARD_AFTER_DELETE,
                    getStorageDomainNameVariableReplacement(storageDomain));
        }
        return ValidationResult.VALID;
    }

    private String getStorageDomainNameVariableReplacement(StorageDomain storageDomain) {
        return String.format("$storageDomainName %1$s", storageDomain.getName());
    }

    protected boolean isExistingPassDiscardFunctionalityPreserved(Collection<LUNs> lunsToAdd,
            StorageDomain storageDomain) {
        boolean sdSupportsDiscard = Boolean.TRUE.equals(storageDomain.getSupportsDiscard());
        if (!sdSupportsDiscard) {
            // The storage domain already doesn't support discard so there's no
            // need to check if the new luns break the discard functionality.
            return true;
        }

        List<DiskImage> sdDisks = diskImageDao.getAllForStorageDomain(storageDomain.getId());
        if (sdDisks.isEmpty()) {
            // There are no disks on the storage domain, so even if
            // it supports discard, no functionality will be damaged,
            return true;
        }
        Collection<Guid> sdDisksIds = sdDisks.stream().map(DiskImage::getId).collect(Collectors.toList());
        Collection<DiskVmElement> diskVmElements = null;

        if (!allLunsSupportDiscard(lunsToAdd)) {
            // The sd supports discard and there is at least one lun that breaks its support for discard.
            diskVmElements = diskVmElementDao.getAllDiskVmElementsByDisksIds(sdDisksIds);
            if (vmDiskWithPassDiscardExists(diskVmElements)) {
                // There is at least one disk that is configured to pass discard
                // and at least one lun that breaks the support for discard.
                return false;
            }
        }
        boolean sdDiscardZeroesData = Boolean.TRUE.equals(storageDomain.getSupportsDiscardZeroesData());
        if (sdDiscardZeroesData && !allLunsHaveDiscardZeroesTheDataSupport(lunsToAdd)) {
            // The sd supports the property that discard zeroes the data and
            // there is at least one lun that breaks this property.
            if (diskVmElements == null) {
                diskVmElements = diskVmElementDao.getAllDiskVmElementsByDisksIds(sdDisksIds);
            }
            if (vmDiskWithPassDiscardAndWadExists(sdDisks, diskVmElements)) {
                // There is at least one disk that requires the storage domain's support for the
                // property that discard zeroes the data and at least one lun that breaks this property.
                return false;
            }
        }
        return true;
    }

    protected boolean isExistingDiscardAfterDeleteFunctionalityPreserved(Collection<LUNs> lunsToAdd,
            StorageDomain storageDomain) {
        if (!storageDomain.isDiscardAfterDelete()) {
            // The storage domain's discard after delete property is already false so there's
            // no need to check if the new luns break the discard after delete functionality.
            return true;
        }
        if (!allLunsSupportDiscard(lunsToAdd)) {
            // The storage domain's discard after delete property is true
            // and there is at least one lun that doesn't support discard.
            return false;
        }
        return true;
    }

    public void logIfLunsBreakStorageDomainDiscardFunctionality(Collection<LUNs> luns, Guid storageDomainId) {
        Collection<LUNs> lunsThatBreakPassDiscardSupport = getLunsThatBreakPassDiscardSupport(luns, storageDomainId);
        if (!lunsThatBreakPassDiscardSupport.isEmpty()) {
            logLunsBrokeStorageDomainPassDiscardSupport(lunsThatBreakPassDiscardSupport, storageDomainId);
        }
        Collection<LUNs> lunsThatBreakDiscardAfterDeleteSupport =
                getLunsThatBreakDiscardAfterDeleteSupport(luns, storageDomainId);
        if (!lunsThatBreakDiscardAfterDeleteSupport.isEmpty()) {
            logLunsBrokeStorageDomainDiscardAfterDeleteSupport(lunsThatBreakDiscardAfterDeleteSupport, storageDomainId);
        }
    }

    public void logIfDisksWithIllegalPassDiscardExist(Guid vmId) {
        Collection<DiskImage> disks = DisksFilter.filterImageDisks(diskDao.getAllForVm(vmId));
        Collection<DiskVmElement> diskVmElements = diskVmElementDao.getAllForVm(vmId);
        Map<Disk, DiskVmElement> diskToDiskVmElement = diskHandler.getDiskToDiskVmElementMap(disks, diskVmElements);
        Map<Guid, Guid> diskIdToDestSdId = disks.stream()
                .collect(Collectors.toMap(DiskImage::getId, diskImage -> diskImage.getStorageIds().get(0)));
        MultipleDiskVmElementValidator multipleDiskVmElementValidator =
                new MultipleDiskVmElementValidator(diskToDiskVmElement);

        Collection<Guid> disksWithoutSupportForPassDiscard = multipleDiskVmElementValidator
                .getDisksWithoutSupportForPassDiscard(diskIdToDestSdId);
        if (!disksWithoutSupportForPassDiscard.isEmpty()) {
            AuditLogableBase auditLog = Injector.injectMembers(new AuditLogableBase());
            auditLog.addCustomValue("DisksIds", disksWithoutSupportForPassDiscard.stream()
                    .map(Guid::toString).collect(Collectors.joining(", ")));
            auditLogDirector.log(auditLog, AuditLogType.DISKS_WITH_ILLEGAL_PASS_DISCARD_EXIST);
        }
    }

    public boolean allLunsSupportDiscard(Collection<LUNs> luns) {
        return luns.stream().allMatch(getLunSupportsDiscardPredicate());
    }

    protected boolean allLunsHaveDiscardZeroesTheDataSupport(Collection<LUNs> luns) {
        return luns.stream().allMatch(getLunHasDiscardZeroesTheDataSupportPredicate());
    }

    protected boolean vmDiskWithPassDiscardExists(Collection<DiskVmElement> diskVmElements) {
        return diskVmElements.stream().anyMatch(DiskVmElement::isPassDiscard);
    }

    protected boolean vmDiskWithPassDiscardAndWadExists(Collection<DiskImage> storageDomainDisks,
            Collection<DiskVmElement> storageDomainVmDisks) {
        Map<Guid, DiskVmElement> diskVmElementsMap = storageDomainVmDisks.stream().collect(
                Collectors.toMap(DiskVmElement::getDiskId, Function.identity()));
        return storageDomainDisks.stream().anyMatch(sdDisk -> sdDisk.isWipeAfterDelete() &&
                diskVmElementsMap.containsKey(sdDisk.getId()) &&
                diskVmElementsMap.get(sdDisk.getId()).isPassDiscard());
    }

    protected Collection<LUNs> getLunsThatBreakPassDiscardSupport(Collection<LUNs> luns, Guid storageDomainId) {
        Collection<DiskImage> sdDisks = diskImageDao.getAllForStorageDomain(storageDomainId);
        Collection<Guid> sdDisksIds = sdDisks.stream().map(Disk::getId).collect(Collectors.toList());
        Collection<DiskVmElement> diskVmElements = diskVmElementDao.getAllDiskVmElementsByDisksIds(sdDisksIds);
        boolean sdContainsVmDiskWithPassDiscard = vmDiskWithPassDiscardExists(diskVmElements);

        if (sdContainsVmDiskWithPassDiscard) {
            boolean sdContainsVmDiskWithPassDiscardAndWad =
                    vmDiskWithPassDiscardAndWadExists(sdDisks, diskVmElements);

            return luns.stream()
                    .filter(getLunSupportsDiscardPredicate().negate()
                            .or(lun -> sdContainsVmDiskWithPassDiscardAndWad &&
                                    getLunHasDiscardZeroesTheDataSupportPredicate().negate().test(lun)))
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    protected Collection<LUNs> getLunsThatBreakDiscardAfterDeleteSupport(Collection<LUNs> luns, Guid storageDomainId) {
        StorageDomain storageDomain = storageDomainDao.get(storageDomainId);
        if (storageDomain.isDiscardAfterDelete()) {
            return luns.stream()
                    .filter(getLunSupportsDiscardPredicate().negate())
                    .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    private Predicate<LUNs> getLunSupportsDiscardPredicate() {
        return lun -> lun.getDiscardMaxSize() != null &&
                lun.getDiscardMaxSize() > 0L;
    }

    private Predicate<LUNs> getLunHasDiscardZeroesTheDataSupportPredicate() {
        return lun -> Boolean.TRUE.equals(lun.getDiscardZeroesData());
    }

    private void logLunsBrokeStorageDomainPassDiscardSupport(Collection<LUNs> lunsThatBreakSdPassDiscardSupport,
            Guid storageDomainId) {
        AuditLogableBase auditLog = Injector.injectMembers(new AuditLogableBase());
        auditLog.setStorageDomainId(storageDomainId);
        auditLog.addCustomValue("LunsIds",
                lunsThatBreakSdPassDiscardSupport.stream().map(LUNs::getLUNId).collect(Collectors.joining(", ")));
        auditLogDirector.log(auditLog, AuditLogType.LUNS_BROKE_SD_PASS_DISCARD_SUPPORT);
    }

    private void logLunsBrokeStorageDomainDiscardAfterDeleteSupport(
            Collection<LUNs> lunsThatBreakSdDiscardAfterDeleteSupport, Guid storageDomainId) {
        AuditLogableBase auditLog = Injector.injectMembers(new AuditLogableBase());
        auditLog.setStorageDomainId(storageDomainId);
        auditLog.addCustomValue("LunsIds", lunsThatBreakSdDiscardAfterDeleteSupport.stream()
                .map(LUNs::getLUNId).collect(Collectors.joining(", ")));
        auditLogDirector.log(auditLog, AuditLogType.LUNS_BROKE_SD_DISCARD_AFTER_DELETE_SUPPORT);
    }
}
