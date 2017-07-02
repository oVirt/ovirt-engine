package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.Queryable;

import com.google.gwt.view.client.ProvidesKey;

public class QueryableEntityKeyProvider<T> implements ProvidesKey<T> {

    @Override
    public Object getKey(T item) {
        if (item instanceof Queryable) {
            return ((Queryable) item).getQueryableId();
        }
        return item;
    }

}
