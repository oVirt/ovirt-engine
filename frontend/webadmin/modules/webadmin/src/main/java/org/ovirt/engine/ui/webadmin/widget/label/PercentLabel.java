package org.ovirt.engine.ui.webadmin.widget.label;

import org.ovirt.engine.ui.webadmin.widget.renderer.PercentRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

/**
 * Label widget that uses {@link PercentRenderer}.
 *
 * @param <T>
 *            Number type.
 */
public class PercentLabel<T extends Number> extends ValueLabel<T> {

    public PercentLabel() {
        super(new PercentRenderer<T>());
    }

}
