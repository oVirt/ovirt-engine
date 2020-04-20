package org.ovirt.engine.ui.common.widget.uicommon.storage;

import static java.lang.Math.ceil;
import static java.lang.Math.min;

import java.util.ArrayList;
import java.util.Collection;

import org.ovirt.engine.ui.common.widget.table.PagingDataProvider;

/**
 * Provides paging capability by filtering out items from the source that do not belong to the current page.
 */
public class PageFilter implements PagingDataProvider {

    private final int pageSize;
    private int currentPage = 0;
    private int currentSize = 0;

    public static PageFilter create(int pageSize) {
        return new PageFilter(pageSize);
    }

    public PageFilter(int pageSize) {
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than zero."); //$NON-NLS-1$
        }

        this.pageSize = pageSize;
    }

    /**
     * Filter the incoming items according to preserved internal state. If the length of the input changes the internal
     * state is cleared.
     */
    public Collection filter(Collection items) {
        if (items == null || items.isEmpty()) {
            currentSize = 0;
            currentPage = 0;
            return items;
        }

        if (currentSize != items.size() || !isCurrentPageValid()) {
            currentSize = items.size();
            currentPage = 0;
        }

        return new ArrayList(items).subList(getFirstItemOnPage(), getLastItemOnPage() + 1);
    }

    private boolean isCurrentPageValid() {
        return currentPage >= 0 && currentPage < numberOfPages(currentSize, pageSize);
    }

    private int numberOfPages(int size, int pageSize) {
        return (int) ceil((double) size / pageSize);
    }

    public void goForward() {
        if (canGoForward()) {
            currentPage++;
        }
    }

    public void goBack() {
        if (canGoBack()) {
            currentPage--;
        }
    }

    @Override
    public void refresh() {

    }

    public boolean canGoForward() {
        return currentPage < numberOfPages(currentSize, pageSize) - 1;
    }

    public boolean canGoBack() {
        return currentPage > 0;
    }

    @Override
    public int getFirstItemOnPage() {
        if (currentSize <= 0) {
            return -1;
        }
        return currentPage * pageSize;
    }

    @Override
    public int getLastItemOnPage() {
        return min(currentSize, (currentPage + 1) * pageSize) - 1;
    }

    @Override
    public int getTotalItemsCount() {
        return currentSize;
    }
}
