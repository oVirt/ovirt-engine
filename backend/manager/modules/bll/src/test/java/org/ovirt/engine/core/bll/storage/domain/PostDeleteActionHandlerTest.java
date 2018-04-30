package org.ovirt.engine.core.bll.storage.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.when;

import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.ovirt.engine.core.common.businessentities.StorageDomain;
import org.ovirt.engine.core.common.businessentities.storage.DiskVmElement;
import org.ovirt.engine.core.common.vdscommands.PostDeleteAction;
import org.ovirt.engine.core.common.vdscommands.StoragePoolDomainAndGroupIdBaseVDSCommandParameters;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.auditloghandling.AuditLogDirector;
import org.ovirt.engine.core.dao.DiskVmElementDao;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class PostDeleteActionHandlerTest {
    @Spy
    @InjectMocks
    private PostDeleteActionHandler postDeleteActionHandler;

    @Mock
    private DiskVmElementDao diskVmElementDao;

    @Mock
    private AuditLogDirector auditLogDirector;

    @Test
    public void parametersWithSecureDeletionAreFixedOnFileDomainWhenPostZeroIsTrue() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(true, false), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsTrue() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(true, false), false);
        assertPostZeroValue(parameters, true);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnFileDomainWhenPostZeroIsFalse() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(false, false), true);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void parametersWithSecureDeletionAreNotFixedOnBlockDomainWhenPostZeroIsFalse() {
        ParametersWithPostDeleteAction parameters = postDeleteActionHandler.fixPostZeroField(
                new ParametersWithPostDeleteAction(false, false), false);
        assertPostZeroValue(parameters, false);
    }

    @Test
    public void discardIsTrueWhenDiscardAfterDeleteIsTrueAndDomainSupportsDiscard() {
        assertDiscardValue(true, true, true, false);
    }

    @Test
    public void discardIsFalseWhenDiscardAfterDeleteIsTrueAndDomainDoesNotSupportDiscard() {
        assertDiscardValue(true, false, false, false);
        assertDiscardValue(true, null, false, false);
    }

    @Test
    public void discardIsTrueWhenTheDiskIsAttachedToVmWithEnableDiscardAndDomainSupportsDiscard() {
        assertDiscardValue(false, true, true, true);
    }

    @Test
    public void discardIsFalseWhenTheDiskIsAttachedToVmWithEnableDiscardAndDomainDoesNotSupportDiscard() {
        assertDiscardValue(false, false, false, true);
    }

    @Test
    public void diskVmElementWithPassDiscardExists() {
        testExistenceOfDiskVmElementWithPassDiscard(true,
                Arrays.asList(createDiskVmElementWithPassDiscardValue(false),
                        createDiskVmElementWithPassDiscardValue(true)));
    }

    @Test
    public void diskVmElementWithPassDiscardDoesNotExist() {
        testExistenceOfDiskVmElementWithPassDiscard(false,
                Arrays.asList(createDiskVmElementWithPassDiscardValue(false),
                        createDiskVmElementWithPassDiscardValue(false)));
    }

    private DiskVmElement createDiskVmElementWithPassDiscardValue(boolean passDiscard) {
        DiskVmElement diskVmElement = new DiskVmElement();
        diskVmElement.setPassDiscard(passDiscard);
        return diskVmElement;
    }

    private void testExistenceOfDiskVmElementWithPassDiscard(boolean expectedResult,
            List<DiskVmElement> diskVmElements) {
        when(diskVmElementDao.getAllDiskVmElementsByDiskId(any())).thenReturn(diskVmElements);
        assertEquals(expectedResult, postDeleteActionHandler.diskVmElementWithPassDiscardExists(Guid.Empty));
    }

    private void assertPostZeroValue(ParametersWithPostDeleteAction parameters, boolean postZeroExpectedValue) {
        assertEquals(
                parameters.getPostZero(), postZeroExpectedValue, MessageFormat.format(
                        "Wrong VDS command parameters: 'postZero' should be {0}.", postZeroExpectedValue));
    }

    private void assertDiscardValue(boolean discardAfterDelete, Boolean supportsDiscard,
            boolean expectedFixedDiscardParameter, boolean diskAttachedToAtLeastOneVmWithEnableDiscard) {
        StorageDomain storageDomain = new StorageDomain();
        storageDomain.setDiscardAfterDelete(discardAfterDelete);
        storageDomain.setSupportsDiscard(supportsDiscard);

        ParametersWithPostDeleteAction params =
                new ParametersWithPostDeleteAction(false, storageDomain.getDiscardAfterDelete());

        doReturn(diskAttachedToAtLeastOneVmWithEnableDiscard)
                .when(postDeleteActionHandler).diskVmElementWithPassDiscardExists(any());

        assertEquals(expectedFixedDiscardParameter,
                postDeleteActionHandler.fixDiscardField(params, storageDomain).isDiscard());
    }

    private static class ParametersWithPostDeleteAction
            extends StoragePoolDomainAndGroupIdBaseVDSCommandParameters implements PostDeleteAction {

        private boolean postZero;

        private boolean discard;

        public ParametersWithPostDeleteAction(boolean postZero, boolean discard) {
            setPostZero(postZero);
            setDiscard(discard);
        }

        @Override
        public boolean getPostZero() {
            return postZero;
        }

        @Override
        public void setPostZero(boolean postZero) {
            this.postZero = postZero;
        }

        @Override
        public boolean isDiscard() {
            return discard;
        }

        @Override
        public void setDiscard(boolean discard) {
            this.discard = discard;
        }
    }

}
