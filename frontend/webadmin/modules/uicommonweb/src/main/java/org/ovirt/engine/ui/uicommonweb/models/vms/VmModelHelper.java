package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskStorageType;
import org.ovirt.engine.ui.uicommonweb.models.Model;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.uicompat.UIMessages;

public class VmModelHelper {

    public static enum WarningType {
        VM_EXPORT,
        VM_SNAPSHOT,
        VM_TEMPLATE
    }

    public static void sendWarningForNonExportableDisks(Model model, List<Disk> vmDisks, WarningType warningType) {
        final List<Disk> sharedImageDisks = new ArrayList<>();
        final List<Disk> directLunDisks = new ArrayList<>();
        final List<Disk> snapshotDisks = new ArrayList<>();

        for (Disk disk : vmDisks) {
            if (disk.getDiskStorageType() == DiskStorageType.IMAGE) {
                if (disk.isShareable()) {
                    sharedImageDisks.add(disk);
                } else if (disk.isDiskSnapshot()) {
                    snapshotDisks.add(disk);
                }
            } else if (disk.getDiskStorageType() == DiskStorageType.LUN) {
                directLunDisks.add(disk);
            }
        }

        final UIMessages messages = ConstantsManager.getInstance().getMessages();

        // check if VM provides any disk for the export
        if (vmDisks.size() - (sharedImageDisks.size() + directLunDisks.size() + snapshotDisks.size()) == 0) {
            switch (warningType) {
            case VM_EXPORT:
                model.setMessage(messages.noExportableDisksFoundForTheExport());
                break;
            case VM_SNAPSHOT:
                model.setMessage(messages.noExportableDisksFoundForTheSnapshot());
                break;
            case VM_TEMPLATE:
                model.setMessage(messages.noExportableDisksFoundForTheTemplate());
                break;
            }
        }

        String diskLabels = getDiskLabelList(sharedImageDisks);
        if (diskLabels != null) {
            switch (warningType) {
            case VM_EXPORT:
                model.setMessage(messages.sharedDisksWillNotBePartOfTheExport(diskLabels));
                break;
            case VM_SNAPSHOT:
                model.setMessage(messages.sharedDisksWillNotBePartOfTheSnapshot(diskLabels));
                break;
            case VM_TEMPLATE:
                model.setMessage(messages.sharedDisksWillNotBePartOfTheTemplate(diskLabels));
                break;
            }
        }

        diskLabels = getDiskLabelList(directLunDisks);
        if (diskLabels != null) {
            switch (warningType) {
            case VM_EXPORT:
                model.setMessage(messages.directLUNDisksWillNotBePartOfTheExport(diskLabels));
                break;
            case VM_SNAPSHOT:
                model.setMessage(messages.directLUNDisksWillNotBePartOfTheSnapshot(diskLabels));
                break;
            case VM_TEMPLATE:
                model.setMessage(messages.directLUNDisksWillNotBePartOfTheTemplate(diskLabels));
                break;
            }
        }

        diskLabels = getDiskLabelList(snapshotDisks);
        if (diskLabels != null) {
            switch (warningType) {
                case VM_EXPORT:
                    model.setMessage(messages.snapshotDisksWillNotBePartOfTheExport(diskLabels));
                    break;
                case VM_SNAPSHOT:
                    model.setMessage(messages.snapshotDisksWillNotBePartOfTheSnapshot(diskLabels));
                    break;
                case VM_TEMPLATE:
                    model.setMessage(messages.snapshotDisksWillNotBePartOfTheTemplate(diskLabels));
                    break;
            }
        }
    }

    public static String getDiskLabelList(List<Disk> disks) {
        if (disks.isEmpty()) {
            return null;
        }
        return disks.stream().map(Disk::getDiskAlias).collect(Collectors.joining(", ")); //$NON-NLS-1$
    }

}
