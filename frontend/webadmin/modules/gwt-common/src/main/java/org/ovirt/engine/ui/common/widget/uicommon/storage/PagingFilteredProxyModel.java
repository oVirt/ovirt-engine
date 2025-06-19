package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStoragePartialModel;

/**
 * Create a proxy model that will intercept {@link SanStorageModelBase#getItems()} and filter the items according filter
 * and then filter the items that belong to the current page. From a logical point of view all data is still one model
 * i.e. user changes are not lost when changing page or applying filter and submitting.
 */
public class PagingFilteredProxyModel extends PagingProxyModel {

    private final LunFilter lunFilter;

    public PagingFilteredProxyModel(SanStorageModelBase model, PageFilter pageFilter, LunFilter lunFilter) {
        super(model, pageFilter);
        this.lunFilter = lunFilter;
    }

    public static SanStoragePartialModel create(PageFilter pageFilter, LunFilter lunFilter, SanStorageModelBase model) {
        return new PagingFilteredProxyModel(model, pageFilter, lunFilter);
    }

    @Override
    public Collection getItems() {
        Collection items = getModel().getItems();
        if (lunFilter != null) {
            items = lunFilter.filter(items);
        }
        return getPageFilter().filter(items);
    }
}
