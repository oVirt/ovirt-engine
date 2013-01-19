package org.ovirt.engine.core.bll;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.util.Collections;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ovirt.engine.core.bll.snapshots.SnapshotsValidator;
import org.ovirt.engine.core.common.action.RemoveSnapshotParameters;
import org.ovirt.engine.core.common.businessentities.DiskImage;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VMStatus;
import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.VdcBllMessages;
import org.ovirt.engine.core.dao.DiskImageDAO;
import org.ovirt.engine.core.dao.VmTemplateDAO;

/** A test case for the {@link RemoveSnapshotCommand} class. */
@RunWith(MockitoJUnitRunner.class)
public class RemoveSnapshotCommandTest {

    /** The command to test */
    private RemoveSnapshotCommand<RemoveSnapshotParameters> cmd;

    @Mock
    private VmTemplateDAO vmTemplateDAO;

    @Mock
    private DiskImageDAO diskImageDAO;

    @Mock
    private SnapshotsValidator snapshotValidator;

    @Before
    public void setUp() {
        Guid vmGuid = Guid.NewGuid();
        Guid snapGuid = Guid.NewGuid();

        RemoveSnapshotParameters params = new RemoveSnapshotParameters(snapGuid, vmGuid);
        cmd = spy(new RemoveSnapshotCommand<RemoveSnapshotParameters>(params));
        doReturn(vmTemplateDAO).when(cmd).getVmTemplateDAO();
        doReturn(diskImageDAO).when(cmd).getDiskImageDao();
        doReturn(snapshotValidator).when(cmd).createSnapshotValidator();
    }

    @Test
    public void testValidateImageNotInTemplateTrue() {
        when(vmTemplateDAO.get(mockSourceImage())).thenReturn(null);
        assertTrue("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateImageNotInTemplateFalse() {
        when(vmTemplateDAO.get(mockSourceImage())).thenReturn(new VmTemplate());
        assertFalse("validation should succeed", cmd.validateImageNotInTemplate());
    }

    @Test
    public void testValidateImageNotActiveTrue() {
        when(diskImageDAO.get(mockSourceImage())).thenReturn(null);
        assertTrue("validation should succeed", cmd.validateImageNotActive());
    }

    @Test
    public void testValidateImageNotActiveFalse() {
        when(diskImageDAO.get(mockSourceImage())).thenReturn(new DiskImage());
        assertFalse("validation should succeed", cmd.validateImageNotActive());
    }

    @Test
    public void testCanDoActionVmUp() {
        VM vm = new VM();
        vm.setId(Guid.NewGuid());
        vm.setStatus(VMStatus.Up);

        cmd.setSnapshotName("someSnapshot");
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotDuringSnapshot(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotValidator).vmNotInPreview(any(Guid.class));
        doReturn(ValidationResult.VALID).when(snapshotValidator).snapshotExists(any(Guid.class), any(Guid.class));
        doReturn(true).when(cmd).validateImagesAndVMStates();
        doReturn(vm).when(cmd).getVm();
        doReturn(Collections.emptyList()).when(cmd).getSourceImages();
        CanDoActionTestUtils.runAndAssertCanDoActionFailure(cmd, VdcBllMessages.ACTION_TYPE_FAILED_VM_IS_NOT_DOWN);
    }

    /** Mocks a call to {@link RemoveSnapshotCommand#getSourceImages()} and returns its image guid */
    private Guid mockSourceImage() {
        Guid imageId = Guid.NewGuid();
        DiskImage image = new DiskImage();
        image.setImageId(imageId);
        doReturn(Collections.singletonList(image)).when(cmd).getSourceImages();
        return imageId;
    }
}
