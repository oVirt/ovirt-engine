package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;

/**
 * Create a proxy model that will intercept {@link SanStorageModelBase#getItems()} and filter the items.
 * From a logical point of view all data is still one model i.e. user changes are not lost when
 * applying the filter and submitting.
 */
public class FilteredProxyModel extends ProxyModelBase {

    private final ModelFilter<?> filter;

    public FilteredProxyModel(SanStorageModelBase model, ModelFilter<?> filter) {
        super(model);
        this.filter = filter;
    }

    public static FilteredProxyModel create(ModelFilter<?> filter, SanStorageModelBase model) {
        return new FilteredProxyModel(model, filter);
    }

    @Override
    public Collection getItems() {
        return filter.filter(getModel().getItems());
    }
}
