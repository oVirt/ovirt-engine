package org.ovirt.engine.ui.common.widget.table;

public interface PagingDataProvider extends HasPaging {
    /**
     * Get the index (in the result list) of the first item on the current page. The returned index is zero based.
     * <p>
     * <b>Example</b> Given:
     * <pre>
     *     results = [ "a", "b", "c", "d"]
     *     pageSize = 2
     *     currentPage = 2
     * </pre>
     * The first item on the second page is "c" so the method should return <code>2</code>.
     * </p>
     * @return index of the first item on the current page or <code>-1</code> if no items
     */
    int getFirstItemOnPage();

    /**
     * Get the index (in the result list) of the last item on the current page. The returned index is zero based.
     * <p>
     * <b>Example</b> Given:
     * <pre>
     *     results = [ "a", "b", "c", "d"]
     *     pageSize = 2
     *     currentPage = 2
     * </pre>
     * The last item on the second page is "d" so the method should return <code>3</code>.
     * </p>
     * @return index of the last item on the current page or <code>-1</code> if no items
     */
    int getLastItemOnPage();

    /**
     * @return result list size or <code>-1</code> if not available
     */
    int getTotalItemsCount();

}
