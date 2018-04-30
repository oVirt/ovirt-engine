package org.ovirt.engine.core.bll.validator;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.bll.network.host.NetworkDeviceHelper;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.Network;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.dao.network.NetworkDao;
import org.ovirt.engine.core.utils.InjectedMock;
import org.ovirt.engine.core.utils.InjectorExtension;

@ExtendWith({MockitoExtension.class, InjectorExtension.class })
@MockitoSettings(strictness = Strictness.LENIENT)
public class VfsConfigValidatorTest {

    private static final String NETWORK_NAME = "net";

    private static final String NIC_NAME = "nic";

    private static final int NUM_OF_VFS = 5;

    private static final Guid NIC_ID = Guid.newGuid();

    private static final Guid NETWORK_ID = Guid.newGuid();

    private static final String LABEL = "lbl";

    @Mock
    private VdsNetworkInterface nic;

    @Mock
    private Network network;

    @Mock
    private HostNicVfsConfig oldVfsConfig;

    @Mock
    @InjectedMock
    public InterfaceDao interfaceDao;

    @Mock
    private NetworkDeviceHelper networkDeviceHelper;

    @Mock
    @InjectedMock
    public NetworkDao networkDao;

    private VfsConfigValidator validator;

    @BeforeEach
    public void createValidator() {
        validator = new VfsConfigValidator(NIC_ID, oldVfsConfig);
    }

    @Test
    public void nicSriovEnabled() {
        simulateNicExists();
        assertThat(validator.nicSriovEnabled(), isValid());
    }

    @Test
    public void nicSriovNotEnabled() {
        simulateNicExists();
        validator.setOldVfsConfig(null);
        assertThat(validator.nicSriovEnabled(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName())));
    }

    private void simulateNicExists() {
        when(interfaceDao.get(NIC_ID)).thenReturn(nic);
        when(nic.getName()).thenReturn(NIC_NAME);
    }

    @Test
    public void numOfVfsValidLessThanMax() {
        numOfVfsInRangeTest(NUM_OF_VFS, NUM_OF_VFS + 1);
        assertThat(validator.nicSriovEnabled(), isValid());
    }

    @Test
    public void numOfVfsValidEqualsMax() {
        numOfVfsInRangeTest(NUM_OF_VFS, NUM_OF_VFS);
        assertThat(validator.nicSriovEnabled(), isValid());
    }

    @Test
    public void numOfVfsValidZero() {
        numOfVfsInRangeTest(0, NUM_OF_VFS);
        assertThat(validator.nicSriovEnabled(), isValid());
    }

    @Test
    public void numOfVfsNotValidBiggerThanMax() {
        numOfVfsInRangeTest(NUM_OF_VFS, NUM_OF_VFS - 1);
        assertNumOfVfsInValidRange(NUM_OF_VFS);
    }

    @Test
    public void numOfVfsNotValidNegative() {
        numOfVfsInRangeTest(-NUM_OF_VFS, NUM_OF_VFS);
        assertNumOfVfsInValidRange(-NUM_OF_VFS);
    }

    private void numOfVfsInRangeTest(int numOfVfs, int maxNumOfVfs) {
        simulateNicExists();
        when(oldVfsConfig.getMaxNumOfVfs()).thenReturn(maxNumOfVfs);
    }

    private void assertNumOfVfsInValidRange(int numOfVfs) {
        assertThat(validator.numOfVfsInValidRange(numOfVfs),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.NUM_OF_VFS_REPLACEMENT, numOfVfs),
                        String.format(VfsConfigValidator.MAX_NUM_OF_VFS_REPLACEMENT, oldVfsConfig.getMaxNumOfVfs())));
    }

    @Test
    public void notAllVfsAreFree() {
        allVfsAreFreeTest(false);
        assertThat(validator.allVfsAreFree(networkDeviceHelper),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName())));
    }

    @Test
    public void allVfsAreFree() {
        allVfsAreFreeTest(true);
        assertThat(validator.allVfsAreFree(networkDeviceHelper), isValid());
    }

    private void allVfsAreFreeTest(boolean areAllVfsFree) {
        simulateNicExists();
        when(networkDeviceHelper.areAllVfsFree(nic)).thenReturn(areAllVfsFree);

    }

    @Test
    public void settingSpecificNetworksAllowed() {
        settingSpecificNetworksAllowedTest(true);
        assertThat(validator.settingSpecificNetworksAllowed(),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_CANNOT_SET_SPECIFIC_NETWORKS,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName())));
    }

    @Test
    public void settingSpecificNetworksNotAllowed() {
        settingSpecificNetworksAllowedTest(false);
        assertThat(validator.settingSpecificNetworksAllowed(), isValid());
    }

    private void settingSpecificNetworksAllowedTest(boolean isAllNetworksAllowed) {
        simulateNicExists();
        when(oldVfsConfig.isAllNetworksAllowed()).thenReturn(isAllNetworksAllowed);
    }

    @Test
    public void networExists() {
        simulateNicExists();
        simulateNetworkExists();
        assertThat(validator.networkExists(NETWORK_ID), isValid());
    }

    @Test
    public void networNotExist() {
        simulateNicExists();
        assertThat(validator.networkExists(NETWORK_ID),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_NOT_EXIST,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.NETWORK_ID_REPLACEMENT, NETWORK_ID)));
    }

    private void simulateNetworkExists() {
        when(networkDao.get(NETWORK_ID)).thenReturn(network);
        when(network.getName()).thenReturn(NETWORK_NAME);
    }

    @Test
    public void networkNotInVfsConfigValid() {
        networkInVfsConfigCommonTest(false);
        assertThat(validator.networkNotInVfsConfig(NETWORK_ID), isValid());
    }

    @Test
    public void networkNotInVfsConfigNotValid() {
        networkInVfsConfigCommonTest(true);
        assertThat(validator.networkNotInVfsConfig(NETWORK_ID),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_ALREADY_IN_VFS_CONFIG,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.NETWORK_NAME_REPLACEMENT, network.getName())));
    }

    @Test
    public void networkInVfsConfigValid() {
        networkInVfsConfigCommonTest(true);
        assertThat(validator.networkInVfsConfig(NETWORK_ID), isValid());
    }

    @Test
    public void networkInVfsConfigNotValid() {
        networkInVfsConfigCommonTest(false);
        assertThat(validator.networkInVfsConfig(NETWORK_ID),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_NETWORK_NOT_IN_VFS_CONFIG,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.NETWORK_NAME_REPLACEMENT, network.getName())));
    }

    private void networkInVfsConfigCommonTest(boolean inVfsConfig) {
        simulateNicExists();
        simulateNetworkExists();

        Set<Guid> networks = new HashSet<>();

        if (inVfsConfig) {
            networks.add(NETWORK_ID);
        }
        when(oldVfsConfig.getNetworks()).thenReturn(networks);
    }

    @Test
    public void labelNotInVfsConfigValid() {
        labelInVfsConfigCommonTest(false);
        assertThat(validator.labelNotInVfsConfig(LABEL), isValid());
    }

    @Test
    public void labelNotInVfsConfigNotValid() {
        labelInVfsConfigCommonTest(true);
        assertThat(validator.labelNotInVfsConfig(LABEL),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_LABEL_ALREADY_IN_VFS_CONFIG,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.LABEL_REPLACEMENT, LABEL)));
    }

    @Test
    public void labelInVfsConfigNotValid() {
        labelInVfsConfigCommonTest(false);
        assertThat(validator.labelInVfsConfig(LABEL),
                failsWith(EngineMessage.ACTION_TYPE_FAILED_LABEL_NOT_IN_VFS_CONFIG,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.LABEL_REPLACEMENT, LABEL)));
    }

    private void labelInVfsConfigCommonTest(boolean inVfsConfig) {
        simulateNicExists();

        Set<String> labels = new HashSet<>();

        if (inVfsConfig) {
            labels.add(LABEL);
        }
        when(oldVfsConfig.getNetworkLabels()).thenReturn(labels);
    }

}
