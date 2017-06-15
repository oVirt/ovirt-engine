package org.ovirt.engine.core.bll.storage.disk.cinder;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.action.GetCinderEntityByStorageDomainIdParameters;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

import com.woorea.openstack.cinder.model.Volume;

public class GetUnregisteredCinderDiskByIdAndStorageDomainIdQuery<P extends GetCinderEntityByStorageDomainIdParameters>
        extends CinderQueryBase<P> {

    @Inject
    private DiskDao diskDao;

    public GetUnregisteredCinderDiskByIdAndStorageDomainIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        Disk diskFromDao = diskDao.get(getParameters().getEntityId());
        if (diskFromDao != null) {
            log.info("The disk already exist in the DB, hence, should not be fetched from Cinder. ID: '{}', Alias: '{}'",
                    diskFromDao.getId(), diskFromDao.getDiskAlias());
            getQueryReturnValue().setReturnValue(null);
            return;
        }
        String volumeId = getParameters().getEntityId().toString();
        Volume volume = getVolumeProviderProxy().getVolumeById(volumeId);
        if (volume == null) {
            log.info("The volume doesn't exist in Cinder. ID: '{}'", volumeId);
            getQueryReturnValue().setReturnValue(null);
            return;
        }
        Guid storageDomainId = getParameters().getId();
        CinderDisk unregisteredDisk = CinderBroker.volumeToCinderDisk(volume, storageDomainId);
        getQueryReturnValue().setReturnValue(unregisteredDisk);
    }
}
