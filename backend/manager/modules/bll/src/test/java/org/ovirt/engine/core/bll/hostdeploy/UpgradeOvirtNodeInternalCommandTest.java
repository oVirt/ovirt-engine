package org.ovirt.engine.core.bll.hostdeploy;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.util.stream.Stream;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.ovirt.engine.core.bll.BaseCommandTest;
import org.ovirt.engine.core.bll.ValidateTestUtils;
import org.ovirt.engine.core.common.action.hostdeploy.InstallVdsParameters;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.config.ConfigValues;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dao.VdsDao;
import org.ovirt.engine.core.utils.MockConfigDescriptor;
import org.ovirt.engine.core.utils.MockConfigExtension;

@ExtendWith(MockConfigExtension.class)
public class UpgradeOvirtNodeInternalCommandTest extends BaseCommandTest {

    private static final String OVIRT_ISO_PREFIX = "^rhevh-(.*)\\.*\\.iso$";
    private static final String OVIRT_ISOS_REPOSITORY_PATH = "src/test/resources/ovirt-isos";
    private static final String VALID_VERSION_OVIRT_ISO_FILENAME = "rhevh-6.2-20111010.0.el6.iso";
    private static final String INVALID_VERSION_OVIRT_ISO_FILENAME = "rhevh-5.5-20111010.0.el6.iso";
    private static final String VALID_OVIRT_VERSION = "6.2";
    private static final String INVALID_OVIRT_VERSION = "5.8";
    private static final String OVIRT_NODEOS = "^rhevh.*";

    public static Stream<MockConfigDescriptor<?>> mockConfiguration() {
        return Stream.of(
                MockConfigDescriptor.of(ConfigValues.OvirtIsoPrefix, OVIRT_ISO_PREFIX),
                MockConfigDescriptor.of(ConfigValues.DataDir, "."),
                MockConfigDescriptor.of(ConfigValues.oVirtISOsRepositoryPath, OVIRT_ISOS_REPOSITORY_PATH),
                MockConfigDescriptor.of(ConfigValues.OvirtInitialSupportedIsoVersion, VALID_OVIRT_VERSION),
                MockConfigDescriptor.of(ConfigValues.OvirtNodeOS, OVIRT_NODEOS)
        );
    }

    @Mock
    private VdsDao vdsDao;

    @InjectMocks
    private UpgradeOvirtNodeInternalCommand<InstallVdsParameters> command =
            new UpgradeOvirtNodeInternalCommand<>(createParameters(), null);

    @BeforeAll
    public static void setUpClass() {
        OVirtNodeInfo.clearInstance();
    }

    @BeforeEach
    public void mockVdsDao() {
        VDS vds = new VDS();
        vds.setVdsType(VDSType.oVirtVintageNode);
        when(vdsDao.get(any())).thenReturn(vds);
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
        when(vdsDao.get(any())).thenReturn(vds);
    }

    @Test
    public void validateSucceeds() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        command.getParameters().setoVirtIsoFile(VALID_VERSION_OVIRT_ISO_FILENAME);
        assertTrue(command.validate());
    }

    @Test
    public void validateFailsNullParameterForIsoFile() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        command.getParameters().setoVirtIsoFile(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
    }

    @Test
    public void validateFailsMissingIsoFile() {
        mockVdsWithOsVersion(VALID_OVIRT_VERSION);
        command.getParameters().setoVirtIsoFile(INVALID_VERSION_OVIRT_ISO_FILENAME);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE);
    }

    @Test
    public void validateFailsIsoVersionNotCompatible() {
        mockVdsWithOsVersion(INVALID_OVIRT_VERSION);
        command.getParameters().setoVirtIsoFile(VALID_VERSION_OVIRT_ISO_FILENAME);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.VDS_CANNOT_UPGRADE_BETWEEN_MAJOR_VERSION);
    }

    @Test
    public void validateFailsIfHostDoesNotExists() {
        when(vdsDao.get(any())).thenReturn(null);
        ValidateTestUtils.runAndAssertValidateFailure(command, EngineMessage.ACTION_TYPE_FAILED_HOST_NOT_EXIST);
    }

}
