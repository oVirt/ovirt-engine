package org.ovirt.engine.core.bll.gluster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalVolumeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.di.InjectorRule;

@RunWith(MockitoJUnitRunner.Silent.class)
public class GlusterThinDeviceServiceTest {
    private static final Guid serverId = new Guid("e03104a4-399f-44e0-b61b-73bac390e49c");
    private static final Guid brickId = new Guid("929fa2ed-54a3-4500-bc34-666833797be3");

    @Rule
    public InjectorRule injectorRule = new InjectorRule();

    @Mock
    protected GlusterBrickDao brickDao;
    @Mock
    protected VdsDao vdsDao;

    @Mock
    private VDSBrokerFrontend resourceManager;

    @Spy
    @InjectMocks
    private GlusterThinDeviceService thinDeviceService;

    GlusterBrickEntity brick;
    BrickProperties brickProperties;

    @Before
    public void setUp() {
        brickProperties = new BrickProperties();
        brickProperties.setConfirmedTotalSize(100.500);
        brickProperties.setConfirmedFreeSize(500.100);
        brickProperties.setDevice("test");

        BrickDetails brickDetails = new BrickDetails();
        brickDetails.setBrickProperties(brickProperties);

        brick = new GlusterBrickEntity();
        brick.setId(brickId);
        brick.setServerId(serverId);
        brick.setBrickDetails(brickDetails);

    }

    @Test
    public void testSetConfirmedSizeNull() {
        brickProperties = thinDeviceService.setConfirmedSize(Collections.emptyMap(), brick, brickProperties);
        assertThat(brickProperties.getConfirmedTotalSize(), closeTo(100.500, 0.001));
        assertThat(brickProperties.getConfirmedFreeSize(), closeTo(500.100, 0.001));
    }

    @Test
    public void testSetConfirmedSize() {
        GlusterLocalVolumeInfo volumeInfo = mock(GlusterLocalVolumeInfo.class);
        when(volumeInfo.getAvailableThinSizeForDevice("test")).thenReturn(Optional.of(9000000L));
        when(volumeInfo.getTotalThinSizeForDevice("test")).thenReturn(Optional.of(36000000L));

        Map<Guid, GlusterLocalVolumeInfo> volumeInfoMap = new HashMap<>();
        volumeInfoMap.put(serverId, volumeInfo);

        brickProperties = thinDeviceService.setConfirmedSize(volumeInfoMap, brick, brickProperties);
        assertThat(brickProperties.getConfirmedFreeSize(), closeTo(8.5, 0.1));
        assertThat(brickProperties.getConfirmedTotalSize(), closeTo(34.3, 0.1));
    }

    @Test
    public void testConfirmedVolumeCapacity() {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(brick);

        when(brickDao.getById(brickId)).thenReturn(brick);

        long volumeConfirmedCapacity = thinDeviceService.calculateConfirmedVolumeCapacity(volumeEntity);
        assertEquals(524392857, volumeConfirmedCapacity);
    }
}
