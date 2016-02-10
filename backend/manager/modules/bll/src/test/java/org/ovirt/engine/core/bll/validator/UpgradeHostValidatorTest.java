package org.ovirt.engine.core.bll.validator;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.failsWith;
import static org.ovirt.engine.core.bll.validator.ValidationResultMatchers.isValid;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.core.common.businessentities.VDSType;
import org.ovirt.engine.core.common.errors.EngineMessage;
import org.ovirt.engine.core.utils.RandomUtils;

@RunWith(MockitoJUnitRunner.class)
public class UpgradeHostValidatorTest {

    @Mock
    private VDS host;

    private UpgradeHostValidator validator;

    @Before
    public void setup() {
        validator = new UpgradeHostValidator(host);
    }

    @Test
    public void hostExists() {
        assertThat(validator.hostExists(), isValid());
    }

    @Test
    public void hostDoesNotExist() {
        validator = new UpgradeHostValidator(null);

        assertThat(validator.hostExists(), failsWith(EngineMessage.VDS_INVALID_SERVER_ID));
    }

    @Test
    public void statusSupportedForHostUpgrade() {
        when(host.getStatus()).thenReturn(VDSStatus.Maintenance);

        assertThat(validator.statusSupportedForHostUpgrade(), isValid());
    }

    @Test
    public void statusNotSupportedForHostUpgrade() {
        VDS host = new VDS();
        host.setStatus(VDSStatus.Unassigned);

        assertThat(new UpgradeHostValidator(host).statusSupportedForHostUpgrade(),
                failsWith(EngineMessage.CANNOT_UPGRADE_HOST_STATUS_ILLEGAL));
    }

    @Test
    public void statusSupportedForHostUpgradeInternal() {
        when(host.getStatus()).thenReturn(VDSStatus.Maintenance);

        assertThat(validator.statusSupportedForHostUpgradeInternal(), isValid());
    }

    @Test
    public void statusNotSupportedForHostUpgradeInternal() {
        when(host.getStatus()).thenReturn(VDSStatus.Unassigned);

        assertThat(validator.statusSupportedForHostUpgradeInternal(),
                failsWith(EngineMessage.CANNOT_UPGRADE_HOST_STATUS_ILLEGAL));
    }

    @Test
    public void updatesAvailable() {
        when(host.isUpdateAvailable()).thenReturn(true);

        assertThat(validator.updatesAvailable(), isValid());
    }

    @Test
    public void updatesUnavailableForUpgradingOvirtNode() {
        mockOvirtNode();

        assertThat(validator.updatesAvailable(), isValid());
    }

    private void mockOvirtNode() {
        when(host.isOvirtVintageNode()).thenReturn(true);
    }

    @Test
    public void updatesUnavailable() {
        assertThat(validator.updatesAvailable(), failsWith(EngineMessage.NO_AVAILABLE_UPDATES_FOR_HOST));
    }

    @Test
    public void imageProvidedForOvirtNode() {
        when(host.getVdsType()).thenReturn(VDSType.oVirtVintageNode);
        String imageName = RandomUtils.instance().nextString(20);

        assertThat(validator.imageProvidedForOvirtNode(imageName), isValid());
    }

    @Test
    public void imageNotProvidedForOvirtNode() {
        mockOvirtNode();

        assertThat(validator.imageProvidedForOvirtNode(null),
                failsWith(EngineMessage.VDS_CANNOT_INSTALL_MISSING_IMAGE_FILE));
    }

    @Test
    public void hostWasInstalled() {
        when(host.getHostOs()).thenReturn(RandomUtils.instance().nextString(20));

        assertThat(validator.hostWasInstalled(), isValid());
    }

    @Test
    public void hostWasNotInstalled() {
        assertThat(validator.hostWasInstalled(), failsWith(EngineMessage.CANNOT_UPGRADE_HOST_WITHOUT_OS));
    }
}
