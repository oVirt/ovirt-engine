package org.ovirt.engine.ui.common.widget.table.resize;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractCheckboxHeader;
import org.ovirt.engine.ui.common.widget.table.header.ResizableHeader;
import org.ovirt.engine.ui.common.widget.table.header.ResizeableCheckboxHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;

import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
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
public class ColumnResizeCellTable<T> extends CellTable<T> implements HasResizableColumns<T> {

    private static final int DEFAULT_MINIMUM_COLUMN_WIDTH = 30;

    private int minimumColumnWidth = DEFAULT_MINIMUM_COLUMN_WIDTH;

    // Prefix for keys used to store widths of individual columns
    private static final String GRID_COLUMN_WIDTH_PREFIX = "GridColumnWidth"; //$NON-NLS-1$

    //This is 1px instead of 0px as 0 size columns seem to confuse the cell table.
    private static final String HIDDEN_WIDTH = "1px"; //$NON-NLS-1$

    // Empty, no-width column used with resizable columns feature
    // that occupies remaining horizontal space within the table
    private Column<T, ?> emptyNoWidthColumn;

    private boolean columnResizingEnabled = false;
    private boolean columnResizePersistenceEnabled = false;
    private boolean applyHeaderStyle = true;

    // used to store column width preferences
    private ClientStorage clientStorage;

    // used to store column width preferences
    private GridController gridController;

    private final static CommonApplicationTemplates templates = AssetProvider.getTemplates();

    private final Map<Integer, Boolean> columnVisibleMap = new HashMap<Integer, Boolean>();

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

        // add emptyNoWidthColumn if necessary
        if (columnResizingEnabled) {
            if (emptyNoWidthColumn != null) {
                removeColumn(emptyNoWidthColumn);
            }

            // Add empty, no-width column as the last column
            emptyNoWidthColumn = new EmptyColumn<T>();
            super.addColumn(emptyNoWidthColumn);
        }
    }

    public void addColumnAndSetWidth(Column<T, ?> column, Header<?> header, String width) {
        addColumn(column, header);
        setColumnWidth(column, width);
    }

    public void addColumn(Column<T, ?> column, String headerText) {
        Header<?> header = new SafeHtmlHeader(SafeHtmlUtils.fromString(headerText));
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
            return new ResizableHeader<T>(safeHtmlHeader, column, this, applyHeaderStyle);
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
            return new ResizeableCheckboxHeader<T>((AbstractCheckboxHeader) header, column, this);
        }
        return header;
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, String headerText, boolean present) {
        ensureColumnPresent(column, SafeHtmlUtils.fromString(headerText), present);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, SafeHtml headerHtml, boolean present) {
        ensureColumnPresent(column, new SafeHtmlHeader(headerHtml), present, null);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, String headerText, boolean present, String width) {
        ensureColumnPresent(column, SafeHtmlUtils.fromString(headerText), present, width);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, SafeHtml headerHtml, boolean present, String width) {
        ensureColumnPresent(column, new SafeHtmlHeader(headerHtml), present, width);
    }


    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     * <p>
     * This method also sets the column width in case the column needs to be added.
     * @param column The column to ensure is there.
     * @param header The header
     * @param present If true make sure the column is there, if false make sure it is not.
     * @param width The width of the column.
     */
    public void ensureColumnPresent(Column<T, ?> column, SafeHtmlHeader header, boolean present, String width) {
        Integer index = getColumnIndex(column);
        boolean columnPresent = index != -1;
        if (!columnPresent) {
            // Add the column
            if (width == null) {
                addColumn(column, header);
            } else {
                addColumnAndSetWidth(column, header, width);
            }
            index = getColumnIndex(column);
        }
        columnVisibleMap.put(index, present);
        if (columnResizePersistenceEnabled) {
            String persistedWidth = readColumnWidth(column);
            if (persistedWidth != null) {
                width = persistedWidth;
            }
        }
        setColumnWidth(column, width);
    }

    @Override
    public void setColumnWidth(Column<T, ?> column, String width) {
        Integer index = getColumnIndex(column);
        if (columnVisibleMap.get(index) != null && !columnVisibleMap.get(index)) {
            width = HIDDEN_WIDTH;
        }

        super.setColumnWidth(column, width);
    }

    List<TableCellElement> getTableBodyCells(TableElement tableElement, int columnIndex) {
        TableSectionElement firstTBodyElement = tableElement.getTBodies().getItem(0);
        return firstTBodyElement != null ? getCells(firstTBodyElement.getRows(), columnIndex)
                : Collections.<TableCellElement> emptyList();
    }

    List<TableCellElement> getTableHeaderCells(TableElement tableElement, int columnIndex) {
        TableSectionElement tHeadElement = tableElement.getTHead();
        return tHeadElement != null ? getCells(tHeadElement.getRows(), columnIndex)
                : Collections.<TableCellElement> emptyList();
    }

    List<TableCellElement> getCells(NodeList<TableRowElement> rows, int columnIndex) {
        List<TableCellElement> result = new ArrayList<TableCellElement>();
        for (int i = 0; i < rows.getLength(); i++) {
            TableCellElement cell = rows.getItem(i).getCells().getItem(columnIndex);
            if (cell != null) {
                result.add(cell);
            }
        }
        return result;
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
     * @param clientStorage
     * @param gridController
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

                    // Apply server-side sorting, if supported by the model
                    if (searchableModel != null && searchableModel.supportsServerSideSorting()) {
                        if (searchableModel.isSearchValidForServerSideSorting()) {
                            searchableModel.updateSortOptions(sortableColumn.getSortBy(), event.isSortAscending());
                            sortApplied = true;
                        } else {
                            searchableModel.clearSortOptions();
                        }
                    }

                    // Otherwise, fall back to client-side sorting
                    else {
                        Comparator<? super T> comparator = sortableColumn.getComparator();
                        if (comparator != null) {
                            sortedModel.setComparator(comparator, event.isSortAscending());
                            sortApplied = true;

                            // SortedListModel.setComparator does not sort the items
                            if (searchableModel == null) {
                                sortedModel.setItems(sortedModel.getItems());
                            }
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
