package org.ovirt.engine.ui.webadmin.widget.renderer;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for percent values.
 *
 * @param <T>
 *            Number type.
 */
public class PercentRenderer<T extends Number> extends AbstractRenderer<T> {

    @Override
    public String render(T percent) {
        return percent != null ? percent.intValue() + "%" : "0%"; //$NON-NLS-1$ //$NON-NLS-2$
    }

}
