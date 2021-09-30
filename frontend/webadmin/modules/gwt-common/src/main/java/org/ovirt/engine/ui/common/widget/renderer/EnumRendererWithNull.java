package org.ovirt.engine.ui.common.widget.renderer;

/**
 * Renderer for Enum values, allowing null key and translating it to the given string
 *
 * @param <E>
 *            Enum type.
 */
public class EnumRendererWithNull<E extends Enum<E>> extends EnumRenderer<E> {

    private String nullValue;

    public EnumRendererWithNull(String nullValue) {
        this.nullValue = nullValue;
    }

    @Override
    public String render(E object) {
        if (object == null) {
            return nullValue;
        }
        return super.render(object);
    }

}
