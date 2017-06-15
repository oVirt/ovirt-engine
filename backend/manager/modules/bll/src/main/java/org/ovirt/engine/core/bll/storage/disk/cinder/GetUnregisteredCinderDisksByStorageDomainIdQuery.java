package org.ovirt.engine.core.bll.storage.disk.cinder;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.inject.Inject;

import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

import com.woorea.openstack.cinder.model.Volume;

public class GetUnregisteredCinderDisksByStorageDomainIdQuery<P extends IdQueryParameters> extends CinderQueryBase<P> {
    @Inject
    private DiskDao diskDao;

    public GetUnregisteredCinderDisksByStorageDomainIdQuery(P parameters, EngineContext context) {
        super(parameters, context);
    }

    @Override
    protected void executeQueryCommand() {
        final List<Volume> allVolumes = getVolumeProviderProxy().getVolumes();
        final List<Disk> registeredDisks =
                diskDao.getAllFromDisksByDiskStorageType(DiskStorageType.CINDER,
                        getUserID(),
                        getParameters().isFiltered());
        Set<String> registeredIDs = registeredDisks.stream().map(d -> d.getId().toString()).collect(Collectors.toSet());

        List<Volume> unregisteredVolumes =
                allVolumes.stream().filter(v -> !registeredIDs.contains(v.getId())).collect(Collectors.toList());

        Guid storageDomainId = getParameters().getId();
        List<CinderDisk> unregisteredDisks = CinderBroker.volumesToCinderDisks(unregisteredVolumes, storageDomainId);
        getQueryReturnValue().setReturnValue(unregisteredDisks);
    }
}
