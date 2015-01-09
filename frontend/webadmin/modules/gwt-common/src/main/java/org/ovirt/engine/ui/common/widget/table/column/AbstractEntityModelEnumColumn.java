package org.ovirt.engine.ui.common.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

/**
 * Enum column based on {@link EntityModel}'s entity value.
 *
 * @param <T>
 *            Entity value type.
 * @param <E>
 *            Enum type.
 */
public abstract class AbstractEntityModelEnumColumn<T, E extends Enum<E>> extends AbstractEnumColumn<EntityModel, E> {

    @SuppressWarnings("unchecked")
    @Override
    protected E getRawValue(EntityModel object) {
        return object == null ? null : getEnum((T) object.getEntity());
    }

    /**
     * Get the enum wrapped inside the entity.
     */
    protected abstract E getEnum(T entity);

}
