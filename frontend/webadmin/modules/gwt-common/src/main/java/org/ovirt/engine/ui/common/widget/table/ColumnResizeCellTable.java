package org.ovirt.engine.ui.common.widget.table;

import static org.ovirt.engine.ui.common.system.StorageKeyUtils.GRID_COLUMN_WIDTH_PREFIX;
import static org.ovirt.engine.ui.common.system.StorageKeyUtils.GRID_HIDDEN_COLUMN_WIDTH_PREFIX;
import static org.ovirt.engine.ui.common.system.StorageKeyUtils.GRID_SWAPPED_COLUMN_LIST_SUFFIX;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.ovirt.engine.core.compat.IntegerCompat;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.css.PatternflyConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.system.StorageKeyUtils;
import org.ovirt.engine.ui.common.uicommon.ClientAgentType;
import org.ovirt.engine.ui.common.uicommon.model.DefaultModelItemComparator;
import org.ovirt.engine.ui.common.utils.JqueryUtils;
import org.ovirt.engine.ui.common.widget.WindowHelper;
import org.ovirt.engine.ui.common.widget.table.column.AbstractColumn;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.table.column.SortableColumn;
import org.ovirt.engine.ui.common.widget.table.header.AbstractCheckboxHeader;
import org.ovirt.engine.ui.common.widget.table.header.AbstractHeader;
import org.ovirt.engine.ui.common.widget.table.header.ResizableHeader;
import org.ovirt.engine.ui.common.widget.table.header.ResizeableCheckboxHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.HasCleanup;
import org.ovirt.engine.ui.uicommonweb.models.GridController;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicommonweb.models.SortedListModel;

import com.google.gwt.cell.client.Cell.Context;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Unit;
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
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.ProvidesKey;

/**
 * A {@link CellTable} that supports resizing its columns using mouse.
 * <p>
 * Column resize feature is disabled by default, use {@link #enableColumnResizing} to enable it.
 * <p>
 * Use {@link #initModelSortHandler} to configure Model-based column sorting that works with:
 * <ul>
 *  <li>{@link SortedListModel} - client-side sorting
 *  <li>{@link SearchableListModel} - client-side or server-side sorting
 * </ul>
 *
 * @param <T>
 *            Table row data type.
 */
public class ColumnResizeCellTable<T> extends DataGrid<T> implements HasResizableColumns<T>, ColumnController<T>,
    HasCleanup {

    private static final String GRID_HIDDEN = StorageKeyUtils.GRID_HIDDEN;
    private static final String GRID_VISIBLE = StorageKeyUtils.GRID_VISIBLE;
    private static final String HIDE_ONE_ROW_SCROLL = "hide-one-row-scroll"; // $NON-NLS-1$

    private static final int CHROME_HEIGHT_ADJUST = 2;
    private static final int FF_HEIGHT_ADJUST = 3;
    private static final int IE_HEIGHT_ADJUST = 3;

    // The height of a row of data in the grid. I wish I could dynamically detect this.
    protected static final int ROW_HEIGHT = 26;
    // The minimum height needed to properly display the loading throbber.
    protected static final int LOADING_HEIGHT = 96;

    private static final ClientAgentType clientAgentType = new ClientAgentType();
    protected static final int scrollbarThickness = WindowHelper.determineScrollbarThickness();

    protected boolean isHeightSet = false;
    protected int maxGridHeight = -1;

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

    // Legacy way of hiding columns - preserved for reading legacy column width data from storage
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

    // Default column widths
    private final Map<Column<T, ?>, String> defaultWidthMap = new HashMap<>();

    // Mapping of column indexes to swapped indexes
    private final Map<Integer, Integer> realToSwappedIndexes = new HashMap<>();
    private final List<Column<T, ?>> unaddedColumns = new ArrayList<>();
    private final List<Header<?>> unaddedHeaders = new ArrayList<>();
    private final Map<Column<T, ?>, String> unaddedColumnWidths = new HashMap<>();
    // Columns not displayed in default configuration (user can override that)
    private final Set<Column<T, ?>> hiddenByDefault = new HashSet<>();
    private int maxSwappedIndex = -1;

    // Column header context menu popup
    private final ColumnContextPopup<T> contextPopup = new ColumnContextPopup<>(this);

    private boolean headerContextMenuEnabled = false;

    private int dragIndex = ColumnController.NO_DRAG;

    public ColumnResizeCellTable() {
        super();
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    public ColumnResizeCellTable(int pageSize, ProvidesKey<T> keyProvider) {
        super(pageSize, keyProvider);
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    public ColumnResizeCellTable(int pageSize, DataGrid.Resources resources,
            ProvidesKey<T> keyProvider, Widget loadingIndicator) {
        super(pageSize, resources, keyProvider, loadingIndicator);
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    public ColumnResizeCellTable(int pageSize, DataGrid.Resources resources,
            ProvidesKey<T> keyProvider) {
        super(pageSize, resources, keyProvider, null);
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    public ColumnResizeCellTable(int pageSize, DataGrid.Resources resources) {
        super(pageSize, resources);
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    public ColumnResizeCellTable(int pageSize) {
        super(pageSize);
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    public ColumnResizeCellTable(ProvidesKey<T> keyProvider) {
        super(keyProvider);
        addStyleName(PatternflyConstants.PF_TABLE_BORDERED);
    }

    protected void popuplateSwappedList() {
        String swappedColumnKey = getSwappedColumnListKey();
        if (swappedColumnKey != null) {
            String swappedColumns = clientStorage.getLocalItem(swappedColumnKey);
            if (swappedColumns != null) {
                // Data is stored in n=m,x=y,m=n,y=x format. so we can split on , and then on = to get mappings from the
                // storage.
                String[] split = swappedColumns.split(","); //$NON-NLS-1$
                for (int i = 0; i < split.length; i++) {
                    String[] tuple = split[i].split("="); //$NON-NLS-1$
                    if (tuple.length == 2) {
                        realToSwappedIndexes.put(Integer.parseInt(tuple[1]), Integer.parseInt(tuple[0]));
                        maxSwappedIndex = Math.max(maxSwappedIndex, Integer.parseInt(tuple[0]));
                    }
                }
            }
        }
    }

    public NativeContextMenuHandler ensureContextMenuHandler() {
        if (headerContextMenuHandler == null) {
            headerContextMenuHandler = event -> {
                event.preventDefault();
                event.stopPropagation();

                if (headerContextMenuEnabled) {
                    contextPopup.getContextMenu().update();
                    contextPopup.showAndFitToScreen(event.getClientX(), event.getClientY());
                }
            };
        }
        return headerContextMenuHandler;
    }

    @Override
    public void addColumn(Column<T, ?> column, Header<?> header) {
        // If for some reason we have less columns than maxSwappedIndex (because we removed a column in the code)
        // this will break as no columns are ever added to the grid.
        if (maxSwappedIndex >= 0) {
            unaddedColumns.add(column);
            unaddedHeaders.add(header);
            if (unaddedColumns.size() == maxSwappedIndex + 1) {
                // Got the last column needed before we can properly add them in the right order.
                // We have a list of columns in the order in which they were added in unaddedColumns/headers.
                // We have a map of original to target indexes, now we can loop over the indexes, and if the map
                // target matches the current index, we look up the source index and add that one instead.
                for (int i = 0; i < unaddedColumns.size(); i++) {
                    int originalIndex = determineOriginalIndex(i);
                    Column<T, ?> unaddedColumn = unaddedColumns.get(originalIndex);
                    addColumnImpl(unaddedColumn, unaddedHeaders.get(originalIndex));
                    setColumnWidth(unaddedColumn, unaddedColumnWidths.get(unaddedColumn), false);
                }
                // clean up the book keeping.
                unaddedColumns.clear();
                unaddedHeaders.clear();
                unaddedColumnWidths.clear();
                // If more columns are added, they will be added in the order they are added.
                maxSwappedIndex = -1;
            }
        } else {
            addColumnImpl(column, header);
        }
    }

    private void addColumnImpl(Column<T, ?> column, Header<?> header) {
        // build resizable headers, if necessary
        if (columnResizingEnabled && header instanceof AbstractCheckboxHeader) {
            header = createResizableCheckboxHeader(header, column);
        } else if (columnResizingEnabled) {
            header = createResizableHeader(column, header);
        } else if (applyHeaderStyle && header instanceof SafeHtmlHeader) {
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

    protected int calculateGridHeight(int rowCount) {
        int height = getLoadingIndicator() != null ? LOADING_HEIGHT : ROW_HEIGHT;
        if (rowCount > 0) {
            height = rowCount * ROW_HEIGHT;
        }
        return height;
    }

    public void updateGridSize() {
        updateGridSize(calculateGridHeight(getRowCount()));
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
        if (unaddedColumns.indexOf(column) < 0) {
            setColumnWidth(column, width, false);
        } else {
            unaddedColumnWidths.put(column, width);
        }
    }

    public void setColumnWidth(Column<T, ?> column, String width, boolean overridePersist) {
        if (!defaultWidthMap.containsKey(column)) {
            // treat first width ever set as the default one
            defaultWidthMap.put(column, width);
        }

        boolean columnVisible = isColumnVisible(column);

        if (columnVisible) {
            columnWidthMap.put(column, width);
        }

        // Update header cell visibility
        TableCellElement headerCell = getHeaderCell(getElement().cast(), getColumnIndex(column));
        if (headerCell != null) {
            headerCell.getStyle().setVisibility(columnVisible ? Visibility.VISIBLE : Visibility.HIDDEN);
        }

        if (columnResizePersistenceEnabled && !overridePersist && columnVisible) {
            String persistedWidth = readColumnWidth(column);
            if (persistedWidth != null) {
                width = persistedWidth;
            }
        }

        super.setColumnWidth(column, width);
        column.setCellStyleNames(columnVisible ? GRID_VISIBLE : GRID_HIDDEN);

        int index = getColumnIndex(column);
        if (index >= 0) {
            removeColumnStyleName(index, columnVisible ? GRID_HIDDEN : GRID_VISIBLE);
            addColumnStyleName(index, columnVisible ? GRID_VISIBLE : GRID_HIDDEN);
        }
        redraw();
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

    public void markColumnAsHiddenByDefault(Column<T, ?> column) {
        hiddenByDefault.add(column);
    }

    public boolean isHiddenByDefault(Column<T, ?> column) {
        return hiddenByDefault.contains(column);
    }

    public boolean isVisibleOnUserRequest(Column<T, ?> column) {
        if (!isHiddenByDefault(column)) {
            return false;
        }

        return getVisibleByUserRequestColumns().contains(column);
    }

    private Set<Column<T, ?>> getVisibleByUserRequestColumns() {
        String key = getVisibleByUserRequestListKey();
        if (key == null) {
            return Collections.emptySet();
        }

        String encodedList = clientStorage.getLocalItem(key);
        if (encodedList == null) {
            return Collections.emptySet();
        }
        // format: comma separated list of (original) indices
        return Arrays.stream(encodedList.split(",")) //$NON-NLS-1$
                .map(IntegerCompat::tryParse)
                .filter(index -> index != null)
                .map(index -> determineRealIndex(index))
                // forward compatibility - column count could change
                .filter(realIndex -> realIndex >= 0 && realIndex < getColumnCount())
                .map(realIndex -> getColumn(realIndex))
                .collect(Collectors.toSet());
    }

    private void addToVisibleByUserRequest(Column<T, ?> column) {
        Set<Column<T, ?>> alreadyStored = getVisibleByUserRequestColumns();
        if(!columnResizePersistenceEnabled || alreadyStored.contains(column)) {
            return;
        }

        Set<Column<T, ?>> toBeStored = new HashSet<>(alreadyStored);
        toBeStored.add(column);

        storeVisibleByUserRequestList(toBeStored);
    }

    private void storeVisibleByUserRequestList(Set<Column<T, ?>> toBeStored) {
        String key = getVisibleByUserRequestListKey();
        if( key == null) {
            return;
        }

        if(toBeStored.isEmpty()) {
            clientStorage.removeRemoteItem(key);
            return;
        }

        String encodedList = toBeStored.stream()
                // forward compatibility - column could be made visible by default
                .filter(this::isHiddenByDefault)
                .map(this::getColumnIndex)
                .map(this::determineOriginalIndex)
                .map(String::valueOf)
                .collect(Collectors.joining(","));  //$NON-NLS-1$

        clientStorage.setRemoteItem(key, encodedList);
    }

    private void removeFromVisibleByUserRequest(Column<T, ?> column) {
        Set<Column<T, ?>> visibleColumns = getVisibleByUserRequestColumns();
        if(!columnResizePersistenceEnabled || !visibleColumns.contains(column) || !isHiddenByDefault(column)) {
            return;
        }

        Set<Column<T, ?>> toBeStored = new HashSet<>(visibleColumns);
        toBeStored.remove(column);

        storeVisibleByUserRequestList(toBeStored);
    }

    private String getVisibleByUserRequestListKey() {
        if (columnResizePersistenceEnabled) {
            return GRID_VISIBLE + "_" + getGridElementId(); //$NON-NLS-1$
        }
        return null;
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
        if (StringHelper.isNullOrEmpty(title)) {
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

        visible = visible && getHiddenPersistedColumnWidth(column) == null;
        return visible;
    }

    @Override
    public void setColumnVisible(Column<T, ?> column, boolean visible) {
        if (isColumnPresent(column)) {
            columnVisibleMapOverride.put(column, visible);
            String persistedWidth = getHiddenPersistedColumnWidth(column);
            String columnWidth = persistedWidth != null ? persistedWidth : columnWidthMap.get(column);
            persistColumnVisibility(column, visible);
            ensureColumnVisible(column, null, visible, columnWidth, false);
        }
    }

    @Override
    public void swapColumns(Column<T, ?> columnOne, Column<T, ?> columnTwo) {
        swapColumns(columnOne, columnTwo, true);
    }

    private void swapColumns(Column<T, ?> columnOne, Column<T, ?> columnTwo, boolean persistSwap) {
        if (isColumnPresent(columnOne) && isColumnPresent(columnTwo)) {
            int columnOneIndex = getColumnIndex(columnOne);
            int columnTwoIndex = getColumnIndex(columnTwo);
            int originalIndexColumnOne = determineOriginalIndex(columnOneIndex);
            int originalIndexColumnTwo = determineOriginalIndex(columnTwoIndex);
            boolean columnOneVisible = isColumnVisible(columnOne);
            boolean columnTwoVisible = isColumnVisible(columnTwo);
            boolean oneWasBeforeTwo = columnOneIndex < columnTwoIndex;

            // columnOne gets removed first
            int columnTwoRemovalIndex = oneWasBeforeTwo ? columnTwoIndex - 1 : columnTwoIndex;

            int columnOneInsertionIndex = oneWasBeforeTwo ? columnTwoIndex - 1 : columnTwoIndex;
            int columnTwoInsertionIndex = oneWasBeforeTwo ? columnOneIndex : columnOneIndex - 1;

            Header<?> columnOneHeader = getHeader(columnOneIndex);
            Header<?> columnTwoHeader = getHeader(columnTwoIndex);

            removeColumn(columnOneIndex);
            removeColumn(columnTwoRemovalIndex);

            // Make both columns visible for swapping.
            setColumnVisible(columnOne, true);
            setColumnVisible(columnTwo, true);
            if (oneWasBeforeTwo) {
                insertColumn(columnOneInsertionIndex, columnOne, columnOneHeader);
                insertColumn(columnTwoInsertionIndex, columnTwo, columnTwoHeader);
            } else {
                insertColumn(columnTwoInsertionIndex, columnTwo, columnTwoHeader);
                insertColumn(columnOneInsertionIndex, columnOne, columnOneHeader);
            }
            if (persistSwap) {
                realToSwappedIndexes.put(columnOneIndex, originalIndexColumnTwo);
                realToSwappedIndexes.put(columnTwoIndex, originalIndexColumnOne);

                storeSwappedIndexMap();
            }
            // Make both columns visible for swapping.
            setColumnVisible(columnOne, columnOneVisible);
            setColumnVisible(columnTwo, columnTwoVisible);
            contextPopup.getContextMenu().update();
        }
    }

    private int determineOriginalIndex(int index) {
        Integer result = realToSwappedIndexes.get(index);
        if (result != null) {
            return result;
        }
        return index;
    }

    private int determineRealIndex(int originalIndex) {
        for (Map.Entry<Integer, Integer> entry: realToSwappedIndexes.entrySet()) {
            if( entry.getValue().intValue() == originalIndex) {
                return entry.getKey();
            }
        }
        return originalIndex;
    }


    private void storeSwappedIndexMap() {
        String value = realToSwappedIndexes.entrySet().stream().map(
                entry -> entry.getValue() + "=" + entry.getKey()).collect(Collectors.joining(",")); //$NON-NLS-1$ $NON-NLS-2$
        if (value != null && !"".equals(value)) { // $NON-NLS-1$
            String swappedColumnKey = getSwappedColumnListKey();
            if (swappedColumnKey != null) {
                clientStorage.setRemoteItem(swappedColumnKey, value);
            }
        }
    }

    @Override
    public int getDragIndex() {
        return dragIndex;
    }

    @Override
    public void setDragIndex(int dragIndex) {
        this.dragIndex = dragIndex;
    }

    @Override
    public void updateColumnContextMenu() {
        contextPopup.getContextMenu().update();
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

        setWidth("100%"); //$NON-NLS-1$
    }

    @Override
    public void onResizeStart(Column<T, ?> column, Element headerElement) {
        // Don't do anything.
    }

    @Override
    public void onResizeEnd(Column<T, ?> column, Element headerElement) {
        updateGridSize();

        // Redraw the table
        redraw();

        if (columnResizePersistenceEnabled) {
            String width = getColumnWidth(column);
            saveColumnWidth(column, width);
        }
    }

    @Override
    public void resizeColumn(Column<T, ?> column, int newWidth) {
        setColumnWidth(column, toPixelString(newWidth), true);
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
            return GRID_COLUMN_WIDTH_PREFIX + "_" + getGridElementId() + "_" //$NON-NLS-1$ //$NON-NLS-2$
                    + determineOriginalIndex(getColumnIndex(column));
        }
        return null;
    }

    protected String getHiddenColumnWidthKey(Column<T, ?> column) {
        if (columnResizePersistenceEnabled) {
            return GRID_HIDDEN_COLUMN_WIDTH_PREFIX + getGridElementId() //$NON-NLS-1$ //$NON-NLS-2$
                + "_" + determineOriginalIndex(getColumnIndex(column)); //$NON-NLS-1$
        }
        return null;
    }

    protected String getSwappedColumnListKey() {
        if (columnResizePersistenceEnabled) {
            return getGridElementId() + "_" + GRID_SWAPPED_COLUMN_LIST_SUFFIX; //$NON-NLS-1$
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

    protected String getHiddenPersistedColumnWidth(Column<T, ?> column) {
        if (!columnResizePersistenceEnabled) {
            return null;
        }
        String result = null;
        String key = getHiddenColumnWidthKey(column);
        if (key != null) {
            result = clientStorage.getLocalItem(key);
        }
        // hidden width is not used/persisted anymore if present in legacy storage it needs to be replaced
        // as it makes difficult to resize the column to reasonable size
        return HIDDEN_WIDTH.equals(result) ? toPixelString(DEFAULT_MINIMUM_COLUMN_WIDTH) : result;
    }

    @Override
    public void persistColumnVisibility(Column<T, ?> column, boolean visible) {
        String key = getHiddenColumnWidthKey(column);
        if (!columnResizePersistenceEnabled || key == null) {
            return;
        }

        if (!visible) {
            if (isHiddenByDefault(column)) {
                // don't store the width since it should be stored under getColumnWidthKey()
                removeFromVisibleByUserRequest(column);
            } else {
                // Store the width of the column before hiding it, so we can restore it.
                // TODO: width is stored 2x: under this key and getColumnWidthKey()
                clientStorage.setRemoteItem(key, getColumnWidth(column));
            }
        } else {
            // column just recently made hidden-by-default might have already grid-hidden keys
            clientStorage.removeRemoteItem(key);
            if (isHiddenByDefault(column)) {
                addToVisibleByUserRequest(column);
            }
        }
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
     * @param sortedModel Model for which to configure column sorting.
     */
    public void initModelSortHandler(SortedListModel<T> sortedModel) {
        addColumnSortHandler(event -> applySort(event, sortedModel));
    }

    private void applySort(ColumnSortEvent event, SortedListModel<T> sortedModel) {
        Column<?, ?> column = event.getColumn();
        if (!(column instanceof SortableColumn)) {
            // Column is not sortable, nothing to do
            return;
        }

        SortableColumn<T> sortableColumn = (SortableColumn<T>) column;
        SearchableListModel<?, T> searchableModel = (sortedModel instanceof SearchableListModel)
                ? (SearchableListModel<?, T>) sortedModel : null;

        boolean sortApplied = false;
        boolean supportsServerSideSorting = searchableModel != null && searchableModel.supportsServerSideSorting();

        // Ensure consistent item order with fallback comparator
        Comparator<? super T> columnComparator = sortableColumn.getComparator();
        Comparator<? super T> realComparator = columnComparator;
        if (sortedModel.useDefaultItemComparator() && columnComparator != null) {
            realComparator = DefaultModelItemComparator.fallbackFor(columnComparator);
        }

        // If server-side sorting is supported by the model, but the column
        // uses Comparator for client-side sorting, use client-side sorting
        if (supportsServerSideSorting && realComparator != null) {
            sortedModel.setComparator(realComparator, event.isSortAscending());
            sortApplied = true;
        } else if (supportsServerSideSorting) {
            // Otherwise, if server-side sorting is supported by the model,
            // update model's sort options and reload its items via search query
            sortedModel.setComparator(null);
            if (searchableModel.isSearchValidForServerSideSorting()) {
                searchableModel.updateSortOptions(sortableColumn.getSortBy(), event.isSortAscending());
                sortApplied = true;
            } else {
                // Search string not valid, cannot perform search query
                searchableModel.clearSortOptions();
            }
        } else if (realComparator != null) {
            // Otherwise, fall back to client-side sorting
            sortedModel.setComparator(realComparator, event.isSortAscending());
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

    @Override
    public void cleanup() {
    }

    protected boolean isHorizontalScrollbarVisible() {
        int tableScrollWidth = this.getTableBodyElement().getScrollWidth();
        return tableScrollWidth != this.getElement().getScrollWidth() && tableScrollWidth != 0;
    }

    protected int determineBrowserHeightAdjustment(int height) {
        int contentHeight = height;
        if (clientAgentType.isFirefox()) {
            contentHeight += FF_HEIGHT_ADJUST;
        } else if(clientAgentType.isIE()) {
            contentHeight += IE_HEIGHT_ADJUST;
        } else {
            contentHeight += CHROME_HEIGHT_ADJUST;
        }
        return contentHeight;
    }

    public void setMaxGridHeight(int maxHeight) {
        this.maxGridHeight = determineBrowserHeightAdjustment(maxHeight);
    }

    public void updateGridSize(final int rowHeight) {
        Scheduler.get().scheduleDeferred(() -> {
            int gridHeaderHeight = getGridHeaderHeight();
            if (!isHeightSet && gridHeaderHeight > 0) {
                if (maxGridHeight == -1 || (maxGridHeight > -1 && maxGridHeight > rowHeight + gridHeaderHeight)) {
                    resizeGridToContentHeight(rowHeight + gridHeaderHeight);
                } else {
                    resizeGridToContentHeight(maxGridHeight);
                }
            }
        });
    }

    protected void resizeGridToContentHeight(int rowHeight) {
        int contentHeight = determineBrowserHeightAdjustment(rowHeight);
        if (isHorizontalScrollbarVisible()) {
            contentHeight += scrollbarThickness;
        }
        super.setHeight(contentHeight + Unit.PX.getType());
        redraw();
    }

    public int getGridHeaderHeight() {
        return this.getTableHeadElement().getOffsetHeight();
    }

    @Override
    public void setRowData(int start, final List<? extends T> values) {
        if (values.size() == 1) {
            addStyleName(HIDE_ONE_ROW_SCROLL);
        } else {
            removeStyleName(HIDE_ONE_ROW_SCROLL);
        }
        super.setRowData(start, values);
        updateGridSize(calculateGridHeight(values.size()));
    }

    private static String toPixelString(int width) {
        return width + "px"; // $NON-NLS-1$
    }

    private void clearPersistedSettings() {
        clientStorage.removeRemoteItem(getSwappedColumnListKey());
        clientStorage.removeRemoteItem(getVisibleByUserRequestListKey());

        for(Column column : getAllColumns().values()) {
            clientStorage.removeLocalItem(getColumnWidthKey(column));
            clientStorage.removeRemoteItem(getHiddenColumnWidthKey(column));
        }
    }

    private Map<Integer, Column> getAllColumns() {
        Map<Integer, Column> result = new HashMap<>();
        for (int index = 0; index < getColumnCount(); index++) {
            result.put(index, getColumn(index));
        }
        return result;
    }

    public void resetGridSettings() {
        clearPersistedSettings();

        boolean originalFlag = columnResizePersistenceEnabled;
        columnResizePersistenceEnabled = false;

        // touch only regular columns
        Map<Integer, Column> indexToColumn = getAllColumns().entrySet().stream()
                .filter(entry -> entry.getValue() != emptyNoWidthColumn)
                .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));

        List<Object[]> tuples = indexToColumn.keySet().stream()
                .map(currentIndex -> {
                    Object[] tuple = {
                            Integer.valueOf(determineOriginalIndex(currentIndex)),
                            getHeader(currentIndex),
                            indexToColumn.get(currentIndex)
                    };
                    return tuple;
                }).collect(Collectors.toList());

        // original indices are already resolved - we can clear this mapping
        realToSwappedIndexes.clear();

        for (Column column :indexToColumn.values()) {
            // side effect: columns hidden by default receive default width
            setColumnVisible(column, true);
            // we assume that all column related mappings are not cleared
            removeColumn(column);
            contextPopup.getContextMenu().removeItem(column);
        }

        tuples.stream()
                // sort columns by original index
                .sorted(Comparator.comparingInt(tuple -> (Integer)tuple[0]))
                .forEach(tuple -> {
                    int originalIndex = (Integer)tuple[0];
                    Header<?> header = (Header)tuple[1];
                    Column column = (Column)tuple[2];

                    insertColumn(originalIndex, column, header);
                    contextPopup.getContextMenu().addItem(column);

                    setColumnWidth(column, defaultWidthMap.get(column));
                    setColumnVisible(column, !isHiddenByDefault(column));
                });

        columnResizePersistenceEnabled = originalFlag;
    }
}
