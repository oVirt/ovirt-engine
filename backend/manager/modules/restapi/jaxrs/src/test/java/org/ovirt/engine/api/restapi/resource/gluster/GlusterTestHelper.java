package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.expect;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.easymock.IMocksControl;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterAsyncTask;
import org.ovirt.engine.core.common.asynctasks.gluster.GlusterTaskType;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.MallInfo;
import org.ovirt.engine.core.common.businessentities.gluster.MemoryStatus;
import org.ovirt.engine.core.common.job.JobExecutionStatus;
import org.ovirt.engine.core.compat.Guid;

public class GlusterTestHelper {

    protected static final Guid[] GUIDS = { new Guid("00000000-0000-0000-0000-000000000000"),
        new Guid("11111111-1111-1111-1111-111111111111"),
        new Guid("22222222-2222-2222-2222-222222222222"),
        new Guid("33333333-3333-3333-3333-333333333333") };
    protected static final Guid clusterId = GUIDS[0];
    protected static final Guid serverId = GUIDS[1];
    protected static final Guid volumeId = GUIDS[2];
    protected static final Guid brickId = GUIDS[0];
    protected static final String brickDir = "/export/vol1/brick1";
    protected static final String brickName = "server:" + brickDir;
    protected static final String volumeName = "AnyVolume";
    protected static final Integer BRICK_PORT = 49152;
    protected static final String BRICK_MNT_OPT = "rw";

    protected IMocksControl control;

    public GlusterTestHelper(IMocksControl control) {
        super();
        this.control = control;
    }

    protected GlusterBrickEntity getBrickEntity(int index, boolean hasDetails) {
        GlusterBrickEntity entity = control.createMock(GlusterBrickEntity.class);

        expect(entity.getId()).andReturn(GUIDS[index]).anyTimes();
        expect(entity.getServerId()).andReturn(serverId).anyTimes();
        expect(entity.getBrickDirectory()).andReturn(GlusterTestHelper.brickDir).anyTimes();
        expect(entity.getQualifiedName()).andReturn(GlusterTestHelper.brickName).anyTimes();
        expect(entity.getVolumeId()).andReturn(volumeId).anyTimes();
        if (hasDetails) {
            BrickDetails brickDetails = control.createMock(BrickDetails.class);
            BrickProperties brickProps = control.createMock(BrickProperties.class);
            MemoryStatus memStatus = control.createMock(MemoryStatus.class);
            MallInfo mallInfo = control.createMock(MallInfo.class);
            expect(mallInfo.getArena()).andReturn(888);
            expect(brickProps.getMntOptions()).andReturn(GlusterTestHelper.BRICK_MNT_OPT).anyTimes();
            expect(brickProps.getPort()).andReturn(GlusterTestHelper.BRICK_PORT).anyTimes();
            expect(brickDetails.getMemoryStatus()).andReturn(memStatus);
            expect(memStatus.getMallInfo()).andReturn(mallInfo);
            expect(brickDetails.getBrickProperties()).andReturn(brickProps).anyTimes();
            expect(entity.getBrickDetails()).andReturn(brickDetails).anyTimes();
        }

        return entity;
    }

    protected GlusterVolumeEntity getVolumeEntity(int index) {
        List<GlusterVolumeEntity> volumesList = new ArrayList<>();

        GlusterVolumeEntity entity1 = control.createMock(GlusterVolumeEntity.class);
        expect(entity1.getId()).andReturn(volumeId).anyTimes();
        expect(entity1.getName()).andReturn(volumeName).anyTimes();
        expect(entity1.getClusterId()).andReturn(clusterId).anyTimes();
        volumesList.add(entity1);

        GlusterAsyncTask task = new GlusterAsyncTask();
        task.setType(GlusterTaskType.REMOVE_BRICK);
        task.setStatus(JobExecutionStatus.FINISHED);
        task.setTaskId(GUIDS[2]);
        GlusterVolumeEntity entity2 = control.createMock(GlusterVolumeEntity.class);
        expect(entity2.getId()).andReturn(volumeId).anyTimes();
        expect(entity2.getName()).andReturn(volumeName).anyTimes();
        expect(entity2.getClusterId()).andReturn(clusterId).anyTimes();
        expect(entity2.getAsyncTask()).andReturn(task).anyTimes();
        volumesList.add(entity2);

        return volumesList.get(index);
    }

    protected GlusterVolumeAdvancedDetails getVolumeAdvancedDetailsEntity() {
        GlusterVolumeAdvancedDetails entity = control.createMock(GlusterVolumeAdvancedDetails.class);

        BrickDetails brickDetails = control.createMock(BrickDetails.class);
        BrickProperties brickProps = control.createMock(BrickProperties.class);
        expect(brickProps.getMntOptions()).andReturn(BRICK_MNT_OPT).anyTimes();
        expect(brickProps.getPort()).andReturn(BRICK_PORT).anyTimes();
        expect(brickDetails.getBrickProperties()).andReturn(brickProps).anyTimes();
        List<BrickDetails> brickDetailsList = Collections.singletonList(brickDetails);
        expect(entity.getBrickDetails()).andReturn(brickDetailsList).anyTimes();
        return entity;
    }


}
