package org.ovirt.engine.api.restapi.resource.gluster;

import static org.easymock.EasyMock.expect;

import java.util.Arrays;
import java.util.List;

import org.easymock.IMocksControl;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeAdvancedDetails;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.compat.Guid;

public class GlusterTestHelper {

    protected static final Guid[] GUIDS = { new Guid("00000000-0000-0000-0000-000000000000"),
        new Guid("11111111-1111-1111-1111-111111111111"),
        new Guid("22222222-2222-2222-2222-222222222222"),
        new Guid("33333333-3333-3333-3333-333333333333") };
    protected static final Guid clusterId = GUIDS[0];
    protected static final Guid serverId = GUIDS[1];
    protected static final Guid volumeId = GUIDS[2];
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


    protected GlusterVolumeEntity getVolumeEntity(int index) {
        GlusterVolumeEntity entity = control.createMock(GlusterVolumeEntity.class);
        expect(entity.getId()).andReturn(volumeId).anyTimes();
        expect(entity.getName()).andReturn(volumeName).anyTimes();
        expect(entity.getClusterId()).andReturn(clusterId).anyTimes();
        return entity;
    }

    protected GlusterVolumeAdvancedDetails getVolumeAdvancedDetailsEntity(int index) {
        GlusterVolumeAdvancedDetails entity = control.createMock(GlusterVolumeAdvancedDetails.class);

        BrickDetails brickDetails = control.createMock(BrickDetails.class);
        BrickProperties brickProps = control.createMock(BrickProperties.class);
        expect(brickProps.getMntOptions()).andReturn(BRICK_MNT_OPT).anyTimes();
        expect(brickProps.getPort()).andReturn(BRICK_PORT).anyTimes();
        expect(brickDetails.getBrickProperties()).andReturn(brickProps).anyTimes();
        List<BrickDetails> brickDetailsList = Arrays.asList(brickDetails);
        expect(entity.getBrickDetails()).andReturn(brickDetailsList).anyTimes();
        return entity;
    }


}
