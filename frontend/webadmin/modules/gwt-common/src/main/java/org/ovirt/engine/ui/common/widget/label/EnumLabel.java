package org.ovirt.engine.ui.common.widget.label;

import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;

import com.google.gwt.user.client.ui.ValueLabel;

/**
 * Label widget that uses {@link EnumRenderer}.
 *
 * @param <E>
 *            Enum type.
 */
public class EnumLabel<E extends Enum<E>> extends ValueLabel<E> {

    public EnumLabel() {
        super(new EnumRenderer<E>());
    }

}
