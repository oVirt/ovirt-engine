package org.ovirt.engine.core.bll.storage;

import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.utils.linq.LinqUtils;
import org.ovirt.engine.core.utils.linq.Predicate;

import com.woorea.openstack.cinder.model.Volume;

public class GetUnregisteredCinderDisksByStorageDomainIdQuery<P extends IdQueryParameters> extends CinderQueryBase<P> {

    public GetUnregisteredCinderDisksByStorageDomainIdQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        final List<Volume> allVolumes = getVolumeProviderProxy().getVolumes();
        final List<Disk> registeredDisks =
                getDbFacade().getDiskDao().getAllFromDisksByDiskStorageType(DiskStorageType.CINDER,
                        getUserID(),
                        getParameters().isFiltered());

        List<Volume> unregisteredVolumes = LinqUtils.filter(allVolumes, new Predicate<Volume>() {
            @Override
            public boolean eval(Volume volume) {
                for (Disk registeredDisk : registeredDisks) {
                    if (volume.getId().equals(registeredDisk.getId().toString())) {
                        return false;
                    }
                }
                return true;
            }
        });

        Guid storageDomainId = getParameters().getId();
        List<CinderDisk> unregisteredDisks = CinderBroker.volumesToCinderDisks(unregisteredVolumes, storageDomainId);
        getQueryReturnValue().setReturnValue(unregisteredDisks);
    }
}
