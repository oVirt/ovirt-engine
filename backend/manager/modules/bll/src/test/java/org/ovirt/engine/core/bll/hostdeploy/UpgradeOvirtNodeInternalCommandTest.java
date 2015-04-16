package org.ovirt.engine.core.bll.hostdeploy;

import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.VdcBllMessages;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDAO;
import org.ovirt.engine.core.utils.MockConfigRule;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.utils.MockConfigRule.mockConfig;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeOvirtNodeInternalCommandTest {

    private static final String OVIRT_ISO_PREFIX = "^rhevh-(.*)\\.*\\.iso$";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String VALID_VERSION_OVIRT_ISO_FILENAME = "rhevh-6.2-20111010.0.el6.iso";
    private static final String INVALID_VERSION_OVIRT_ISO_FILENAME = "rhevh-5.5-20111010.0.el6.iso";
    private static final String VALID_OVIRT_VERSION = "6.2";
    private static final String INVALID_OVIRT_VERSION = "5.8";
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
    private VdsDAO vdsDAO;

    private UpgradeOvirtNodeInternalCommand<InstallVdsParameters> createCommand(InstallVdsParameters params) {
        UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command = spy(new UpgradeOvirtNodeInternalCommand<InstallVdsParameters>(params, null));
        doReturn(vdsDAO).when(command).getVdsDAO();
        return command;
    }

    @Before
    public void mockVdsDAO() {
        VDS vds = new VDS();
        vds.setVdsType(VDSType.oVirtNode);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
    }

    private static InstallVdsParameters createParameters() {
        InstallVdsParameters param = new InstallVdsParameters(Guid.newGuid());
        param.setIsReinstallOrUpgrade(true);
        return param;
    }

    private void mockVdsWithOsVersion(String osVersion) {
        VDS vds = new VDS();
        vds.setVdsType(VDSType.oVirtNode);
        vds.setHostOs(osVersion);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
    }

    private static void assertFailsWithCanDoActionMessage
            (UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command, VdcBllMessages message) {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(message.name()));
    }

    @Test
    public void canDoActionSucceeds() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(VALID_VERSION_OVIRT_ISO_FILENAME);
        UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command = createCommand(param);
        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionFailsNullParameterForIsoFile() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(null);
        UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
    }

    @Test
    public void canDoActionFailsMissingIsoFile() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(INVALID_VERSION_OVIRT_ISO_FILENAME);
        UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
    }

    @Test
    public void canDoActionFailsIsoVersionNotCompatible() {
        mockVdsWithOsVersion(INVALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(VALID_VERSION_OVIRT_ISO_FILENAME);
        UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.VDS_CANNOT_UPGRADE_BETWEEN_MAJOR_VERSION);
    }

    @Test
    public void canDoActionFailsIHostDoesNotExists() {
        when(vdsDAO.get(any(Guid.class))).thenReturn(null);
        InstallVdsParameters param = createParameters();
        UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
    }

}
