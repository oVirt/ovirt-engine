package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.Collection;

import org.ovirt.engine.ui.uicommonweb.models.storage.SanStorageModelBase;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStoragePartialModel;

/**
 * Create a proxy model that will intercept {@link SanStorageModelBase#getItems()} and filter the items that belong to
 * the current page. From a logical point of view all pages are still one model i.e. user changes are not lost when
 * changing page and submitting.
 */
public class PagingProxyModel extends ProxyModelBase {

    private final PageFilter pageFilter;

    public PagingProxyModel(SanStorageModelBase model, PageFilter pageFilter) {
        super(model);
        this.pageFilter = pageFilter;
    }

    public static SanStoragePartialModel create(PageFilter pageFilter, SanStorageModelBase model) {
        return new PagingProxyModel(model, pageFilter);
    }

    @Override
    public Collection getItems() {
        return pageFilter.filter(getModel().getItems());
    }

    protected PageFilter getPageFilter() {
        return pageFilter;
    }
}
