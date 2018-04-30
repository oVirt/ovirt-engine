package org.ovirt.engine.core.bll.gluster;

import static org.mockito.Mockito.doReturn;

import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GeoRepSessionStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterGeoRepSession;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.StorageDomainDRDao;
import org.ovirt.engine.core.dao.gluster.GlusterGeoRepDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public abstract class GeoRepSessionCommandTest<T extends GeoRepSessionCommandBase<?>> extends BaseCommandTest {

    @Mock
    protected GlusterGeoRepDao geoRepDao;
    @Mock
    protected GlusterVolumeDao volumeDao;
    @Mock
    private StorageDomainDRDao storageDomainDRDao;
    protected final Guid stoppedVolumeId = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    protected final Guid startedVolumeId = new Guid("b2cb2f73-fab3-4a42-93f0-d5e4c069a43e");
    protected final Guid geoRepSessionId = new Guid("bbcb2f73-fab3-4a42-93f0-d5e4c069a43e");
    private final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");

    @Spy
    @InjectMocks
    T cmd = createCommand();

    protected abstract T createCommand();

    @BeforeEach
    public void prepareMocks() {
        doReturn(getVds(VDSStatus.Up)).when(cmd).getUpServer();
        doReturn(getGeoRepSession(geoRepSessionId)).when(geoRepDao).getById(geoRepSessionId);
        doReturn(getGlusterVolume(startedVolumeId)).when(volumeDao).getById(startedVolumeId);
        doReturn(getGlusterVolume(stoppedVolumeId)).when(volumeDao).getById(stoppedVolumeId);
    }

    private VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    private GlusterGeoRepSession getGeoRepSession(Guid gSessionId) {
        return getGeoRepSession(gSessionId, GeoRepSessionStatus.ACTIVE);
    }

    protected GlusterGeoRepSession getGeoRepSession(Guid gSessionId, GeoRepSessionStatus status) {
        GlusterGeoRepSession session = new GlusterGeoRepSession();
        session.setStatus(status);
        session.setId(gSessionId);
        return session;
    }

    private GlusterVolumeEntity getGlusterVolume(Guid volumeId) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(volumeId);
        volumeEntity.setName("test-vol");
        volumeEntity.setStatus(volumeId.equals(startedVolumeId) ? GlusterStatus.UP : GlusterStatus.DOWN);
        volumeEntity.setClusterId(CLUSTER_ID);
        return volumeEntity;
    }
}
