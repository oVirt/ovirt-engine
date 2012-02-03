package org.ovirt.engine.ui.userportal.section.main.view.tab.basic.widget;

import org.ovirt.engine.core.common.businessentities.DiskImage;

import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.FlowPanel;

public class DisksImageWidget extends FlowPanel implements IsEditor<TakesValueEditor<Iterable<DiskImage>>>, TakesValue<Iterable<DiskImage>> {

    private Iterable<DiskImage> images;

    @Override
    public void setValue(Iterable<DiskImage> images) {
        this.images = images;
        clear();
        for (DiskImage image : images) {
            add(new DiskImageWidget(image));
        }
    }

    @Override
    public Iterable<DiskImage> getValue() {
        return images;
    }

    @Override
    public TakesValueEditor<Iterable<DiskImage>> asEditor() {
        return TakesValueEditor.of(this);
    }

}
