package org.ovirt.engine.core.bll.storage.disk.cinder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.AbstractQueryTest;
import org.ovirt.engine.core.bll.provider.storage.OpenStackVolumeProviderProxy;
import org.ovirt.engine.core.common.businessentities.storage.CinderDisk;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.DiskDao;

import com.woorea.openstack.cinder.model.Volume;

public class GetUnregisteredCinderDisksByStorageDomainIdQueryTest
        extends AbstractQueryTest<IdQueryParameters, GetUnregisteredCinderDisksByStorageDomainIdQuery<IdQueryParameters>> {

    @Mock
    private OpenStackVolumeProviderProxy provider;

    @Mock
    private DiskDao diskDao;

    private List<Volume> volumes;

    @Override
    protected void initQuery(GetUnregisteredCinderDisksByStorageDomainIdQuery<IdQueryParameters> query) {
        super.initQuery(query);
        IdQueryParameters parameters = query.getParameters();
        when(parameters.getId()).thenReturn(Guid.newGuid());
        setUpVolumes();
        setUpDisks();
    }

    private void setUpVolumes() {
        volumes = new ArrayList<>(3);
        for (int i = 0; i < 3; ++i) {
            Volume v = mock(Volume.class);
            when(v.getId()).thenReturn(Guid.newGuid().toString());
            when(v.getName()).thenReturn("volume" + i);
            when(v.getSize()).thenReturn(i);
            when(v.getCreatedAt()).thenReturn(new SimpleDateFormat(CinderBroker.DATE_FORMAT).format(new Date()));
            volumes.add(v);
        }

        when(provider.getVolumes()).thenReturn(volumes);
        doReturn(provider).when(getQuery()).getVolumeProviderProxy();
    }

    private void setUpDisks() {
        Volume existingVolume = volumes.get(1);
        List<Disk> existingDisks =
                new ArrayList<>(CinderBroker.volumesToCinderDisks
                        (Collections.singletonList(existingVolume), getQueryParameters().getId()));

        when(diskDao.getAllFromDisksByDiskStorageType(DiskStorageType.CINDER, getUser().getId(), false)).thenReturn(existingDisks);
    }

    @Test
    public void executeQueryCommand() {
        getQuery().executeQueryCommand();

        List<CinderDisk> result = getQuery().getQueryReturnValue().getReturnValue();

        assertEquals(2, result.size());
        assertDiskFromVolume(volumes.get(0), result.get(0));
        assertDiskFromVolume(volumes.get(2), result.get(1));
    }

    private void assertDiskFromVolume(Volume volume, CinderDisk  disk) {
        assertEquals(volume.getId(), disk.getId().toString());
        assertEquals(volume.getName(), disk.getDiskAlias());
        assertEquals(volume.getSize().intValue(), disk.getSizeInGigabytes());
        assertEquals(getQueryParameters().getId(), disk.getStorageIds().get(0));
    }
}
