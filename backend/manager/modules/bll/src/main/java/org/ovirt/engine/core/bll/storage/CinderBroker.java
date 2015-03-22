package org.ovirt.engine.core.bll.storage;

import com.woorea.openstack.base.client.OpenStackResponseException;
import com.woorea.openstack.cinder.model.VolumeForCreate;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
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

    private OpenStackVolumeProviderProxy getVolumeProviderProxy(Guid storageDomainId) {
        if (proxy == null) {
            proxy = OpenStackVolumeProviderProxy.getFromStorageDomainId(storageDomainId);
        }
        return proxy;
    }

}
