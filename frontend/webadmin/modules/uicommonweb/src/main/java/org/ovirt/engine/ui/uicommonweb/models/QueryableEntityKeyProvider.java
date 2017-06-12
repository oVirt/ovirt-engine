package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.common.businessentities.IVdcQueryable;

import com.google.gwt.view.client.ProvidesKey;

public class QueryableEntityKeyProvider<T> implements ProvidesKey<T> {

    @Override
    public Object getKey(T item) {
        if (item instanceof IVdcQueryable) {
            return ((IVdcQueryable) item).getQueryableId();
        }
        return item;
    }

}
