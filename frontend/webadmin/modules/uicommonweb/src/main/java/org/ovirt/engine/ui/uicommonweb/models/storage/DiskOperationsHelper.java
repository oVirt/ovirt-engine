package org.ovirt.engine.ui.uicommonweb.models.storage;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.VmEntityType;
import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.core.common.businessentities.storage.ImageStatus;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.help.HelpTag;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicommonweb.models.templates.CopyDiskModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.MoveDiskModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class DiskOperationsHelper {

    public static void move(Model windowModel, List<DiskImage> selectedItems) {
        if (selectedItems == null || windowModel == null) {
            return;
        }

        ArrayList<DiskImage> disks = new ArrayList<>(selectedItems);

        if (windowModel.getWindow() != null) {
            return;
        }

        MoveDiskModel model = new MoveDiskModel();
        windowModel.setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().moveDisksTitle());
        model.setHelpTag(HelpTag.move_disks);
        model.setHashName("move_disks"); //$NON-NLS-1$
        model.setEntity(windowModel);
        model.init(disks);
        model.startProgress();
    }

    public static void copy(Model windowModel, List<DiskImage> selectedItems) {
        if ( selectedItems == null || windowModel == null) {
            return;
        }

        ArrayList<DiskImage> disks = new ArrayList<>(selectedItems);

        if (windowModel.getWindow() != null) {
            return;
        }

        CopyDiskModel model = new CopyDiskModel();
        windowModel.setWindow(model);
        model.setTitle(ConstantsManager.getInstance().getConstants().copyDisksTitle());
        model.setHelpTag(HelpTag.copy_disks);
        model.setHashName("copy_disks"); //$NON-NLS-1$
        model.setEntity(windowModel);
        model.init(disks);
        model.startProgress();
    }

    public static List<DiskImage> asDiskImages(List<Disk> disks) {
        if (disks == null) {
            return null;
        }
        return disks.stream()
                .filter(disk -> disk.getDiskStorageType().isInternal())
                .map(disk -> (DiskImage) disk)
                .collect(Collectors.toList());
    }

    private static void disableMoveAndCopyCommands(UICommand moveCommand, UICommand copyCommand) {
        copyCommand.setIsExecutionAllowed(false);
        moveCommand.setIsExecutionAllowed(false);
    }

    public static void updateMoveAndCopyCommandAvailability(List<DiskImage> disks, UICommand moveCommand, UICommand copyCommand) {
        boolean isCopyAllowed = true;
        boolean isMoveAllowed = true;

        if (moveCommand == null || copyCommand == null) {
            return;
        }

        if (disks == null || disks.isEmpty()) {
            disableMoveAndCopyCommands(moveCommand, copyCommand);
            return;
        }

        Guid dataCenterId = disks.get(0).getStoragePoolId();

        boolean foundTemplateDisk = false;
        boolean foundVmDisk = false;
        boolean foundUnattachedDisk = false;

        for (DiskImage disk : disks) {

            boolean isCopyOrMoveAllowed = isCopyAllowed || isMoveAllowed;
            boolean isImageDisk = disk.getDiskStorageType() == DiskStorageType.IMAGE;
            boolean isManagedBlockDisk = disk.getDiskStorageType() == DiskStorageType.MANAGED_BLOCK_STORAGE;
            boolean isImageStatusOK = disk.getImageStatus() == ImageStatus.OK;
            boolean isTheSameDataCenter = dataCenterId.equals(disk.getStoragePoolId());
            boolean isOvf = disk.isOvfStore();

            if (!isCopyOrMoveAllowed || !(isImageDisk || isManagedBlockDisk) || !isImageStatusOK ||
                    !isTheSameDataCenter || isOvf) {
                disableMoveAndCopyCommands(moveCommand, copyCommand);
                return;
            }

            if (isManagedBlockDisk) {
                isMoveAllowed = false;
            }

            VmEntityType vmEntityType = disk.getVmEntityType();
            if (vmEntityType == null) {
                foundUnattachedDisk = true;
            } else if (vmEntityType.isTemplateType()) {
                foundTemplateDisk = true;
            } else if (vmEntityType.isVmType()) {
                foundVmDisk = true;
            }

            if (foundTemplateDisk && (foundUnattachedDisk || foundVmDisk)) {
                isCopyAllowed = false;
            }

            if (vmEntityType != null && vmEntityType.isTemplateType()) {
                isMoveAllowed = false;
            }
        }

        copyCommand.setIsExecutionAllowed(isCopyAllowed);
        moveCommand.setIsExecutionAllowed(isMoveAllowed);
    }
}
