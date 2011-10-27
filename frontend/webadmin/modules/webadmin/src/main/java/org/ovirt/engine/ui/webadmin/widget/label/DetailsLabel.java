package org.ovirt.engine.ui.webadmin.widget.label;

import java.util.ArrayList;

import org.ovirt.engine.ui.webadmin.widget.renderer.DetailsRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

public class DetailsLabel<T extends ArrayList<ValueLabel<V>>, V> extends ValueLabel<T> {

    public DetailsLabel(String... delimiters) {
        super(new DetailsRenderer<V>(delimiters));
    }
}
