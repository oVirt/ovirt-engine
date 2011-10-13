package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mockStatic;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.ovirt.engine.core.common.action.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.Config;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.dao.VdsDAO;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ Config.class, DbFacade.class })
public class InstallVdsCommandTest {

    private static final String OVIRT_ISO_PREFIX = "rhevh";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String VALID_VERSION_OVIRT_ISO_FILENAME = "rhevh-6.2-20111010.0.el6.iso";
    private static final String INVALID_VERSION_OVIRT_ISO_FILENAME = "rhevh-5.5-20111010.0.el6.iso";
    private static final String VALID_OVIRT_VERSION = "6.2";
    private static final String INVALID_OVIRT_VERSION = "5.8";

    @Mock private DbFacade dbFacade;
    @Mock private VdsDAO vdsDAO;

    public InstallVdsCommandTest() {
    }

    @Before
    public void setUp() {
        initMocks(this);
        mockStatic(Config.class);
        ConfigMocker cfgMocker = new ConfigMocker();
        cfgMocker.mockConfigOvirtIsoPrefix(OVIRT_ISO_PREFIX);
        cfgMocker.mockOVirtISOsRepositoryPath(OVIRT_ISOS_REPOSITORY_PATH);

        mockStatic(DbFacade.class);
        when(DbFacade.getInstance()).thenReturn(dbFacade);
        when(dbFacade.getVdsDAO()).thenReturn(vdsDAO);
        mockVdsDAO();
    }

    private InstallVdsCommand<InstallVdsParameters> createCommand(InstallVdsParameters params) {
        return new InstallVdsCommand<InstallVdsParameters>(params);
    }

    private void mockVdsDAO() {
        VDS vds = new VDS();
        vds.setvds_type(VDSType.oVirtNode);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
    }

    private InstallVdsParameters createParameters() {
        InstallVdsParameters param = new InstallVdsParameters(Guid.NewGuid(), null);
        param.setIsReinstallOrUpgrade(true);
        return param;
    }

    private void mockVdsWithOsVersion(String osVersion) {
        VDS vds = new VDS();
        vds.setvds_type(VDSType.oVirtNode);
        vds.sethost_os(osVersion);
        when(vdsDAO.get(any(Guid.class))).thenReturn(vds);
    }

    private void assertFailsWithCanDoActionMessage(InstallVdsCommand<InstallVdsParameters> command, VdcBllMessages message) {
        assertFalse(command.canDoAction());
        assertTrue(command.getReturnValue().getCanDoActionMessages().contains(message.name()));
    }

    @Test
    public void canDoActionSucceeds() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(VALID_VERSION_OVIRT_ISO_FILENAME);
        InstallVdsCommand<InstallVdsParameters> command = createCommand(param);
        assertTrue(command.canDoAction());
    }

    @Test
    public void canDoActionFailsNullParameterForIsoFile() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(null);
        InstallVdsCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
    }

    @Test
    public void canDoActionFailsMissingIsoFile() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(INVALID_VERSION_OVIRT_ISO_FILENAME);
        InstallVdsCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
    }

    @Test
    public void canDoActionFailsIsoVersionNotCompatible() {
        mockVdsWithOsVersion(INVALID_OVIRT_VERSION);
        InstallVdsParameters param = createParameters();
        param.setoVirtIsoFile(VALID_VERSION_OVIRT_ISO_FILENAME);
        InstallVdsCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.VDS_CANNOT_UPGRADE_BETWEEN_MAJOR_VERSION);
    }

    @Test
    public void canDoActionFailsIHostDoesNotExists() {
        when(vdsDAO.get(any(Guid.class))).thenReturn(null);
        InstallVdsParameters param = createParameters();
        InstallVdsCommand<InstallVdsParameters> command = createCommand(param);
        assertFailsWithCanDoActionMessage(command, VdcBllMessages.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
    }

}
