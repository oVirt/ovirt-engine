package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.core.common.businessentities.storage.DiskImage;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;

public class DisksImageWidget extends FlowPanel implements IsEditor<TakesValueEditor<Iterable<DiskImage>>>, TakesValue<Iterable<DiskImage>>, HasElementId {

    private Iterable<DiskImage> images;

    private String elementId = DOM.createUniqueId();

    @Override
    public void setValue(Iterable<DiskImage> images) {
        this.images = images;
        clear();

        int i = 0;
        for (DiskImage image : images) {
            add(createDiskWidget(image, i++));
        }
    }

    DiskImageWidget createDiskWidget(DiskImage image, int index) {
        DiskImageWidget diskWidget = new DiskImageWidget(image);
        diskWidget.setElementId(
                ElementIdUtils.createElementId(elementId, "disk" + index)); //$NON-NLS-1$
        return diskWidget;
    }

    @Override
    public Iterable<DiskImage> getValue() {
        return images;
    }

    @Override
    public TakesValueEditor<Iterable<DiskImage>> asEditor() {
        return TakesValueEditor.of(this);
    }

    @Override
    public void setElementId(String elementId) {
        this.elementId = elementId;
    }

}
