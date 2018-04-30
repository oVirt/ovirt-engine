package org.ovirt.engine.core.bll.network.host;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.lang.RandomStringUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentMatcher;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.HostDevice;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VmDevice;
import org.ovirt.engine.core.common.businessentities.VmDeviceGeneralType;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.HostDeviceDao;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.dao.VmDeviceDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.RandomUtils;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class VfSchedulerImplTest {

    @Mock
    private NetworkDeviceHelper networkDeviceHelper;

    @Mock
    private InterfaceDao interfaceDao;

    @Mock
    private VdsDao hostDao;

    @Mock
    private HostDeviceDao hostDeviceDao;

    @Mock
    private VmDeviceDao vmDeviceDao;

    @Mock
    private NetworkDao networkDao;

    @Mock
    private Guid vmId;

    @Mock
    private Guid hostId;

    @Mock
    private Guid dataCenterId;

    @Mock
    private VDS host;

    private VfSchedulerImpl vfScheduler;

    private Map<Guid, String> expectedVnicToVfMap;

    @BeforeEach
    public void setUp() {
        when(hostDao.get(hostId)).thenReturn(host);
        when(host.getStoragePoolId()).thenReturn(dataCenterId);

        vfScheduler = new VfSchedulerImpl(networkDao, interfaceDao, hostDao, hostDeviceDao, vmDeviceDao, networkDeviceHelper);
        expectedVnicToVfMap = new HashMap<>();
    }

    @Test
    public void hostNotHaveSriovNics() {
        VmNetworkInterface vnic = mockVnic(true);
        assertHostNotValid(Collections.singletonList(vnic), Collections.singletonList(vnic.getName()));
    }

    @Test
    public void hostNicNotHaveVfsOnSriovNic() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 0, true, false, false, false);

        assertHostNotValid(Collections.singletonList(vnic), Collections.singletonList(vnic.getName()));
    }

    @Test
    public void hostNicNotHaveNetworkInSriovConfig() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 5, false, false, false, true);

        assertHostNotValid(Collections.singletonList(vnic), Collections.singletonList(vnic.getName()));
    }

    @Test
    public void validNetworkInSriovConfig() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 5, false, true, false, true);

        assertHostValid(Collections.singletonList(vnic));
    }

    @Test
    public void validLabelInSriovConfig() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 1, false, false, true, true);

        assertHostValid(Collections.singletonList(vnic));
    }

    @Test
    public void validAllNetworksAllowed() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 5, true, false, false, true);

        assertHostValid(Collections.singletonList(vnic));
    }

    @Test
    public void hostNicNotHaveFreeVfs() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 8, true, false, false, false);

        assertHostNotValid(Collections.singletonList(vnic), Collections.singletonList(vnic.getName()));
    }

    @Test
    public void hostNicHaveOneFreeVfWhichShareIommuGroup() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 1, true, false, false, true, true, false);

        assertHostNotValid(Collections.singletonList(vnic), Collections.singletonList(vnic.getName()));
    }

    @Test
    public void hostNicHaveOneFreeVfWhichShouldBeDirectlyPassthrough() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 1, true, false, false, true, false, true);

        assertHostNotValid(Collections.singletonList(vnic), Collections.singletonList(vnic.getName()));
    }

    @Test
    public void hostNicHaveTwoFreeVfOneShouldBeDirectlyPassthrough() {
        VmNetworkInterface vnic = mockVnic(true);
        List<HostDevice> vfs =
                initHostWithOneVfsConfig(Collections.singletonList(vnic), 2, true, false, false, true, false, true);
        HostDevice freeVf = vfs.get(0);
        mockVfDirectlyAttached(false, freeVf);
        expectedVnicToVfMap.put(vnic.getId(), freeVf.getDeviceName());

        assertHostValid(Collections.singletonList(vnic));
    }

    @Test
    public void validVnicNotPlugged() {
        VmNetworkInterface vnic = mockVnic(true);
        when(vnic.isPlugged()).thenReturn(false);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 0, true, false, false, false);

        assertHostValid(Collections.singletonList(vnic));
    }

    @Test
    public void multipleVnicsValid() {
        multipleVnicCommonTest(true);
    }

    @Test
    public void multipleVnicsNotValid() {
        multipleVnicCommonTest(false);
    }

    private void multipleVnicCommonTest(boolean allNicsValid) {
        VmNetworkInterface vnic1 = mockVnic(true, "net1");
        VmNetworkInterface vnic2 = mockVnic(true, "net2");
        VmNetworkInterface vnic3 = mockVnic(false);
        VmNetworkInterface vnic4 = mockVnic(false);

        HostNicVfsConfig hostNicVfsConfig1 = new HostNicVfsConfig();
        updateVfsConfig(hostNicVfsConfig1, vnic1, true, false, true);

        HostNicVfsConfig hostNicVfsConfig2 = new HostNicVfsConfig();
        updateVfsConfig(hostNicVfsConfig2, vnic2, false, allNicsValid, allNicsValid);

        mockVfsConfigsOnHost(Arrays.asList(hostNicVfsConfig1, hostNicVfsConfig2));

        List<VmNetworkInterface> allVnics = Arrays.asList(vnic1, vnic2, vnic3, vnic4);

        if (allNicsValid) {
            assertHostValid(allVnics);
        } else {
            assertHostNotValid(allVnics, Collections.singletonList(vnic2.getName()));
        }
    }

    @Test
    public void multipleVfsConfigsFirstValid() {
        multipleVfsConfigsCommon(true);
    }

    @Test
    public void multipleVfsConfigsLastValid() {
        multipleVfsConfigsCommon(false);
    }

    private void multipleVfsConfigsCommon(boolean firstValid) {
        VmNetworkInterface vnic = mockVnic(true);

        HostNicVfsConfig hostNicVfsConfig1 = new HostNicVfsConfig();
        updateVfsConfig(hostNicVfsConfig1, vnic, firstValid, false, firstValid);

        HostNicVfsConfig hostNicVfsConfig2 = new HostNicVfsConfig();
        updateVfsConfig(hostNicVfsConfig2, vnic, !firstValid, false, !firstValid);

        mockVfsConfigsOnHost(Arrays.asList(hostNicVfsConfig1, hostNicVfsConfig2));

        assertHostValid(Collections.singletonList(vnic));
    }

    @Test
    public void cleanVmDataTest() {
        VmNetworkInterface vnic = mockVnic(true);
        initHostWithOneVfsConfig(Collections.singletonList(vnic), 1, true, false, false, true);

        assertHostValid(Collections.singletonList(vnic));

        vfScheduler.cleanVmData(vmId);
        assertNull(vfScheduler.getVnicToVfMap(vmId, hostId));
    }

    @Test
    public void findFreeVfForVnicNoFreeVfTest() {
        findFreeVfForVnicCommon(false);
    }

    @Test
    public void findFreeVfForVnicTest() {
        findFreeVfForVnicCommon(true);
    }

    private void findFreeVfForVnicCommon(boolean existFreeVf) {
        VmNetworkInterface vnic = mockVnic(true, "net1");

        HostNicVfsConfig hostNicVfsConfig = new HostNicVfsConfig();
        updateVfsConfig(hostNicVfsConfig, vnic, true, false, existFreeVf);

        mockVfsConfigsOnHost(Collections.singletonList(hostNicVfsConfig));

        String freeVf = vfScheduler.findFreeVfForVnic(hostId, createNetwork(vnic.getNetworkName()), vmId);

        if (existFreeVf) {
            assertNotNull(freeVf);
        } else {
            assertNull(freeVf);
        }
    }

    private List<HostDevice> initHostWithOneVfsConfig(List<VmNetworkInterface> passthroughVnics,
            int numOfVfs,
            boolean allNetworksAllowed,
            boolean networkInSriovConfig,
            boolean labelInSriovConfig,
            boolean hasFreeVf,
            boolean freeVfShareIommuGroup,
            boolean vfDirectlyAttached) {
        HostNicVfsConfig hostNicVfsConfig = new HostNicVfsConfig();
        List<HostDevice> vfs = passthroughVnics.stream().map(vnic -> updateVfsConfig(hostNicVfsConfig,
                vnic,
                numOfVfs,
                allNetworksAllowed,
                networkInSriovConfig,
                labelInSriovConfig,
                hasFreeVf,
                freeVfShareIommuGroup,
                vfDirectlyAttached)).collect(Collectors.toList());
        mockVfsConfigsOnHost(Collections.singletonList(hostNicVfsConfig));
        return vfs;
    }

    private void initHostWithOneVfsConfig(List<VmNetworkInterface> passthroughVnics,
            int numOfVfs,
            boolean allNetworksAllowed,
            boolean networkInSriovConfig,
            boolean labelInSriovConfig,
            boolean hasFreeVf) {
        initHostWithOneVfsConfig(passthroughVnics,
                numOfVfs,
                allNetworksAllowed,
                networkInSriovConfig,
                labelInSriovConfig,
                hasFreeVf,
                false,
                false);
    }

    private void validateVnics(List<VmNetworkInterface> vnics, List<String> excetedProblematicVnics) {
        List<String> problematicVnics =
                vfScheduler.validatePassthroughVnics(vmId, hostId, vnics);
        assertEquals(excetedProblematicVnics, problematicVnics);
    }

    private VmNetworkInterface mockVnic(boolean passthrough, String networkName) {
        VmNetworkInterface vnic = mock(VmNetworkInterface.class);
        when(vnic.getId()).thenReturn(Guid.newGuid());
        when(vnic.getName()).thenReturn(getRandomString());
        when(vnic.isPassthrough()).thenReturn(passthrough);
        Network network = createNetwork(networkName);
        when(vnic.getNetworkName()).thenReturn(network.getName());
        when(vnic.isPlugged()).thenReturn(true);
        when(vnic.getVmId()).thenReturn(vmId);
        return vnic;
    }

    private VmNetworkInterface mockVnic(boolean passthrough) {
        return mockVnic(passthrough, getRandomString());
    }

    private void assertHostNotValid(List<VmNetworkInterface> vnics, List<String> exceptedProblematicVnics) {
        validateVnics(vnics, exceptedProblematicVnics);
        validateVnicToVfMap();
    }

    private void assertHostValid(List<VmNetworkInterface> vnics) {
        validateVnics(vnics, new ArrayList<>());
        validateVnicToVfMap();
    }

    private void validateVnicToVfMap() {
        Map<Guid, String> vnicToVfMap = vfScheduler.getVnicToVfMap(vmId, hostId);
        vnicToVfMap = vnicToVfMap == null ? new HashMap<>() : vnicToVfMap;
        assertEquals(expectedVnicToVfMap, vnicToVfMap);
    }

    private HostDevice updateVfsConfig(HostNicVfsConfig hostNicVfsConfig, VmNetworkInterface vnic,
            int numOfVfs,
            boolean allNetworksAllowed,
            boolean vnicNetworkInSriovConfig,
            boolean vnicLabelInSriovConfig,
            boolean hasFreeVf,
            boolean freeVfShareIommuGroup,
            boolean vfDirectlyAttached) {
        hostNicVfsConfig.setNicId(Guid.newGuid());
        hostNicVfsConfig.setNumOfVfs(numOfVfs);
        hostNicVfsConfig.setAllNetworksAllowed(allNetworksAllowed);
        updateVfsConfigNetworks(hostNicVfsConfig, vnic, vnicNetworkInSriovConfig);
        updateVfsConfigLabels(hostNicVfsConfig, vnic, vnicLabelInSriovConfig);

        VdsNetworkInterface nic = new VdsNetworkInterface();
        nic.setId(hostNicVfsConfig.getNicId());
        when(getNic(hostNicVfsConfig)).thenReturn(nic);

        HostDevice vf = null;
        if (hasFreeVf) {
            vf = createFreeVf(hostNicVfsConfig);
            mockVfShareIommuGroup(vf, freeVfShareIommuGroup);

            if (!freeVfShareIommuGroup && (allNetworksAllowed || vnicNetworkInSriovConfig || vnicLabelInSriovConfig)) {
                if (!vfDirectlyAttached) {
                    expectedVnicToVfMap.put(vnic.getId(), vf.getDeviceName());
                }
                mockVfDirectlyAttached(vfDirectlyAttached, vf);
            }
        }

        return vf;
    }

    private void mockVfDirectlyAttached(boolean vfDirectlyAttached, HostDevice vf) {
        when(vmDeviceDao.getVmDeviceByVmIdTypeAndDevice(vmId, VmDeviceGeneralType.HOSTDEV, vf.getName()))
                .thenReturn(vfDirectlyAttached ? Collections.singletonList(new VmDevice()) : Collections.emptyList());
    }

    private void updateVfsConfig(HostNicVfsConfig hostNicVfsConfig,
            VmNetworkInterface vnic,
            boolean allNetworksAllowed,
            boolean vnicNetworkInSriovConfig,
            boolean hasFreeVf) {
        updateVfsConfig(hostNicVfsConfig,
                vnic,
                1,
                allNetworksAllowed,
                vnicNetworkInSriovConfig,
                false,
                hasFreeVf,
                false,
                false);
    }

    private void mockVfShareIommuGroup(HostDevice vf, boolean share) {
        vf.setIommuGroup(RandomUtils.instance().nextInt());
        List<HostDevice> devices = new ArrayList<>();
        devices.add(vf);

        if (share) {
            HostDevice extraIommuDevice = new HostDevice();
            extraIommuDevice.setHostId(vf.getHostId());
            extraIommuDevice.setIommuGroup(vf.getIommuGroup());
            devices.add(extraIommuDevice);
        }
        when(hostDeviceDao.getHostDevicesByHostIdAndIommuGroup(vf.getHostId(), vf.getIommuGroup())).thenReturn(devices);
    }

    private void updateVfsConfigLabels(HostNicVfsConfig hostNicVfsConfig,
            VmNetworkInterface vnic,
            boolean vnicLabelInSriovConfig) {
        if (hostNicVfsConfig.getNetworkLabels() == null) {
            hostNicVfsConfig.setNetworkLabels(new HashSet<>());
        }

        if (vnicLabelInSriovConfig) {
            hostNicVfsConfig.getNetworkLabels().add(getVnicNetwork(vnic).getLabel());
        }
    }

    private void updateVfsConfigNetworks(HostNicVfsConfig hostNicVfsConfig,
            VmNetworkInterface vnic,
            boolean vnicNetworkInSriovConfig) {
        if (hostNicVfsConfig.getNetworks() == null) {
            hostNicVfsConfig.setNetworks(new HashSet<>());
        }

        if (vnicNetworkInSriovConfig) {
            hostNicVfsConfig.getNetworks().add(getVnicNetwork(vnic).getId());
        }
    }

    private Network createNetwork(String networkName) {
        Network network = new Network();
        network.setId(Guid.newGuid());
        network.setName(networkName);
        network.setLabel(getRandomString());

        when(networkDao.getByNameAndDataCenter(network.getName(), dataCenterId)).thenReturn(network);

        return network;
    }

    private Network getVnicNetwork(VmNetworkInterface vnic) {
        return networkDao.getByNameAndDataCenter(vnic.getNetworkName(), dataCenterId);
    }

    private HostDevice createVf() {
        HostDevice hostDevice = new HostDevice();
        hostDevice.setHostId(hostId);
        hostDevice.setDeviceName(getRandomString());
        return hostDevice;
    }

    private HostDevice createFreeVf(HostNicVfsConfig hostNicVfsConfig) {
        HostDevice vf = createVf();
        ArgumentMatcher<List<String>> matchNotContainVf = argVf -> argVf == null || !argVf.contains(vf.getName());
        when(networkDeviceHelper.getFreeVf(eq(getNic(hostNicVfsConfig)), argThat(matchNotContainVf))).thenReturn(vf);
        return vf;
    }

    private void mockVfsConfigsOnHost(List<HostNicVfsConfig> vfsConfigs) {
        when(networkDeviceHelper.getHostNicVfsConfigsWithNumVfsDataByHostId(hostId)).thenReturn(vfsConfigs);
    }

    private VdsNetworkInterface getNic(HostNicVfsConfig hostNicVfsConfig) {
        return interfaceDao.get(hostNicVfsConfig.getNicId());
    }

    private String getRandomString() {
        return RandomStringUtils.random(10);
    }
}
