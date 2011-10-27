package org.ovirt.engine.ui.webadmin.widget.table.column;

import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.user.cellview.client.TextColumn;

/**
 * A Text Column for EntityModel Values
 * 
 * @param <T>
 *            Entity Type
 */
public abstract class EntityModelTextColumn<T> extends TextColumn<EntityModel> {

    @SuppressWarnings("unchecked")
    @Override
    public String getValue(EntityModel object) {
        return object == null ? null : getValue((T) object.getEntity());
    }

    /**
     * Get the String Value from the Entity Value
     * 
     * @param entity
     * @return
     */
    protected abstract String getValue(T entity);

}
