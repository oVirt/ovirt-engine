package org.ovirt.engine.ui.common.view.popup;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public abstract class AbstractDiskRemoveConfirmationPopupView extends RemoveConfirmationPopupView {

    ArrayList<String> notes = new ArrayList<>();

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public AbstractDiskRemoveConfirmationPopupView(EventBus eventBus) {
        super(eventBus);
    }

    @Override
    protected void addItemText(Object item) {

        DiskModel diskModel = (DiskModel) item;
        Disk disk = diskModel.getDisk();
        boolean isInVm = diskModel.getVm() != null;
        notes.clear();

        addItemLabel(getItemTextFormatted(disk.getDiskAlias()));

        if (disk.isShareable()) {
            notes.add(constants.shareable());
        }
        if (isInVm && disk.getDiskVmElementForVm(diskModel.getVm().getId()) != null &&
                disk.getDiskVmElementForVm(diskModel.getVm().getId()).isBoot()) {
            notes.add(constants.bootable());
        }

        if (isInVm && disk.getNumberOfVms() > 1) {
            notes.add(messages.diskAttachedToOtherVMs(disk.getNumberOfVms() - 1, disk.getVmNames().get(0)));
        } else if (!isInVm && disk.getNumberOfVms() > 0) {
            notes.add(messages.diskAttachedToVMs(disk.getNumberOfVms()));
        }

        if (disk.getContentType() == DiskContentType.MEMORY_METADATA_VOLUME ||
                disk.getContentType() == DiskContentType.MEMORY_DUMP_VOLUME) {
            notes.add(constants.memoryDisk());
            notes.add(constants.otherMemoryDiskWillbeRemoved());
        }

        if (!notes.isEmpty()) {
            String notes = constants.htmlTab() + messages.diskNote() + getFormattedNote();
            addItemLabel(SafeHtmlUtils.fromSafeConstant("<b>" + notes + "</b>")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        addItemLabel(SafeHtmlUtils.fromSafeConstant(constants.lineBreak()));
    }

    String getFormattedNote() {
        StringBuilder formattedNote = new StringBuilder(constants.empty());

        for (int i = 0; i < notes.size(); i++) {
            String note = notes.get(i);
            formattedNote.append(constants.lineBreak()).append(constants.htmlTab()).append(constants.htmlTab()).append("- ").append(note); //$NON-NLS-1$
        }

        return formattedNote.toString();
    }
}
