package org.ovirt.engine.ui.common.widget.renderer;

import org.ovirt.engine.ui.uicompat.EnumTranslator;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * Renderer for Enum values.
 *
 * @param <E>
 *            Enum type.
 */
public class EnumRenderer<E extends Enum<E>> extends AbstractRenderer<E> {

    @Override
    public String render(E object) {
        return EnumTranslator.getInstance().translate(object);
    }

}
