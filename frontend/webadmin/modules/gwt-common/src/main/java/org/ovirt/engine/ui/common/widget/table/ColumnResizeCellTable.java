package org.ovirt.engine.ui.common.widget.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.utils.JqueryUtils;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractCheckboxHeader;
import org.ovirt.engine.ui.common.widget.table.header.AbstractHeader;
import org.ovirt.engine.ui.common.widget.table.header.ResizableHeader;
import org.ovirt.engine.ui.common.widget.table.header.ResizeableCheckboxHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;
import org.ovirt.engine.ui.uicompat.external.StringUtils;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A {@link CellTable} that supports resizing its columns using mouse.
 * <p>
 * Column resize feature is disabled by default, use {@link #enableColumnResizing} to enable it.
 * <p>
 * Use {@link #initModelSortHandler} method to configure column sorting that works with:
 * <ul>
 *  <li>{@link SortedListModel} - client-side sorting
 *  <li>{@link SearchableListModel} - client-side or server-side sorting
 * </ul>
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnResizeCellTable<T> extends CellTable<T> implements HasResizableColumns<T>, ColumnController<T> {

    /**
     * {@link #emptyNoWidthColumn} header that supports handling context menu events.
     */
    private class EmptyColumnHeader extends Header<String> {

        public EmptyColumnHeader() {
            super(new TextCell() {
                @Override
                public Set<String> getConsumedEvents() {
                    return new HashSet<>(Collections.singletonList(BrowserEvents.CONTEXTMENU));
                }
            });
        }

        @Override
        public String getValue() {
            return null;
        }

        @Override
        public void onBrowserEvent(Context context, Element elem, NativeEvent event) {
            super.onBrowserEvent(context, elem, event);

            if (BrowserEvents.CONTEXTMENU.equals(event.getType())) {
                ensureContextMenuHandler().onContextMenu(event);
            }
        }

    }

    private static final Logger logger = Logger.getLogger(ColumnResizeCellTable.class.getName());

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private static final int DEFAULT_MINIMUM_COLUMN_WIDTH = 30;

    private int minimumColumnWidth = DEFAULT_MINIMUM_COLUMN_WIDTH;

    // Prefix for keys used to store widths of individual columns
    private static final String GRID_COLUMN_WIDTH_PREFIX = "GridColumnWidth"; //$NON-NLS-1$

    // This is 1px instead of 0px as zero-size columns seem to confuse the cell table.
    private static final String HIDDEN_WIDTH = "1px"; //$NON-NLS-1$

    // Context menu event handler attached to all column headers
    private NativeContextMenuHandler headerContextMenuHandler;

    // Empty no-width column used with resizable columns feature
    // that occupies remaining horizontal space within the table
    private final Column<T, ?> emptyNoWidthColumn = new EmptyColumn<>();
    private final Header<?> emptyNoWidthColumnHeader = new EmptyColumnHeader();

    private boolean columnResizingEnabled = false;
    private boolean columnResizePersistenceEnabled = false;
    private boolean applyHeaderStyle = true;

    // used to store column width preferences
    private ClientStorage clientStorage;

    // used to store column width preferences
    private GridController gridController;

    // Column visibility, controlled via ensureColumnVisible method
    private final Map<Column<T, ?>, Boolean> columnVisibleMap = new HashMap<>();

    // Column visibility override, controlled via setColumnVisible method
    private final Map<Column<T, ?>, Boolean> columnVisibleMapOverride = new HashMap<>();

    // Current column widths
    private final Map<Column<T, ?>, String> columnWidthMap = new HashMap<>();

    // Column header context menu popup
    private final ColumnContextPopup<T> contextPopup = new ColumnContextPopup<>(this);

    private boolean headerContextMenuEnabled = false;

    public ColumnResizeCellTable() {
        super();
    }

    public ColumnResizeCellTable(int pageSize, ProvidesKey<T> keyProvider) {
        super(pageSize, keyProvider);
    }

    public ColumnResizeCellTable(int pageSize, CellTable.Resources resources,
            ProvidesKey<T> keyProvider, Widget loadingIndicator) {
        super(pageSize, resources, keyProvider, loadingIndicator);
    }

    public ColumnResizeCellTable(int pageSize, CellTable.Resources resources,
            ProvidesKey<T> keyProvider) {
        super(pageSize, resources, keyProvider);
    }

    public ColumnResizeCellTable(int pageSize, CellTable.Resources resources) {
        super(pageSize, resources);
    }

    public ColumnResizeCellTable(int pageSize) {
        super(pageSize);
    }

    public ColumnResizeCellTable(ProvidesKey<T> keyProvider) {
        super(keyProvider);
    }

    private NativeContextMenuHandler ensureContextMenuHandler() {
        if (headerContextMenuHandler == null) {
            headerContextMenuHandler = new NativeContextMenuHandler() {
                @Override
                public void onContextMenu(NativeEvent event) {
                    event.preventDefault();
                    event.stopPropagation();

                    if (headerContextMenuEnabled) {
                        contextPopup.getContextMenu().update();
                        contextPopup.showAndFitToScreen(event.getClientX(), event.getClientY());
                    }
                }
            };
        }
        return headerContextMenuHandler;
    }

    @Override
    public void addColumn(Column<T, ?> column, Header<?> header) {
        // build resizable headers, if necessary
        if (columnResizingEnabled && header instanceof AbstractCheckboxHeader) {
            header = createResizableCheckboxHeader(header, column);
        }
        else if (columnResizingEnabled) {
            header = createResizableHeader(column, header);
        }
        else if (applyHeaderStyle && header instanceof SafeHtmlHeader) {
            SafeHtmlHeader safeHtmlHeader = (SafeHtmlHeader) header;
            // not using Resizeable header, but still want it to look that way.
            // nonResizeableColumnHeader does that.
            // TODO nonResizeableColumnHeader copy-pastes CSS. fix.
            SafeHtml newValue = templates.nonResizeableColumnHeader(safeHtmlHeader.getValue());
            header = new SafeHtmlHeader(newValue, safeHtmlHeader.getTooltip());
        }

        // actually add the column
        super.addColumn(column, header);

        // Add empty no-width column as the last column
        if (columnResizingEnabled) {
            if (isColumnPresent(emptyNoWidthColumn)) {
                removeColumn(emptyNoWidthColumn);
            }
            super.addColumn(emptyNoWidthColumn, emptyNoWidthColumnHeader);
        }

        // Add column to header context menu
        if (header instanceof AbstractHeader) {
            ((AbstractHeader) header).setContextMenuHandler(ensureContextMenuHandler());
            contextPopup.getContextMenu().addItem(column);
        }
    }

    public void addColumnAndSetWidth(Column<T, ?> column, Header<?> header, String width) {
        addColumn(column, header);
        setColumnWidth(column, width);
    }

    public void addColumn(Column<T, ?> column, String headerText) {
        Header<?> header = new SafeHtmlHeader(SafeHtmlUtils.fromTrustedString(headerText));
        addColumn(column, header);
    }

    public void addColumnAndSetWidth(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    public void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml) {
        Header<?> header = new SafeHtmlHeader(headerHtml);
        addColumn(column, header);
    }

    public void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml, String width) {
        Header<?> header = new SafeHtmlHeader(headerHtml);
        addColumn(column, header);
        setColumnWidth(column, width);
    }

    protected Header<?> createResizableHeader(Column<T, ?> column, Header<?> header) {
        if (header instanceof SafeHtmlHeader) {
            SafeHtmlHeader safeHtmlHeader = (SafeHtmlHeader) header;
            return new ResizableHeader<>(safeHtmlHeader, column, this, applyHeaderStyle);
        }
        return header;
    }

    /**
     * Wraps the given {@code header} passed from user code, if necessary.
     * <p>
     * This method is called whenever a column is added to the table.
     */
    protected Header<?> createResizableCheckboxHeader(Header<?> header, Column<T, ?> column) {
        if (header instanceof AbstractCheckboxHeader) {
            return new ResizeableCheckboxHeader<>((AbstractCheckboxHeader) header, column, this);
        }
        return header;
    }

    /**
     * Ensures that the given column is visible or hidden.
     */
    public void ensureColumnVisible(Column<T, ?> column, String headerText, boolean visible) {
        ensureColumnVisible(column, SafeHtmlUtils.fromTrustedString(headerText), visible);
    }

    /**
     * Ensures that the given column is visible or hidden.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtml headerHtml, boolean visible) {
        ensureColumnVisible(column, new SafeHtmlHeader(headerHtml), visible, null);
    }

    /**
     * Ensures that the given column is visible or hidden.
     */
    public void ensureColumnVisible(Column<T, ?> column, String headerText, boolean visible, String width) {
        ensureColumnVisible(column, SafeHtmlUtils.fromTrustedString(headerText), visible, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtml headerHtml, boolean visible, String width) {
        ensureColumnVisible(column, new SafeHtmlHeader(headerHtml), visible, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtmlHeader header, boolean visible, String width) {
        ensureColumnVisible(column, header, visible, width, true);
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     *
     * @param column The column to update.
     * @param header The header for the column (used only when adding new column).
     * @param visible {@code true} to ensure the column is visible, {@code false} to ensure the column is hidden.
     * @param width The width of the column.
     * @param removeFromContextMenuIfNotVisible {@code true} to remove corresponding context menu item when the column is to be hidden.
     */
    private void ensureColumnVisible(Column<T, ?> column, SafeHtmlHeader header, boolean visible, String width,
            boolean removeFromContextMenuIfNotVisible) {
        if (!isColumnPresent(column)) {
            // Add the column
            if (width == null) {
                addColumn(column, header);
            } else {
                addColumnAndSetWidth(column, header, width);
            }
        }

        columnVisibleMap.put(column, visible);

        if (columnResizePersistenceEnabled) {
            String persistedWidth = readColumnWidth(column);
            if (persistedWidth != null) {
                width = persistedWidth;
            }
        }

        setColumnWidth(column, width);

        // Update header context menu
        if (removeFromContextMenuIfNotVisible && !visible) {
            contextPopup.getContextMenu().removeItem(column);
        } else if (removeFromContextMenuIfNotVisible && !contextPopup.getContextMenu().containsItem(column)) {
            contextPopup.getContextMenu().addItem(column);
        }

        contextPopup.getContextMenu().update();
    }

    @Override
    public void setColumnWidth(Column<T, ?> column, String width) {
        boolean columnVisible = isColumnVisible(column);

        if (columnVisible) {
            columnWidthMap.put(column, width);
        } else {
            width = HIDDEN_WIDTH;
        }

        // Update header cell visibility
        TableCellElement headerCell = getHeaderCell(getElement().<TableElement> cast(), getColumnIndex(column));
        if (headerCell != null) {
            headerCell.getStyle().setVisibility(columnVisible ? Visibility.VISIBLE : Visibility.HIDDEN);
        }

        // Prevent resizing of "hidden" (1px wide) columns
        if (columnResizingEnabled) {
            Header<?> header = getHeader(getColumnIndex(column));
            if (header instanceof ResizableHeader) {
                ((ResizableHeader<?>) header).setResizeEnabled(columnVisible);
            }
        }

        super.setColumnWidth(column, width);
    }

    private TableCellElement getHeaderCell(TableElement tableElement, int columnIndex) {
        TableSectionElement tHeadElement = tableElement.getTHead();
        if (tHeadElement == null) {
            return null;
        }

        List<TableCellElement> cells = getCells(tHeadElement.getRows(), columnIndex);
        return (cells.size() == 1) ? cells.get(0) : null;
    }

    private List<TableCellElement> getCells(NodeList<TableRowElement> rows, int columnIndex) {
        List<TableCellElement> result = new ArrayList<>();

        for (int i = 0; i < rows.getLength(); i++) {
            TableCellElement cell = rows.getItem(i).getCells().getItem(columnIndex);
            if (cell != null) {
                result.add(cell);
            }
        }

        return result;
    }

    private boolean isColumnPresent(Column<T, ?> column) {
        return getColumnIndex(column) != -1;
    }

    @Override
    public String getColumnContextMenuTitle(Column<T, ?> column) {
        if (!isColumnPresent(column)) {
            return null;
        }

        Header<?> header = getHeader(getColumnIndex(column));
        String title = null;

        if (column instanceof AbstractColumn) {
            // Might return null (no custom title defined)
            title = ((AbstractColumn) column).getContextMenuTitle();
        }
        if (title == null && header instanceof SafeHtmlHeader) {
            // Might return empty string (header's HTML contains no text)
            title = JqueryUtils.getTextFromHtml(((SafeHtmlHeader) header).getValue().asString());
        }
        if (StringUtils.isEmpty(title)) {
            title = constants.missingColumnContextMenuTitle();
            logger.warning("Column with missing context menu title at index " + getColumnIndex(column)); //$NON-NLS-1$
        }

        return title;
    }

    @Override
    public boolean isColumnVisible(Column<T, ?> column) {
        if (!isColumnPresent(column)) {
            return false;
        }

        // Columns are visible by default
        boolean visible = true;

        if (columnVisibleMap.containsKey(column)) {
            visible = columnVisibleMap.get(column);
        }

        if (visible && columnVisibleMapOverride.containsKey(column)) {
            visible = columnVisibleMapOverride.get(column);
        }

        return visible;
    }

    @Override
    public void setColumnVisible(Column<T, ?> column, boolean visible) {
        if (isColumnPresent(column)) {
            columnVisibleMapOverride.put(column, visible);
            ensureColumnVisible(column, null, visible, columnWidthMap.get(column), false);
        }
    }

    @Override
    public void swapColumns(Column<T, ?> columnOne, Column<T, ?> columnTwo) {
        if (isColumnPresent(columnOne) && isColumnPresent(columnTwo)) {
            int columnOneIndex = getColumnIndex(columnOne);
            int columnTwoIndex = getColumnIndex(columnTwo);
            boolean oneWasBeforeTwo = columnOneIndex < columnTwoIndex;

            // columnOne gets removed first
            int columnTwoRemovalIndex = oneWasBeforeTwo ? columnTwoIndex - 1 : columnTwoIndex;

            int columnOneInsertionIndex = oneWasBeforeTwo ? columnTwoIndex - 1 : columnTwoIndex;
            int columnTwoInsertionIndex = oneWasBeforeTwo ? columnOneIndex : columnOneIndex - 1;

            Header<?> columnOneHeader = getHeader(columnOneIndex);
            Header<?> columnTwoHeader = getHeader(columnTwoIndex);

            removeColumn(columnOneIndex);
            removeColumn(columnTwoRemovalIndex);

            if (oneWasBeforeTwo) {
                insertColumn(columnOneInsertionIndex, columnOne, columnOneHeader);
                insertColumn(columnTwoInsertionIndex, columnTwo, columnTwoHeader);
            } else {
                insertColumn(columnTwoInsertionIndex, columnTwo, columnTwoHeader);
                insertColumn(columnOneInsertionIndex, columnOne, columnOneHeader);
            }

            contextPopup.getContextMenu().update();
        }
    }

    /**
     * Enables header context menu triggered by right-clicking table header area.
     * <p>
     * <em>After calling this method, each column must have non-empty header HTML content <b>or</b>
     * {@linkplain org.ovirt.engine.ui.common.widget.table.column.AbstractColumn#setContextMenuTitle
     * custom context menu title} defined, otherwise the context menu will contain "unnamed column"
     * items.</em>
     */
    public void enableHeaderContextMenu() {
        headerContextMenuEnabled = true;
    }

    /**
     * Allows table columns to be resized by dragging their right-hand border using mouse.
     * <p>
     * This method should be called before calling any {@code addColumn} methods.
     * <p>
     * <em>After calling this method, each column must have an explicit width defined in PX units, otherwise the resize
     * behavior will not function properly.</em>
     */
    public void enableColumnResizing() {
        columnResizingEnabled = true;

        // Column resize implementation needs table-layout:fixed (disable browser-specific table layout algorithm)
        setWidth("100%", true); //$NON-NLS-1$
    }

    @Override
    public void onResizeStart(Column<T, ?> column, Element headerElement) {
        // Don't do anything.
    }

    @Override
    public void onResizeEnd(Column<T, ?> column, Element headerElement) {
        // Redraw the table
        redraw();

        if (columnResizePersistenceEnabled) {
            String width = getColumnWidth(column);
            saveColumnWidth(column, width);
        }
    }

    @Override
    public void resizeColumn(Column<T, ?> column, int newWidth) {
        setColumnWidth(column, newWidth + "px"); //$NON-NLS-1$
    }

    @Override
    public int getMinimumColumnWidth(Column<T, ?> column) {
        return minimumColumnWidth;
    }

    public void setMinimumColumnWidth(int minimumColumnWidth) {
        this.minimumColumnWidth = minimumColumnWidth;
    }

    /**
     * Enables saving this table's column widths to LocalStorage (or a cookie if LocalStorage unsupported).
     */
    public void enableColumnWidthPersistence(ClientStorage clientStorage, GridController gridController) {
        this.clientStorage = clientStorage;
        this.gridController = gridController;

        if (clientStorage != null && gridController != null) {
            columnResizePersistenceEnabled = true;
        }
    }

    protected String getColumnWidthKey(Column<T, ?> column) {
        if (columnResizePersistenceEnabled) {
            return GRID_COLUMN_WIDTH_PREFIX + "_" + getGridElementId() + "_" + getColumnIndex(column); //$NON-NLS-1$ //$NON-NLS-2$
        }
        return null;
    }

    protected String getGridElementId() {
        return gridController.getId();
    }

    protected void saveColumnWidth(Column<T, ?> column, String width) {
        if (columnResizePersistenceEnabled) {
            String key = getColumnWidthKey(column);
            if (key != null) {
                clientStorage.setLocalItem(key, width);
            }
        }
    }

    protected String readColumnWidth(Column<T, ?> column) {
        if (columnResizePersistenceEnabled) {
            String key = getColumnWidthKey(column);
            if (key != null) {
                return clientStorage.getLocalItem(key);
            }
        }
        return null;
    }

    protected void dontApplyResizableHeaderStyle() {
        applyHeaderStyle = false;
    }

    /**
     * Adds column sort handler that works with {@link SortedListModel} (client-side sorting)
     * or {@link SearchableListModel} (client-side or server-side sorting).
     * <p>
     * The sort handler ensures that column sort definition ({@linkplain SortableColumn#getComparator
     * comparator} for client-side sorting, {@linkplain SortableColumn#getSortBy sortBy} for server-side
     * sorting) is propagated to the given model, causing model's item collection to be updated.
     *
     * @param sortedModel
     *            Model for which to configure column sorting.
     */
    @SuppressWarnings("unchecked")
    public void initModelSortHandler(final SortedListModel<T> sortedModel) {
        final SearchableListModel<?, T> searchableModel = (sortedModel instanceof SearchableListModel)
                ? (SearchableListModel<?, T>) sortedModel : null;

        addColumnSortHandler(new ColumnSortEvent.Handler() {
            @Override
            public void onColumnSort(ColumnSortEvent event) {
                Column<?, ?> column = event.getColumn();

                if (column instanceof SortableColumn) {
                    SortableColumn<T, ?> sortableColumn = (SortableColumn<T, ?>) column;
                    boolean sortApplied = false;
                    Comparator<? super T> comparator = sortableColumn.getComparator();
                    boolean supportsServerSideSorting = searchableModel != null && searchableModel.supportsServerSideSorting();

                    // If server-side sorting is supported by the model, but the column
                    // uses Comparator for client-side sorting, use client-side sorting
                    if (supportsServerSideSorting && comparator != null) {
                        sortedModel.setComparator(comparator, event.isSortAscending());
                        sortApplied = true;
                    }

                    // Otherwise, if server-side sorting is supported by the model,
                    // update model's sort options and reload its items via search query
                    else if (supportsServerSideSorting) {
                        sortedModel.setComparator(null);
                        if (searchableModel.isSearchValidForServerSideSorting()) {
                            searchableModel.updateSortOptions(sortableColumn.getSortBy(), event.isSortAscending());
                            sortApplied = true;
                        } else {
                            // Search string not valid, cannot perform search query
                            searchableModel.clearSortOptions();
                        }
                    }

                    // Otherwise, fall back to client-side sorting
                    else if (comparator != null) {
                        sortedModel.setComparator(comparator, event.isSortAscending());
                        sortApplied = true;

                        // SortedListModel.setComparator does not sort the items
                        if (searchableModel == null) {
                            sortedModel.setItems(sortedModel.getItems());
                        }
                    }

                    // Update column sort status, redrawing table headers if necessary
                    ColumnSortInfo columnSortInfo = event.getColumnSortList().get(0);
                    if (sortApplied) {
                        pushColumnSort(columnSortInfo);
                    } else {
                        clearColumnSort();
                    }
                }
            }
        });
    }

    /**
     * Mark a {@linkplain ColumnSortInfo#getColumn column} as sorted.
     *
     * @see #getColumnSortList
     */
    protected void pushColumnSort(ColumnSortInfo columnSortInfo) {
        getColumnSortList().push(columnSortInfo);
    }

    /**
     * Mark all columns as un-sorted.
     *
     * @see #getColumnSortList
     */
    protected void clearColumnSort() {
        getColumnSortList().clear();
    }

    /**
     * Adds a {@link CellPreviewEvent} handler for double-click event
     * simulated as two {@code click} events fired in succession.
     */
    public void addSimulatedDoubleClickHandler(final CellPreviewEvent.Handler<T> handler) {
        addCellPreviewHandler(new CellPreviewEvent.Handler<T>() {

            private static final long DOUBLE_CLICK_THRESHOLD = 300; // Milliseconds
            private long lastClick = -1;

            @Override
            public void onCellPreview(CellPreviewEvent<T> event) {
                if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())) {
                    long click = System.currentTimeMillis();

                    if (lastClick > 0 && (click - lastClick < DOUBLE_CLICK_THRESHOLD)) {
                        handler.onCellPreview(event);
                        lastClick = -1;
                    } else {
                        lastClick = click;
                    }
                }
            }

        });
    }

}
