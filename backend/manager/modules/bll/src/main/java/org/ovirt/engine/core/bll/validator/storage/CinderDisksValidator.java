package org.ovirt.engine.core.bll.validator.storage;

import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.cinder.model.Limits;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.DiskDao;
import org.ovirt.engine.core.dao.StorageDomainDAO;

public class CinderDisksValidator {

    private Iterable<CinderDisk> cinderDisks;

    private Map<Guid, OpenStackVolumeProviderProxy> diskProxyMap;
    private Map<Guid, CinderStorageRelatedDisksAndProxy> cinderStorageToRelatedDisks;

    public CinderDisksValidator(Iterable<CinderDisk> cinderDisks) {
        this.cinderDisks = cinderDisks;
        this.diskProxyMap = initializeVolumeProviderProxyMap();
    }

    public CinderDisksValidator(CinderDisk cinderDisk) {
        this(Collections.singleton(cinderDisk));
    }

    private ValidationResult validate(Callable<ValidationResult> callable) {
        try {
            return callable.call();
        } catch (OpenStackResponseException e) {
            return new ValidationResult(VdcBllMessages.ACTION_TYPE_FAILED_CINDER,
                    String.format("$cinderException %1$s", e.getMessage()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public ValidationResult validateCinderDiskLimits() {
        return validate(new Callable<ValidationResult>() {
            @Override
            public ValidationResult call() {
                int diskIndex = 0;
                for (CinderDisk disk : cinderDisks) {
                    OpenStackVolumeProviderProxy proxy = diskProxyMap.get(disk.getId());
                    Limits limits = proxy.getLimits();
                    if (limits.getAbsolute().getTotalVolumesUsed() + diskIndex >= limits.getAbsolute().getMaxTotalVolumes()) {
                        return new ValidationResult(VdcBllMessages.CANNOT_ADD_CINDER_DISK_VOLUME_LIMIT_EXCEEDED,
                                String.format("$maxTotalVolumes %d", limits.getAbsolute().getMaxTotalVolumes()),
                                String.format("$diskAlias %s", disk.getDiskAlias()));
                    }
                    diskIndex++;
                }
                return ValidationResult.VALID;
            }
        });
    }

    public ValidationResult validateCinderDiskSnapshotsLimits() {
        return validate(new Callable<ValidationResult>() {
            @Override
            public ValidationResult call() {
                Map<Guid, CinderStorageRelatedDisksAndProxy> relatedCinderDisksByStorageMap =
                        getRelatedCinderDisksToStorageDomainMap();
                Collection<CinderStorageRelatedDisksAndProxy> relatedCinderDisksByStorageCollection =
                        relatedCinderDisksByStorageMap.values();
                for (CinderStorageRelatedDisksAndProxy relatedCinderDisksByStorage : relatedCinderDisksByStorageCollection) {
                    Limits limits = relatedCinderDisksByStorage.getProxy().getLimits();
                    int numOfDisks = relatedCinderDisksByStorage.getCinderDisks().size();
                    if (isLimitExceeded(limits, VolumeClassification.Snapshot, numOfDisks)) {
                        String storageName =
                                getStorageDomainDao().get(relatedCinderDisksByStorage.getStorageDomainId())
                                        .getStorageName();
                        return new ValidationResult(VdcBllMessages.CANNOT_ADD_CINDER_DISK_SNAPSHOT_LIMIT_EXCEEDED,
                                String.format("$maxTotalSnapshots %d", limits.getAbsolute().getMaxTotalVolumes()),
                                String.format("$storageName %s", storageName));
                    }
                }
                return ValidationResult.VALID;
            }
        });
    }

    private boolean isLimitExceeded(Limits limits, VolumeClassification cinderType, int diskCount) {
        if (cinderType == VolumeClassification.Snapshot) {
            return (limits.getAbsolute().getTotalSnapshotsUsed() + diskCount >= limits.getAbsolute().getMaxTotalSnapshots());
        }
        if (cinderType == VolumeClassification.Volume) {
            return (limits.getAbsolute().getTotalVolumesUsed() + diskCount >= limits.getAbsolute().getMaxTotalVolumes());
        }
        return false;
    }

    private Map<Guid, CinderStorageRelatedDisksAndProxy> getRelatedCinderDisksToStorageDomainMap() {
        if (cinderStorageToRelatedDisks == null) {
            cinderStorageToRelatedDisks = new HashMap<>();
            for (CinderDisk cinderDisk : cinderDisks) {
                Guid storageDomainId = cinderDisk.getStorageIds().get(0);
                CinderStorageRelatedDisksAndProxy cinderRelatedDisksAndProxy =
                        cinderStorageToRelatedDisks.get(storageDomainId);
                if (cinderRelatedDisksAndProxy == null) {
                    List cinderDisks = new ArrayList();
                    cinderDisks.add(cinderDisk);
                    OpenStackVolumeProviderProxy proxy = diskProxyMap.get(cinderDisk.getId());
                    CinderStorageRelatedDisksAndProxy newCinderRelatedDisksAndProxy =
                            new CinderStorageRelatedDisksAndProxy(storageDomainId, cinderDisks, proxy);
                    cinderStorageToRelatedDisks.put(storageDomainId, newCinderRelatedDisksAndProxy);
                } else {
                    cinderRelatedDisksAndProxy.getCinderDisks().add(cinderDisk);
                }
            }
        }
        return cinderStorageToRelatedDisks;
    }

    private static class CinderStorageRelatedDisksAndProxy {
        private Guid storageDomainId;
        private List<CinderDisk> cinderDisks = new ArrayList<>();
        private OpenStackVolumeProviderProxy proxy;

        public CinderStorageRelatedDisksAndProxy(Guid storageDomainId, List<CinderDisk> cinderDisks, OpenStackVolumeProviderProxy proxy) {
            setStorageDomainId(storageDomainId);
            setCinderDisks(cinderDisks);
            setProxy(proxy);
        }

        public Guid getStorageDomainId() {
            return storageDomainId;
        }

        public void setStorageDomainId(Guid storageDomainId) {
            this.storageDomainId = storageDomainId;
        }

        public List<CinderDisk> getCinderDisks() {
            return cinderDisks;
        }

        public void setCinderDisks(List<CinderDisk> cinderDisks) {
            this.cinderDisks = cinderDisks;
        }

        public OpenStackVolumeProviderProxy getProxy() {
            return proxy;
        }

        public void setProxy(OpenStackVolumeProviderProxy proxy) {
            this.proxy = proxy;
        }
    }

    public ValidationResult validateCinderDisksAlreadyRegistered() {
        return validate(new Callable<ValidationResult>() {
            @Override
            public ValidationResult call() {
                for (CinderDisk disk : cinderDisks) {
                    Disk diskFromDB = getDiskDao().get(disk.getId());
                    if (diskFromDB != null) {
                        return new ValidationResult(VdcBllMessages.CINDER_DISK_ALREADY_REGISTERED,
                                String.format("$diskAlias %s", diskFromDB.getDiskAlias()));
                    }
                }
                return ValidationResult.VALID;
            }
        });
    }

    private Map<Guid, OpenStackVolumeProviderProxy> initializeVolumeProviderProxyMap() {
        if (diskProxyMap == null) {
            diskProxyMap = new HashMap<>();
            for (CinderDisk cinderDisk : cinderDisks) {
                OpenStackVolumeProviderProxy volumeProviderProxy = getVolumeProviderProxy(cinderDisk);
                diskProxyMap.put(cinderDisk.getId(), volumeProviderProxy);
            }

        }
        return diskProxyMap;
    }

    private OpenStackVolumeProviderProxy getVolumeProviderProxy(CinderDisk cinderDisk) {
        if (cinderDisk == null || cinderDisk.getStorageIds().isEmpty()) {
            return null;
        }
        return OpenStackVolumeProviderProxy.getFromStorageDomainId(cinderDisk.getStorageIds().get(0));
    }

    protected DiskDao getDiskDao() {
        return DbFacade.getInstance().getDiskDao();
    }

    protected StorageDomainDAO getStorageDomainDao() {
        return DbFacade.getInstance().getStorageDomainDao();
    }
}
