package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

/**
 * An Enum Column for EntityModel Values
 *
 * @param <T>
 *            Entity Type
 * @param <E>
 *            Enum Type
 */
public abstract class EntityModelEnumColumn<T, E extends Enum<E>> extends EnumColumn<EntityModel, E> {

    @SuppressWarnings("unchecked")
    @Override
    protected E getRawValue(EntityModel object) {
        return object == null ? null : getRawValue((T) object.getEntity());
    }

    /**
     * Get the Raw Value from the Entity Value
     *
     * @param entity
     * @return
     */
    protected abstract E getRawValue(T entity);

}
