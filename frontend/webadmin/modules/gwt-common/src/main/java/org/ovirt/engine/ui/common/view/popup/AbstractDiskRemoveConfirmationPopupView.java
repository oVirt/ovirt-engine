package org.ovirt.engine.ui.common.view.popup;

import java.util.ArrayList;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.uicommonweb.models.vms.DiskModel;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.inject.Inject;

public abstract class AbstractDiskRemoveConfirmationPopupView extends RemoveConfirmationPopupView {

    ArrayList<String> notes = new ArrayList<String>();

    @Inject
    public AbstractDiskRemoveConfirmationPopupView(EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationMessages messages,
            CommonApplicationConstants constants) {
        super(eventBus, resources, messages, constants);
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
        if (disk.isBoot()) {
            notes.add(constants.bootable());
        }

        if (isInVm && disk.getNumberOfVms() > 1) {
            notes.add(messages.diskAttachedToOtherVMs(disk.getNumberOfVms() - 1, disk.getVmNames().get(0)));
        }
        else if (!isInVm && disk.getNumberOfVms() > 0) {
            notes.add(messages.diskAttachedToVMs(disk.getNumberOfVms()));
        }

        if (!notes.isEmpty()) {
            String notes = messages.diskNote()
                + getFormattedNote();
            addItemLabel(SafeHtmlUtils.fromSafeConstant("<b>" + notes + "</b>")); //$NON-NLS-1$ //$NON-NLS-2$
        }

        addItemLabel(SafeHtmlUtils.fromSafeConstant(constants.lineBreak()));
    }

    String getFormattedNote() {
        StringBuilder formattedNote = new StringBuilder(constants.empty());

        for (int i = 0; i < notes.size(); i++) {
            String note = notes.get(i);
            formattedNote.append(constants.lineBreak()).append(constants.htmlTab()).append(note);
        }

        return formattedNote.toString();
    }
}
