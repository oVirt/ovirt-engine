package org.ovirt.engine.ui.common.widget.uicommon.disks;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.storage.DiskContentType;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBox;
import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class DisksContentTypeSelectionList extends FlowPanel {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final List<DisksContentViewChangeHandler> changeHandlers = new ArrayList<>();

    private ListModelListBox<DiskContentType> diskContentTypeList;

    public interface DisksContentViewChangeHandler {
        /**
         * Called when the selected disks content type changes.
         */
        void disksContentViewChanged(DiskContentType newType);
    }

    public DisksContentTypeSelectionList() {
        Label label = new Label();
        label.setText(constants.diskContentType() + ":"); //$NON-NLS-1$
        label.addStyleName("disk-content-type-group-label"); //$NON-NLS-1$
        add(label);
        diskContentTypeList = getDiskContentTypeList();
        add(diskContentTypeList);
   }

    private ListModelListBox<DiskContentType> getDiskContentTypeList() {
        ListModelListBox<DiskContentType> l = new ListModelListBox<>(new ContentTypeRenderer());
        List<DiskContentType> values = new ArrayList<>();
        values.add(null);
        values.addAll(Arrays.asList(DiskContentType.values()));

        l.setAcceptableValues(values);
        l.addValueChangeHandler(event -> fireChangeHandlers(event.getValue()));
        l.setValue(values.get(0));
        l.addStyleName("disk-type-buttons-group"); //$NON-NLS-1$
        return l;
    }

    public void addChangeHandler(DisksContentViewChangeHandler handler) {
        if (!changeHandlers.contains(handler)) {
            changeHandlers.add(handler);
        }
    }

    private void fireChangeHandlers(DiskContentType type) {
        for (DisksContentViewChangeHandler disksViewChangeHandler : changeHandlers) {
            disksViewChangeHandler.disksContentViewChanged(type);
        }
    }

    public DiskContentType getDiskContentType() {
        return diskContentTypeList.getValue();
    }

    public void setDiskContentType(DiskContentType diskContentType) {
        diskContentTypeList.setValue(diskContentType);
    }

    class ContentTypeRenderer extends AbstractRenderer<DiskContentType> {
        @Override
        public String render(DiskContentType object) {
            return object == null ? constants.allDisksLabel() : EnumTranslator.getInstance().translate(object);
        }
    }

}
