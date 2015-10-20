package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

/**
 * Text column based on {@link EntityModel}'s entity value.
 *
 * @param <T>
 *            Entity value type.
 */
@SuppressWarnings("rawtypes")
public abstract class AbstractEntityModelTextColumn<T> extends AbstractTextColumn<EntityModel> {

    @SuppressWarnings("unchecked")
    @Override
    public String getValue(EntityModel object) {
        return object == null ? null : getText((T) object.getEntity());
    }

    /**
     * Get the text representation of the entity to be displayed in the column.
     */
    protected abstract String getText(T entity);

}
