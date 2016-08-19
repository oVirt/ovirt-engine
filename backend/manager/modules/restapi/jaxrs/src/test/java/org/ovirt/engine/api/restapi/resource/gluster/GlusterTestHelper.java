package org.ovirt.engine.api.restapi.resource.gluster;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    protected GlusterBrickEntity getBrickEntity(int index, boolean hasDetails) {
        GlusterBrickEntity entity = mock(GlusterBrickEntity.class);

        when(entity.getId()).thenReturn(GUIDS[index]);
        when(entity.getServerId()).thenReturn(serverId);
        when(entity.getBrickDirectory()).thenReturn(GlusterTestHelper.brickDir);
        when(entity.getQualifiedName()).thenReturn(GlusterTestHelper.brickName);
        when(entity.getVolumeId()).thenReturn(volumeId);
        if (hasDetails) {
            BrickDetails brickDetails = mock(BrickDetails.class);
            BrickProperties brickProps = mock(BrickProperties.class);
            MemoryStatus memStatus = mock(MemoryStatus.class);
            MallInfo mallInfo = mock(MallInfo.class);
            when(mallInfo.getArena()).thenReturn(888);
            when(brickProps.getMntOptions()).thenReturn(GlusterTestHelper.BRICK_MNT_OPT);
            when(brickProps.getPort()).thenReturn(GlusterTestHelper.BRICK_PORT);
            when(brickDetails.getMemoryStatus()).thenReturn(memStatus);
            when(memStatus.getMallInfo()).thenReturn(mallInfo);
            when(brickDetails.getBrickProperties()).thenReturn(brickProps);
            when(entity.getBrickDetails()).thenReturn(brickDetails);
        }

        return entity;
    }

    protected GlusterVolumeEntity getVolumeEntity(int index) {
        List<GlusterVolumeEntity> volumesList = new ArrayList<>();

        GlusterVolumeEntity entity1 = mock(GlusterVolumeEntity.class);
        when(entity1.getId()).thenReturn(volumeId);
        when(entity1.getName()).thenReturn(volumeName);
        when(entity1.getClusterId()).thenReturn(clusterId);
        volumesList.add(entity1);

        GlusterAsyncTask task = new GlusterAsyncTask();
        task.setType(GlusterTaskType.REMOVE_BRICK);
        task.setStatus(JobExecutionStatus.FINISHED);
        task.setTaskId(GUIDS[2]);
        GlusterVolumeEntity entity2 = mock(GlusterVolumeEntity.class);
        when(entity2.getId()).thenReturn(volumeId);
        when(entity2.getName()).thenReturn(volumeName);
        when(entity2.getClusterId()).thenReturn(clusterId);
        when(entity2.getAsyncTask()).thenReturn(task);
        volumesList.add(entity2);

        return volumesList.get(index);
    }

    protected GlusterVolumeAdvancedDetails getVolumeAdvancedDetailsEntity() {
        GlusterVolumeAdvancedDetails entity = mock(GlusterVolumeAdvancedDetails.class);

        BrickDetails brickDetails = mock(BrickDetails.class);
        BrickProperties brickProps = mock(BrickProperties.class);
        when(brickProps.getMntOptions()).thenReturn(BRICK_MNT_OPT);
        when(brickProps.getPort()).thenReturn(BRICK_PORT);
        when(brickDetails.getBrickProperties()).thenReturn(brickProps);
        List<BrickDetails> brickDetailsList = Collections.singletonList(brickDetails);
        when(entity.getBrickDetails()).thenReturn(brickDetailsList);
        return entity;
    }


}
