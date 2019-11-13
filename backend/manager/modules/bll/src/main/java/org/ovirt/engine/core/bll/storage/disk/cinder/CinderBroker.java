package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.http.HttpStatus;
import org.ovirt.engine.core.bll.provider.ProviderProxyFactory;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.storage.CinderConnectionInfo;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeDriver;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.businessentities.storage.VolumeClassification;
import org.ovirt.engine.core.common.businessentities.storage.VolumeFormat;
import org.ovirt.engine.core.common.errors.EngineError;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableImpl;
import org.ovirt.engine.core.di.Injector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.cinder.model.ConnectionForInitialize;
import com.woorea.openstack.cinder.model.Snapshot;
import com.woorea.openstack.cinder.model.SnapshotForCreate;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import com.woorea.openstack.cinder.model.VolumeForUpdate;

public class CinderBroker {

    private static final Logger log = LoggerFactory.getLogger(CinderBroker.class);
    public static final String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSS";

    private OpenStackVolumeProviderProxy proxy;
    private ArrayList<String> executeFailedMessages;

    public CinderBroker(Guid storageDomainId, ArrayList<String> executeFailedMessages) {
        this.proxy = getVolumeProviderProxy(storageDomainId);
        this.executeFailedMessages = executeFailedMessages;
    }

    private <T> T execute(Callable<T> callable) {
        try {
            return callable.call();
        } catch (OpenStackResponseException e) {
            executeFailedMessages.add(EngineError.CINDER_ERROR.name());
            executeFailedMessages.add(String.format("$cinderException %1$s", e.getMessage()));
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String createDisk(final CinderDisk cinderDisk) {
        return execute(() -> {
            VolumeForCreate cinderVolume = new VolumeForCreate();
            cinderVolume.setName(cinderDisk.getDiskAlias());
            cinderVolume.setDescription(cinderDisk.getDiskDescription());
            cinderVolume.setSize((int) cinderDisk.getSizeInGigabytes());
            cinderVolume.setVolumeType(cinderDisk.getCinderVolumeType());
            return proxy.createVolume(cinderVolume);
        });
    }

    public String cloneDisk(final CinderDisk cinderDisk) {
        return execute(() -> {
            VolumeForCreate cinderVolume = new VolumeForCreate();
            cinderVolume.setName(cinderDisk.getDiskAlias());
            cinderVolume.setDescription(cinderDisk.getDiskDescription());
            cinderVolume.setSize((int) cinderDisk.getSizeInGigabytes());
            cinderVolume.setSourceVolid(cinderDisk.getImageId().toString());
            return proxy.createVolume(cinderVolume);
        });
    }

    public VolumeClassification deleteVolumeByClassificationType(CinderDisk cinderDisk) {
        VolumeClassification cinderVolumeType = cinderDisk.getVolumeClassification();
        if (cinderVolumeType == VolumeClassification.Volume) {
            deleteVolume(cinderDisk);
        } else if (cinderVolumeType == VolumeClassification.Snapshot) {
            deleteSnapshot(cinderDisk.getImageId());
        } else {
            log.error("Error, could not determine Cinder entity {} with id {} from Cinder provider.",
                    cinderDisk.getDiskAlias(),
                    cinderDisk.getImageId());
        }
        return cinderVolumeType;
    }

    public ImageStatus getImageStatusByClassificationType(CinderDisk cinderDisk) {
        VolumeClassification cinderVolumeType = cinderDisk.getVolumeClassification();
        if (cinderVolumeType == VolumeClassification.Volume) {
            return getDiskStatus(cinderDisk.getImageId());
        } else if (cinderVolumeType == VolumeClassification.Snapshot) {
            return getSnapshotStatus(cinderDisk.getImageId());
        }
        log.error("Error, could not determine Cinder entity {} with id {} from Cinder provider.",
                cinderDisk.getDiskAlias(),
                cinderDisk.getImageId());
        return ImageStatus.ILLEGAL;
    }

    public boolean isVolumeExistsByClassificationType(CinderDisk cinderDisk) {
        VolumeClassification cinderVolumeType = cinderDisk.getVolumeClassification();
        if (cinderVolumeType == VolumeClassification.Volume) {
            return isDiskExist(cinderDisk.getImageId());
        } else if (cinderVolumeType == VolumeClassification.Snapshot) {
            return isSnapshotExist(cinderDisk.getImageId());
        }
        log.error("No valid cinder volume type enum has been initialized in the Cinder disk business entity.");
        return true;
    }

    public boolean deleteVolume(final CinderDisk cinderDisk) {
        return execute(() -> {
            try {
                proxy.deleteVolume(cinderDisk.getImageId().toString());
                return true;
            } catch (OpenStackResponseException ex) {
                if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                    return false;
                }
                throw ex;
            }
        });
    }

    public Void updateDisk(final CinderDisk cinderDisk) {
        return execute(() -> {
            VolumeForUpdate volumeForUpdate = new VolumeForUpdate();
            volumeForUpdate.setName(cinderDisk.getDiskAlias());
            volumeForUpdate.setDescription(cinderDisk.getDiskDescription());
            proxy.updateVolume(cinderDisk.getImageId().toString(), volumeForUpdate);
            return null;
        });
    }

    public Void extendDisk(final CinderDisk cinderDisk, final int newSize) {
        return execute(() -> {
            proxy.extendVolume(cinderDisk.getImageId().toString(), newSize);
            return null;
        });
    }

    public String createSnapshot(final CinderDisk cinderDisk, final String snapshotDescription) {
        return execute(() -> {
            SnapshotForCreate snapshotForCreate = new SnapshotForCreate();
            snapshotForCreate.setVolumeId(cinderDisk.getImageId().toString());
            snapshotForCreate.setDescription(snapshotDescription);
            return proxy.createSnapshot(snapshotForCreate);
        });
    }

    public boolean deleteSnapshot(final Guid snapshotId) {
        return execute(() -> {
            try {
                proxy.deleteSnapshot(snapshotId.toString());
                return true;
            } catch (OpenStackResponseException ex) {
                if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                    return false;
                }
                throw ex;
            }
        });
    }

    public String cloneVolumeFromSnapshot(final CinderDisk cinderDisk, final Guid snapshotId) {
        return execute(() -> {
            VolumeForCreate cinderVolume = new VolumeForCreate();
            cinderVolume.setName(cinderDisk.getDiskAlias());
            cinderVolume.setDescription(cinderDisk.getDiskDescription());
            cinderVolume.setSize((int) cinderDisk.getSizeInGigabytes());
            cinderVolume.setVolumeType(cinderDisk.getCinderVolumeType());
            cinderVolume.setSnapshotId(snapshotId.toString());
            return proxy.cloneVolumeFromSnapshot(cinderVolume);
        });
    }

    public CinderConnectionInfo initializeConnectionForDisk(final CinderDisk cinderDisk) {
        return execute(() -> {
            ConnectionForInitialize connectionForInitialize = new ConnectionForInitialize();
            return proxy.initializeConnectionForVolume(cinderDisk.getImageId().toString(), connectionForInitialize);
        });
    }

    public void updateConnectionInfoForDisk(CinderDisk cinderDisk) {
        try {
            CinderConnectionInfo connectionInfo = initializeConnectionForDisk(cinderDisk);
            CinderVolumeDriver cinderVolumeDriver = CinderVolumeDriver.forValue(connectionInfo.getDriverVolumeType());
            if (cinderVolumeDriver == null) {
                logDiskEvent(cinderDisk, AuditLogType.CINDER_DISK_CONNECTION_VOLUME_DRIVER_UNSUPPORTED);
            }
            cinderDisk.setCinderConnectionInfo(connectionInfo);
        } catch (OpenStackResponseException ex) {
            logDiskEvent(cinderDisk, AuditLogType.CINDER_DISK_CONNECTION_FAILURE);
            throw ex;
        }
    }

    private void logDiskEvent(CinderDisk cinderDisk, AuditLogType cinderDiskConnectionFailure) {
        AuditLogable logable = new AuditLogableImpl();
        logable.addCustomValue("DiskAlias", cinderDisk.getDiskAlias());
        Injector.get(AuditLogDirector.class).log(logable, cinderDiskConnectionFailure);
    }

    public boolean isDiskExist(final Guid id) {
        return execute(() -> {
            try {
                Volume volume = proxy.getVolumeById(id.toString());
                return volume != null;
            } catch (OpenStackResponseException ex) {
                if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                    return false;
                }
                throw ex;
            }
        });
    }

    public boolean isSnapshotExist(final Guid id) {
        return execute(() -> {
            try {
                Snapshot snapshot = proxy.getSnapshotById(id.toString());
                return snapshot != null;
            } catch (OpenStackResponseException ex) {
                if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                    log.info("Snapshot does not exists");
                    return false;
                } else {
                    log.error("An error has occurred while looking for snapshot.");
                    throw ex;
                }

            }
        });
    }

    public ImageStatus getSnapshotStatus(final Guid id) {
        return execute(() -> {
            Snapshot snapshot = proxy.getSnapshotById(id.toString());
            CinderVolumeStatus cinderVolumeStatus = CinderVolumeStatus.forValue(snapshot.getStatus());
            return mapCinderVolumeStatusToImageStatus(cinderVolumeStatus);
        });
    }

    public ImageStatus getDiskStatus(final Guid id) {
        return execute(() -> {
            Volume volume = proxy.getVolumeById(id.toString());
            CinderVolumeStatus cinderVolumeStatus = CinderVolumeStatus.forValue(volume.getStatus());
            return mapCinderVolumeStatusToImageStatus(cinderVolumeStatus);
        });
    }

    protected static ImageStatus mapCinderVolumeStatusToImageStatus(CinderVolumeStatus cinderVolumeStatus) {
        switch (cinderVolumeStatus) {
            case Available:
                return ImageStatus.OK;
            case Creating:
            case Deleting:
            case Extending:
                return ImageStatus.LOCKED;
            case Error:
            case ErrorDeleting:
            case ErrorExtending:
                return ImageStatus.ILLEGAL;
            default:
                return null;
        }
    }

    public static List<CinderDisk> volumesToCinderDisks(List<Volume> volumes, Guid storageDomainId) {
        List<CinderDisk> cinderDisks = new ArrayList<>();
        for (Volume volume : volumes) {
            cinderDisks.add(volumeToCinderDisk(volume, storageDomainId));
        }
        return cinderDisks;
    }

    public static CinderDisk volumeToCinderDisk(Volume volume, Guid storageDomainId) {
        CinderDisk cinderDisk = new CinderDisk();
        cinderDisk.setId(Guid.createGuidFromString(volume.getId()));
        cinderDisk.setImageId(Guid.createGuidFromString(volume.getId()));
        cinderDisk.setDiskAlias(volume.getName());
        cinderDisk.setDescription(volume.getDescription());
        cinderDisk.setSizeInGigabytes(volume.getSize());
        cinderDisk.setCinderVolumeType(volume.getVolumeType());
        cinderDisk.setStorageIds(new ArrayList<>(Arrays.asList(storageDomainId)));
        cinderDisk.setActive(true);
        cinderDisk.setImageStatus(ImageStatus.OK);
        cinderDisk.setVolumeFormat(VolumeFormat.RAW);
        try {
            cinderDisk.setCreationDate(new SimpleDateFormat(DATE_FORMAT).parse(volume.getCreatedAt()));
        } catch (ParseException e) {
            cinderDisk.setCreationDate(null);
            log.error("Invalid disk creation date format, id: '{}' (info: {})", volume.getId(), e.getMessage());
        }
        return cinderDisk;
    }

    private OpenStackVolumeProviderProxy getVolumeProviderProxy(Guid storageDomainId) {
        if (proxy == null) {
            proxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(storageDomainId,
                    Injector.get(ProviderProxyFactory.class));
        }
        return proxy;
    }

}
