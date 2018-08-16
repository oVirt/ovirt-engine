package org.ovirt.engine.core.bll.gluster;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.core.Is.is;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.businessentities.StorageDomainStatic;
import org.ovirt.engine.core.common.businessentities.gluster.BrickDetails;
import org.ovirt.engine.core.common.businessentities.gluster.BrickProperties;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterLocalVolumeInfo;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.interfaces.VDSBrokerFrontend;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogable;
import org.ovirt.engine.core.dao.StorageDomainStaticDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.gluster.GlusterBrickDao;
import org.ovirt.engine.core.utils.InjectorExtension;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith({ MockitoExtension.class, MockConfigExtension.class, InjectorExtension.class })
public class GlusterThinDeviceServiceTest {
    private static final Integer LOW_SPACE_THRESHOLD_VALUE = 70;
    private static final Guid serverId = new Guid("e03104a4-399f-44e0-b61b-73bac390e49c");
    private static final Guid brickId = new Guid("929fa2ed-54a3-4500-bc34-666833797be3");
    private static final Guid sdId = new Guid("4d43b701-a689-45cf-b390-f64e5f883682");

    @Mock
    private AuditLogDirector auditLogDirector;

    @Mock
    protected GlusterBrickDao brickDao;

    @Mock
    protected StorageDomainStaticDao storageDomainStaticDao;

    @Mock
    protected VdsDao vdsDao;

    @Mock
    private VDSBrokerFrontend resourceManager;

    @Spy
    @InjectMocks
    private GlusterThinDeviceService thinDeviceService;

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(MockConfigDescriptor.of(ConfigValues.GlusterVolumeFreeSpaceThresholdInPercent, LOW_SPACE_THRESHOLD_VALUE));
    }

    GlusterBrickEntity brick;

    @BeforeEach
    public void setUp() {
        brick = getBrick(brickId, "test", 5000.100, 1000.500);
    }

    private GlusterBrickEntity getBrick(Guid id, String device, Double confirmedTotalSize,
            Double confirmedFreeSize) {
        GlusterBrickEntity brick = new GlusterBrickEntity();
        brick.setId(id);
        BrickProperties brickProperties = new BrickProperties();
        brickProperties.setConfirmedTotalSize(confirmedTotalSize);
        brickProperties.setConfirmedFreeSize(confirmedFreeSize);
        brickProperties.setVdoSavings(80);
        brickProperties.setDevice(device);

        BrickDetails brickDetails = new BrickDetails();
        brickDetails.setBrickProperties(brickProperties);

        brick.setServerId(serverId);
        brick.setBrickDetails(brickDetails);
        if (!brickId.equals(id)) {
            doReturn(brick).when(brickDao).getById(id);
        }

        return brick;
    }

    @Test
    public void testSetConfirmedSizeNull() {

        BrickProperties brickProperties =
                thinDeviceService.setConfirmedSize(Collections.emptyMap(), brick, brick.getBrickProperties());
        assertThat(brickProperties.getConfirmedTotalSize(), closeTo(5000.100, 0.001));
        assertThat(brickProperties.getConfirmedFreeSize(), closeTo(1000.500, 0.001));
    }

    @Test
    public void testSetConfirmedSize() {
        GlusterLocalVolumeInfo volumeInfo = mock(GlusterLocalVolumeInfo.class);
        when(volumeInfo.getAvailableThinSizeForDevice("test")).thenReturn(Optional.of(9000000L));
        when(volumeInfo.getTotalThinSizeForDevice("test")).thenReturn(Optional.of(36000000L));

        Map<Guid, GlusterLocalVolumeInfo> volumeInfoMap = new HashMap<>();
        volumeInfoMap.put(serverId, volumeInfo);

        BrickProperties brickProperties =
                thinDeviceService.setConfirmedSize(volumeInfoMap, brick, brick.getBrickProperties());
        assertThat(brickProperties.getConfirmedFreeSize(), closeTo(8.5, 0.1));
        assertThat(brickProperties.getConfirmedTotalSize(), closeTo(34.3, 0.1));
    }

    @Test
    public void testConfirmedVolumeCapacity() {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 300.1, 500.1));
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);

        long volumeConfirmedCapacity = thinDeviceService.calculateConfirmedVolumeCapacity(volumeEntity);
        long value = (long) (500.1 * SizeConverter.BYTES_IN_MB);
        assertEquals(value, volumeConfirmedCapacity);

        int volumeConfirmedSavings = thinDeviceService.calculateVolumeSavings(volumeEntity);
        assertEquals(80, volumeConfirmedSavings);
    }

    @Test
    public void testConfirmedVolumeCapacityForDist() {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 300.1, 500.1));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 300.1, 400.2));
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);

        long volumeConfirmedCapacity = thinDeviceService.calculateConfirmedVolumeCapacity(volumeEntity);
        long expectedValue = (long) (500.1 * SizeConverter.BYTES_IN_MB) + (long) (400.2 * SizeConverter.BYTES_IN_MB);
        assertEquals(expectedValue, volumeConfirmedCapacity);

        int volumeConfirmedSavings = thinDeviceService.calculateVolumeSavings(volumeEntity);
        assertEquals(80, volumeConfirmedSavings);
    }

    @Test
    public void testConfirmedVolumeCapacityForReplica() {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 100.1, 500.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 300.1, 400.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test3", 300.1, 200.2));
        volumeEntity.setVolumeType(GlusterVolumeType.REPLICATE);
        volumeEntity.setReplicaCount(3);

        long volumeConfirmedCapacity = thinDeviceService.calculateConfirmedVolumeCapacity(volumeEntity);
        long expectedValue = (long) (200.2 * SizeConverter.BYTES_IN_MB);
        assertEquals(expectedValue, volumeConfirmedCapacity);

        int volumeConfirmedSavings = thinDeviceService.calculateVolumeSavings(volumeEntity);
        assertEquals(80, volumeConfirmedSavings);
    }

    @Test
    public void testConfirmedVolumeCapacityForDistributedReplica() {
        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 300.1, 500.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test2", 300.1, 400.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test3", 300.1, 200.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test4", 300.1, 600.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test5", 300.1, 300.2));
        volumeEntity.addBrick(getBrick(Guid.newGuid(), "test6", 300.1, 400.2));
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTED_REPLICATE);
        volumeEntity.setReplicaCount(3);

        long volumeConfirmedCapacity = thinDeviceService.calculateConfirmedVolumeCapacity(volumeEntity);
        long expectedValue = (long) (200.2 * SizeConverter.BYTES_IN_MB + 300.2 * SizeConverter.BYTES_IN_MB);
        assertEquals(expectedValue, volumeConfirmedCapacity);

        int volumeConfirmedSavings = thinDeviceService.calculateVolumeSavings(volumeEntity);
        assertEquals(80, volumeConfirmedSavings);
    }

    @Test
    public void testSendLowConfirmedSpaceEvent() {

        StorageDomainStatic sd = new StorageDomainStatic();
        sd.setId(sdId);
        sd.setWarningLowConfirmedSpaceIndicator(LOW_SPACE_THRESHOLD_VALUE);
        doReturn(sd).when(storageDomainStaticDao).get(sdId);

        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(brick);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        doReturn(brick).when(brickDao).getById(brickId);

        thinDeviceService.sendLowConfirmedSpaceEvent(SizeConverter.BYTES_IN_MB * 3400, volumeEntity, Collections.singletonList(sdId));

        ArgumentCaptor<AuditLogable> event = ArgumentCaptor.forClass(AuditLogable.class);
        verify(auditLogDirector, times(1)).log(event.capture(), eq(AuditLogType.IRS_CONFIRMED_DISK_SPACE_LOW));
        assertThat(event.getValue().getCustomValues().get("diskspace"), is("3"));
        verify(auditLogDirector, times(1)).log(event.capture(), eq(AuditLogType.GLUSTER_VOLUME_CONFIRMED_SPACE_LOW));

    }

    @Test
    public void testDontSendLowConfirmedSpaceEvent() {

        StorageDomainStatic sd = new StorageDomainStatic();
        sd.setId(sdId);
        sd.setWarningLowConfirmedSpaceIndicator(70);
        doReturn(sd).when(storageDomainStaticDao).get(sdId);

        GlusterVolumeEntity volumeEntity = new GlusterVolumeEntity();
        volumeEntity.addBrick(brick);
        volumeEntity.setVolumeType(GlusterVolumeType.DISTRIBUTE);
        doReturn(brick).when(brickDao).getById(brickId);

        thinDeviceService.sendLowConfirmedSpaceEvent(SizeConverter.BYTES_IN_MB * 3750, volumeEntity, Collections.singletonList(sdId));

        ArgumentCaptor<AuditLogable> event = ArgumentCaptor.forClass(AuditLogable.class);
        verify(auditLogDirector, times(0)).log(event.capture(), eq(AuditLogType.IRS_CONFIRMED_DISK_SPACE_LOW));
        verify(auditLogDirector, times(0)).log(event.capture(), eq(AuditLogType.GLUSTER_VOLUME_CONFIRMED_SPACE_LOW));
    }
}
