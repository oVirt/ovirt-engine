package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.common.utils.MockConfigRule.mockConfig;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.common.utils.MockConfigRule;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;

public class InstallVdsInternalCommandTest extends BaseCommandTest {

    private static final String OVIRT_ISO_PREFIX = "^rhevh-(.*)\\.*\\.iso$";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String VALID_OVIRT_VERSION = "6.2";
    private static final String OVIRT_NODEOS = "^rhevh.*";

    @ClassRule
    public static MockConfigRule mcr = new MockConfigRule(
            mockConfig(ConfigValues.OvirtIsoPrefix, OVIRT_ISO_PREFIX),
            mockConfig(ConfigValues.DataDir, "."),
            mockConfig(ConfigValues.oVirtISOsRepositoryPath, OVIRT_ISOS_REPOSITORY_PATH),
            mockConfig(ConfigValues.OvirtInitialSupportedIsoVersion, VALID_OVIRT_VERSION),
            mockConfig(ConfigValues.OvirtNodeOS, OVIRT_NODEOS)
            );

    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private InstallVdsInternalCommand<InstallVdsParameters> command =
            new InstallVdsInternalCommand<>(createParameters(), null);

    @Before
    public void mockVdsDao() {
        VDS vds = new VDS();
        vds.setVdsType(VDSType.oVirtVintageNode);
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);
    }

    private static InstallVdsParameters createParameters() {
        InstallVdsParameters param = new InstallVdsParameters(Guid.newGuid());
        param.setIsReinstallOrUpgrade(true);
        return param;
    }

    private void mockVdsWithOsVersion(String osVersion) {
        VDS vds = new VDS();
        vds.setVdsType(VDSType.oVirtVintageNode);
        vds.setHostOs(osVersion);
        when(vdsDao.get(any(Guid.class))).thenReturn(vds);
    }

    @Test
    public void validateSucceeds() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        assertTrue(command.validate());
    }

    @Test
    public void validateFailsIfHostDoesNotExists() {
        when(vdsDao.get(any(Guid.class))).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
    }

}
