package org.ovirt.engine.ui.common.widget.table.column;

import com.google.gwt.text.shared.Renderer;

/**
 * Base class for text columns that use {@link Renderer} to render the given column type into its text-based
 * representation.
 *
 * @param <T>
 *            Table row data type.
 * @param <C>
 *            Column value type.
 */
public abstract class AbstractRenderedTextColumn<T, C> extends AbstractTextColumn<T> {

    protected final Renderer<C> renderer;

    public AbstractRenderedTextColumn(Renderer<C> renderer) {
        super();
        this.renderer = renderer;
    }

    @Override
    public String getValue(T object) {
        return renderer.render(getRawValue(object));
    }

    /**
     * Returns the raw value to be rendered.
     */
    protected abstract C getRawValue(T object);

}
