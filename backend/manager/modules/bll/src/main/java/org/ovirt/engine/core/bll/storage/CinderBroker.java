package org.ovirt.engine.core.bll.storage;

import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.cinder.model.ConnectionForInitialize;
import com.woorea.openstack.cinder.model.Volume;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import com.woorea.openstack.cinder.model.VolumeForUpdate;
import org.apache.commons.httpclient.HttpStatus;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.businessentities.storage.CinderConnectionInfo;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.CinderVolumeStatus;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.common.errors.VdcBllErrors;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogableBase;

import java.util.ArrayList;
import java.util.concurrent.Callable;

public class CinderBroker extends AuditLogableBase {

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
            executeFailedMessages.add(VdcBllErrors.CINDER_ERROR.name());
            executeFailedMessages.add(String.format("$cinderException %1$s", e.getMessage()));
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public String createDisk(final CinderDisk cinderDisk) {
        return execute(new Callable<String>() {
            @Override
            public String call() {
                VolumeForCreate cinderVolume = new VolumeForCreate();
                cinderVolume.setName(cinderDisk.getDiskAlias());
                cinderVolume.setDescription(cinderDisk.getDiskDescription());
                cinderVolume.setSize((int) (cinderDisk.getSizeInGigabytes()));
                cinderVolume.setVolumeType(cinderDisk.getCinderVolumeType());
                return proxy.createVolume(cinderVolume);
            }
        });
    }

    public String cloneDisk(final CinderDisk cinderDisk) {
        return execute(new Callable<String>() {
            @Override
            public String call() {
                VolumeForCreate cinderVolume = new VolumeForCreate();
                cinderVolume.setName(cinderDisk.getDiskAlias());
                cinderVolume.setDescription(cinderDisk.getDiskDescription());
                cinderVolume.setSize((int) (cinderDisk.getSizeInGigabytes()));
                cinderVolume.setSourceVolid(cinderDisk.getId().toString());
                return proxy.createVolume(cinderVolume);
            }
        });
    }

    public Void deleteDisk(final CinderDisk cinderDisk) {
        return execute(new Callable<Void>() {
            @Override
            public Void call() {
                proxy.deleteVolume(cinderDisk.getId().toString());
                return null;
            }
        });
    }

    public Void updateDisk(final CinderDisk cinderDisk) {
        return execute(new Callable<Void>() {
            @Override
            public Void call() {
                VolumeForUpdate volumeForUpdate = new VolumeForUpdate();
                volumeForUpdate.setName(cinderDisk.getDiskAlias());
                volumeForUpdate.setDescription(cinderDisk.getDiskDescription());
                proxy.updateVolume(cinderDisk.getId().toString(), volumeForUpdate);
                return null;
            }
        });
    }

    public Void extendDisk(final CinderDisk cinderDisk, final int newSize) {
        return execute(new Callable<Void>() {
            @Override
            public Void call() {
                proxy.extendVolume(cinderDisk.getId().toString(), newSize);
                return null;
            }
        });
    }

    public CinderConnectionInfo initializeConnectionForDisk(final CinderDisk cinderDisk) {
        return execute(new Callable<CinderConnectionInfo>() {
            @Override
            public CinderConnectionInfo call() {
                ConnectionForInitialize connectionForInitialize = new ConnectionForInitialize();
                return proxy.initializeConnectionForVolume(cinderDisk.getId().toString(), connectionForInitialize);
            }
        });
    }

    public boolean isDiskExist(final Guid id) {
        return execute(new Callable<Boolean>() {
            @Override
            public Boolean call() {
                try {
                    Volume volume = proxy.getVolumeById(id.toString());
                    return volume != null;
                } catch (OpenStackResponseException ex) {
                    if (ex.getStatus() == HttpStatus.SC_NOT_FOUND) {
                        return false;
                    }
                    throw ex;
                }
            }
        });
    }

    public ImageStatus getDiskStatus(final Guid id) {
        return execute(new Callable<ImageStatus>() {
            @Override
            public ImageStatus call() {
                Volume volume = proxy.getVolumeById(id.toString());
                CinderVolumeStatus cinderVolumeStatus = CinderVolumeStatus.forValue(volume.getStatus());
                return mapCinderVolumeStatusToImageStatus(cinderVolumeStatus);
            }
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
                return ImageStatus.ILLEGAL;
            default:
                return null;
        }
    }

    private OpenStackVolumeProviderProxy getVolumeProviderProxy(Guid storageDomainId) {
        if (proxy == null) {
            proxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(storageDomainId);
        }
        return proxy;
    }

}
