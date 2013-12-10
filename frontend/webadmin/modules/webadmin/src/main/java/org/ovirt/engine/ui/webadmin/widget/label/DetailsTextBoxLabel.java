package org.ovirt.engine.ui.webadmin.widget.label;

import java.util.ArrayList;

import org.ovirt.engine.ui.common.widget.label.TextBoxLabelBase;
import org.ovirt.engine.ui.webadmin.widget.renderer.DetailsRenderer;

public class DetailsTextBoxLabel<T extends ArrayList<TextBoxLabelBase<V>>, V> extends TextBoxLabelBase<T> {

    public DetailsTextBoxLabel(String... delimiters) {
        super(new DetailsRenderer<V>(delimiters));
    }

}
