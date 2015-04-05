package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.ValidationResult;
import org.ovirt.engine.core.bll.network.host.HostNicVfsConfigHelper;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.network.HostNicVfsConfig;
import org.ovirt.engine.core.common.businessentities.network.VdsNetworkInterface;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.compat.Version;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.dao.network.HostNicVfsConfigDao;
import org.ovirt.engine.core.dao.network.InterfaceDao;
import org.ovirt.engine.core.utils.MockConfigRule;

@RunWith(MockitoJUnitRunner.class)
public class VfsConfigValidatorTest {

    private static final String CLUSTER_VERSION = "7";

    private static final int NUM_OF_VFS = 5;

    private Guid nicId = Guid.newGuid();

    private Guid hostId = Guid.newGuid();

    @Mock
    private VdsNetworkInterface nic;

    @Mock
    private VDS host;

    @Mock
    private HostNicVfsConfig oldVfsConfig;

    @Mock
    private Version version;

    @Mock
    private DbFacade dbFacade;

    @Mock
    private InterfaceDao interfaceDao;

    @Mock
    private HostNicVfsConfigDao vfsConfigDao;

    @Mock
    private VdsDAO vdsDao;

    @Mock
    private HostNicVfsConfigHelper hostNicVfsConfigHelper;

    private VfsConfigValidator validator;

    @Rule
    public MockConfigRule mockConfigRule = new MockConfigRule();

    @Before
    public void setup() {
        createValidator();

        when(dbFacade.getInterfaceDao()).thenReturn(interfaceDao);
        when(dbFacade.getHostNicVfsConfigDao()).thenReturn(vfsConfigDao);
        when(dbFacade.getVdsDao()).thenReturn(vdsDao);
    }

    private void createValidator() {
        validator = spy(new VfsConfigValidator(nicId, oldVfsConfig));
        doReturn(dbFacade).when(validator).getDbFacade();
    }

    @Test
    public void sriovFeatureSupported() {
        sriovFeatureSupportTest(isValid(), true);
    }

    @Test
    public void sriovFeatureNotSupported() {
        sriovFeatureSupportTest(failsWith(VdcBllMessages.ACTION_TYPE_FAILED_SRIOV_FEATURE_NOT_SUPPORTED), false);
    }

    private void sriovFeatureSupportTest(Matcher<ValidationResult> matcher,
            boolean isSupported) {
        simulateNicExists();
        when(nic.getVdsId()).thenReturn(hostId);
        when(vdsDao.get(hostId)).thenReturn(host);
        when(host.getVdsGroupCompatibilityVersion()).thenReturn(version);
        when(version.getValue()).thenReturn(CLUSTER_VERSION);

        mockConfigRule.mockConfigValue(ConfigValues.NetworkSriovSupported, version, isSupported);

        assertThat(validator.sriovFeatureSupported(), matcher);
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NIC_IS_NOT_SRIOV_ENABLED,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName())));
    }

    private void simulateNicExists() {
        when(interfaceDao.get(nicId)).thenReturn(nic);
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
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_NOT_IN_VALID_RANGE,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName()),
                        String.format(VfsConfigValidator.NUM_OF_VFS_REPLACEMENT, numOfVfs),
                        String.format(VfsConfigValidator.MAX_NUM_OF_VFS_REPLACEMENT, oldVfsConfig.getMaxNumOfVfs())));
    }

    @Test
    public void notAllVfsAreFree() {
        allVfsAreFreeTest(false);
        assertThat(validator.allVfsAreFree(hostNicVfsConfigHelper),
                failsWith(VdcBllMessages.ACTION_TYPE_FAILED_NUM_OF_VFS_CANNOT_BE_CHANGED,
                        String.format(VfsConfigValidator.NIC_NAME_REPLACEMENT, nic.getName())));
    }

    @Test
    public void allVfsAreFree() {
        allVfsAreFreeTest(true);
        assertThat(validator.allVfsAreFree(hostNicVfsConfigHelper), isValid());
    }

    private void allVfsAreFreeTest(boolean areAllVfsFree) {
        simulateNicExists();
        when(hostNicVfsConfigHelper.areAllVfsFree(nic)).thenReturn(areAllVfsFree);

    }

}
