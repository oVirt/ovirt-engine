package org.ovirt.engine.ui.webadmin.widget.label;

import java.util.List;

import org.ovirt.engine.ui.common.widget.renderer.StringRenderer;

import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.text.shared.Renderer;
import com.google.gwt.user.client.ui.ValueLabel;

public class ValueListLabel<T> extends ValueLabel<List<T>> {

    public ValueListLabel(final String delimiter) {
        this(delimiter, new StringRenderer<T>());
    }

    public ValueListLabel(final String delimiter, final Renderer<T> itemRenderer) {
        super(new AbstractRenderer<List<T>>() {
            @Override
            public String render(List<T> values) {
                StringBuilder builder = new StringBuilder();
                if (values != null) {
                    for(T value: values) {
                        if (value != values.get(0)) {
                            builder.append(delimiter);
                        }
                        builder.append(itemRenderer.render(value));
                    }
                }
                return builder.toString();
            }
        });
    }

}
