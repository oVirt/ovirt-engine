package org.ovirt.engine.core.bll.gluster;

import java.util.ArrayList;
import java.util.List;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.gluster.tasks.GlusterTaskUtils;
import org.ovirt.engine.core.bll.interfaces.BackendInternal;
import org.ovirt.engine.core.bll.validator.gluster.GlusterBrickValidator;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.gluster.AccessProtocol;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterStatus;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.businessentities.gluster.TransportType;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.dao.gluster.GlusterVolumeDao;

public abstract class AbstractRemoveGlusterVolumeBricksCommandTest extends BaseCommandTest {

    @Mock
    GlusterVolumeDao volumeDao;
    @Mock
    GlusterBrickDao brickDao;
    @Mock
    protected BackendInternal backend;
    @Mock
    protected VDSBrokerFrontend vdsBrokerFrontend;
    @Spy
    private GlusterTaskUtils glusterTaskUtils;
    @InjectMocks
    @Spy
    private GlusterBrickValidator brickValidator;

    protected final Guid volumeWithRemoveBricksTask = new Guid("8bc6f108-c0ef-43ab-ba20-ec41107220f5");
    protected final Guid volumeWithoutAsyncTask = new Guid("000000000000-0000-0000-0000-00000003");
    protected final Guid volumeWithoutRemoveBricksTask = new Guid("000000000000-0000-0000-0000-00000004");
    protected final Guid CLUSTER_ID = new Guid("b399944a-81ab-4ec5-8266-e19ba7c3c9d1");
    protected final Guid SERVER_ID = new Guid("da9e2f09-2835-4530-9bf5-576c52b11941");
    protected static final Guid BRICK_UUID1 = new Guid("6ccdc294-d77b-4929-809d-8afe7634b47d");

    protected List<GlusterBrickEntity> getInvalidNoOfBricks(Guid volumeId) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setVolumeId(volumeId);
        brick.setBrickDirectory("/tmp/test-vol1");
        brick.setServerId(SERVER_ID);
        brick.setStatus(GlusterStatus.UP);
        bricks.add(brick);
        return bricks;
    }

    protected List<GlusterBrickEntity> getInvalidBricks(Guid volumeId) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();
        GlusterBrickEntity brick1 = new GlusterBrickEntity();
        brick1.setVolumeId(volumeId);
        brick1.setBrickDirectory("/tmp/test-vol1");
        brick1.setServerId(SERVER_ID);
        brick1.setStatus(GlusterStatus.UP);
        bricks.add(brick1);
        GlusterBrickEntity brick2 = new GlusterBrickEntity();
        brick2.setVolumeId(volumeId);
        brick2.setBrickDirectory("/tmp/test-vol122");
        brick2.setServerId(SERVER_ID);
        brick2.setStatus(GlusterStatus.UP);
        bricks.add(brick2);
        return bricks;
    }

    protected Object getVolumeWithoutRemoveBricksTask(Guid volumeId) {
        GlusterVolumeEntity volume = getVolumeWithRemoveBricksTask(volumeId);
        volume.getAsyncTask().setType(null);
        return volume;
    }

    protected VDS getVds(VDSStatus status) {
        VDS vds = new VDS();
        vds.setId(Guid.newGuid());
        vds.setVdsName("gfs1");
        vds.setClusterId(CLUSTER_ID);
        vds.setStatus(status);
        return vds;
    }

    protected GlusterVolumeEntity getVolume(Guid id) {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.setId(id);
        volumeEntity.setName("test-vol");
        volumeEntity.addAccessProtocol(AccessProtocol.GLUSTER);
        volumeEntity.addTransportType(TransportType.TCP);
        volumeEntity.setStatus(GlusterStatus.UP);
        volumeEntity.setBricks(getBricks(id));
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        volumeEntity.setClusterId(CLUSTER_ID);
        return volumeEntity;
    }

    protected List<GlusterBrickEntity> getBricks(Guid volumeId) {
        List<GlusterBrickEntity> bricks = new ArrayList<>();

        GlusterBrickEntity brick1 = new GlusterBrickEntity();
        brick1.setVolumeId(volumeId);
        brick1.setId(BRICK_UUID1);
        brick1.setBrickDirectory("/tmp/test-vol0");
        brick1.setServerId(SERVER_ID);
        brick1.setStatus(GlusterStatus.UP);
        bricks.add(brick1);

        GlusterBrickEntity brick2 = new GlusterBrickEntity();
        brick2.setVolumeId(volumeId);
        brick2.setId(BRICK_UUID1);
        brick2.setBrickDirectory("/tmp/test-vol1");
        brick2.setServerId(SERVER_ID);
        brick2.setStatus(GlusterStatus.UP);
        bricks.add(brick2);

        return bricks;
    }

    protected abstract GlusterVolumeEntity getVolumeWithRemoveBricksTask(Guid volumeId);
}
