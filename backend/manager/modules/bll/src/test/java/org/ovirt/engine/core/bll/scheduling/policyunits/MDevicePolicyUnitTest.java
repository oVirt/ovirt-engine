package org.ovirt.engine.core.bll.scheduling.policyunits;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.scheduling.SchedulingContext;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.MDevType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.scheduling.PerHostMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dao.HostDeviceDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class MDevicePolicyUnitTest {

    private static final String MDEV_NAME_1 = "a";

    private static final String MDEV_NAME_2 = "aa";

    private static final String MDEV_NAME_3 = "aaa";

    private static final String NODISPLAY = "nodisplay";

    private static final String MDEV_NAME_NON_EXISTING = "bbb";

    @Mock
    public HostDeviceDao hostDeviceDao;

    @InjectMocks
    public final MDevicePolicyUnit policyUnit = new MDevicePolicyUnit(null, null);

    private VDS vdsWithNoMdevs;

    private VDS vdsWithMdevs;

    private VDS vdsWithNonAvailableMdevs;

    private VDS vdsWithMixedMdevs;

    private List<VDS> vdsList;

    private VM vm;

    @BeforeEach
    public void setUp() {
        vm = new VM();
        vm.setCustomCompatibilityVersion(Version.v4_4);

        vdsWithNoMdevs = new VDS();
        vdsWithNoMdevs.setId(Guid.newGuid());

        vdsWithMdevs = new VDS();
        vdsWithMdevs.setId(Guid.newGuid());

        vdsWithNonAvailableMdevs = new VDS();
        vdsWithNonAvailableMdevs.setId(Guid.newGuid());

        vdsWithMixedMdevs = new VDS();
        vdsWithMixedMdevs.setId(Guid.newGuid());

        vdsList = new ArrayList<>();
        vdsList.add(vdsWithNoMdevs);
        vdsList.add(vdsWithMdevs);
        vdsList.add(vdsWithNonAvailableMdevs);
        vdsList.add(vdsWithMixedMdevs);

        doReturn(hostDevices()).when(hostDeviceDao).getHostDevicesByHostId(vdsWithNoMdevs.getId());
        doReturn(hostDevicesWithAvailableMdevs()).when(hostDeviceDao).getHostDevicesByHostId(vdsWithMdevs.getId());
        doReturn(hostDevicesWithNonAvailableMdevs()).when(hostDeviceDao).getHostDevicesByHostId(vdsWithNonAvailableMdevs.getId());
        doReturn(hostDevicesWithMixedMdevs()).when(hostDeviceDao).getHostDevicesByHostId(vdsWithMixedMdevs.getId());
    }

    @Test
    public void noMdevs() {
        vm.setCustomProperties("key=value");

        List<VDS> result = filter();

        assertSame(vdsList, result);
    }

    @Test
    public void nonExistingMdevs() {
        vm.setCustomProperties(String.format("key=value;mdev_type=%s;key2=value", MDEV_NAME_NON_EXISTING));

        List<VDS> result = filter();

        assertThat(result).isEmpty();
    }

    @Test
    public void oneMdev() {
        vm.setCustomProperties(String.format("key=value;mdev_type=%s;key2=value", MDEV_NAME_1));

        List<VDS> result = filter();

        assertEquals(2, result.size());
        assertThat(result.contains(vdsWithMdevs));
        assertThat(result.contains(vdsWithMixedMdevs));
    }

    @Test
    public void moreMdevs() {
        vm.setCustomProperties(String.format("key=value;mdev_type=%s,%s;key2=value", MDEV_NAME_1, MDEV_NAME_3));

        List<VDS> result = filter();

        assertEquals(2, result.size());
        assertThat(result.contains(vdsWithMdevs));
        assertThat(result.contains(vdsWithMixedMdevs));
    }

    @Test
    public void noDisplay() {
        vm.setCustomProperties(String.format("key=value;mdev_type=%s,%s,%s;key2=value", NODISPLAY, MDEV_NAME_1, MDEV_NAME_3));

        List<VDS> result = filter();

        assertEquals(2, result.size());
        assertThat(result.contains(vdsWithMdevs));
        assertThat(result.contains(vdsWithMixedMdevs));
    }

    private List<VDS> filter() {
        return policyUnit.filter(new SchedulingContext(null, Collections.emptyMap()),
                vdsList,
                vm, mock(PerHostMessages.class));
    }

    private HostDevice hostDevice(MDevType... mdevTypes) {
        HostDevice device = new HostDevice();
        device.setMdevTypes(Arrays.asList(mdevTypes));
        return device;
    }

    private List<HostDevice> hostDevices() {
        return Collections.singletonList(new HostDevice());
    }

    private List<HostDevice> hostDevicesWithAvailableMdevs() {
        return Arrays.asList(hostDevice(mDevType(MDEV_NAME_1, 1), mDevType(MDEV_NAME_2, 0), mDevType(MDEV_NAME_3, 10)));
    }

    private List<HostDevice> hostDevicesWithNonAvailableMdevs() {
        return Arrays.asList(hostDevice(mDevType(MDEV_NAME_1, 0), mDevType(MDEV_NAME_2, 1), mDevType(MDEV_NAME_3, 10)));
    }

    private List<HostDevice> hostDevicesWithMixedMdevs() {
        return Arrays.asList(
                hostDevice(mDevType(MDEV_NAME_1, 0), mDevType(MDEV_NAME_2, 0), mDevType(MDEV_NAME_3, 10)),
                hostDevice(mDevType(MDEV_NAME_1, 0), mDevType(MDEV_NAME_2, 0), mDevType(MDEV_NAME_3, 0)),
                hostDevice(mDevType(MDEV_NAME_1, 1), mDevType(MDEV_NAME_2, 0), mDevType(MDEV_NAME_3, 0))
                );
    }

    private MDevType mDevType(String name, Integer availableInstances) {
        return new MDevType(name, null, availableInstances, null);
    }
}
