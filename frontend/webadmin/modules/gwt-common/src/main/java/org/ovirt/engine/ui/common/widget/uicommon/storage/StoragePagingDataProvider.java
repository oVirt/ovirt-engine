package org.ovirt.engine.ui.common.widget.uicommon.storage;

import org.ovirt.engine.ui.common.widget.table.PagingDataProvider;

public class StoragePagingDataProvider implements PagingDataProvider {

    private final PageFilter pageFilter;
    private final SanStorageLunToTargetList sanStorageLunToTargetList;

    public StoragePagingDataProvider(PageFilter pageFilter, SanStorageLunToTargetList sanStorageLunToTargetList) {
        this.pageFilter = pageFilter;
        this.sanStorageLunToTargetList = sanStorageLunToTargetList;
    }

    public static PagingDataProvider create(PageFilter pageFilter,
            SanStorageLunToTargetList sanStorageLunToTargetList) {
        return new StoragePagingDataProvider(pageFilter, sanStorageLunToTargetList);
    }

    @Override
    public boolean canGoForward() {
        return pageFilter.canGoForward();
    }

    @Override
    public boolean canGoBack() {
        return pageFilter.canGoBack();
    }

    @Override
    public void goForward() {
        pageFilter.goForward();
        sanStorageLunToTargetList.updateItems();
    }

    @Override
    public void goBack() {
        pageFilter.goBack();
        sanStorageLunToTargetList.updateItems();
    }

    @Override
    public void refresh() {
        sanStorageLunToTargetList.updateItems();
    }

    @Override
    public int getFirstItemOnPage() {
        return pageFilter.getFirstItemOnPage();
    }

    @Override
    public int getLastItemOnPage() {
        return pageFilter.getLastItemOnPage();
    }

    @Override
    public int getTotalItemsCount() {
        return pageFilter.getTotalItemsCount();
    }
}
