package org.ovirt.engine.ui.common.widget.table;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.AbstractActionPanel;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.table.resize.HasResizableColumns;
import org.ovirt.engine.ui.common.widget.table.resize.ResizableHeader;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.dom.client.TableSectionElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * Base class used to implement action table widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #actionPanel} widget into which action button widgets will be rendered
 * <li>{@link #prevPageButton} widget representing the "previous page" button
 * <li>{@link #nextPageButton} widget representing the "next page" button
 * <li>{@link #refreshPageButton} widget representing the "refresh current page" button
 * <li>{@link #tableContainer} widget for displaying the actual table
 * </ul>
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractActionTable<T> extends AbstractActionPanel<T> implements ActionTable<T>, HasResizableColumns<T> {

    // Minimum width of a column used with column resizing, in pixels
    private static final int RESIZE_MINIMUM_COLUMN_WIDTH = 30;

    @UiField
    @WithElementId
    public ButtonBase prevPageButton;

    @UiField
    @WithElementId
    public ButtonBase nextPageButton;

    @UiField
    public SimplePanel tableContainer;

    @UiField
    public SimplePanel tableHeaderContainer;

    @UiField
    public SimplePanel tableOverhead;

    private final OrderedMultiSelectionModel<T> selectionModel;

    @WithElementId("content")
    public final ActionCellTable<T> table;
    protected final ActionCellTable<T> tableHeader;

    // If false, tableHeader widget will be visible, providing a separate table header UI.
    // If true, tableHeader widget will be hidden, with header UI provided by the main table widget.
    protected final boolean showDefaultHeader;

    private boolean multiSelectionDisabled;
    private final int[] mousePosition = new int[2];

    private boolean columnResizingEnabled = false;

    // Reference to an empty, no-width column used with resizable columns
    private Column<T, ?> emptyNoWidthColumn;

    // Table container's horizontal scroll position, used to align table header with main table
    private int tableContainerScrollPosition = 0;

    public AbstractActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerRresources, EventBus eventBus) {
        super(dataProvider, eventBus);
        this.selectionModel = new OrderedMultiSelectionModel<T>(dataProvider);

        this.table = new ActionCellTable<T>(dataProvider, resources) {

            @Override
            protected void onBrowserEvent2(Event event) {
                // Enable multiple selection only when Control/Shift key is pressed
                mousePosition[0] = event.getClientX();
                mousePosition[1] = event.getClientY();
                if ("click".equals(event.getType()) && !multiSelectionDisabled) { //$NON-NLS-1$
                    selectionModel.setMultiSelectEnabled(event.getCtrlKey());
                    selectionModel.setMultiRangeSelectEnabled(event.getShiftKey());
                }

                super.onBrowserEvent2(event);
            }

            @Override
            protected int getKeyboardSelectedRow() {
                if (selectionModel.getLastSelectedRow() == -1) {
                    return super.getKeyboardSelectedRow();
                }

                return selectionModel.getLastSelectedRow();
            }

            @Override
            protected void onLoad() {
                super.onLoad();
                if (selectionModel.getLastSelectedRow() == -1) {
                    return;
                }

                Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                    @Override
                    public void execute() {
                        setFocus(true);
                    }
                });
            }

            @Override
            public void setRowData(int start, List<? extends T> values) {
                super.setRowData(start, values);
                selectionModel.resolveChanges();
                updateTableControls();
            }

            @Override
            protected void onLoadingStateChanged(LoadingState state) {
                super.onLoadingStateChanged(state);

                if (state == LoadingState.LOADED) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            enforceScrollPosition();
                        }
                    });
                }
            }

        };

        // Create table header row
        this.tableHeader = new ActionCellTable<T>(dataProvider, headerRresources);
        this.tableHeader.setRowData(new ArrayList<T>());
        this.showDefaultHeader = headerRresources == null;

        // Apply selection model to the table widget
        this.selectionModel.setDataDisplay(table);
    }

    protected void updateTableControls() {
        prevPageButton.setEnabled(getDataProvider().canGoBack());
        nextPageButton.setEnabled(getDataProvider().canGoForward());
    }

    public void showPagingButtons() {
        prevPageButton.setVisible(true);
        nextPageButton.setVisible(true);
    }

    public void showSelectionCountTooltip(final CommonApplicationConstants constants) {
        this.selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            private PopupPanel tooltip = null;

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                int selectedItems = selectionModel.getSelectedList().size();
                if (selectedItems < 2) {
                    return;
                }

                if (tooltip != null) {
                    tooltip.hide();
                }

                tooltip = new PopupPanel(true);
                tooltip.setWidget(new Label(selectionModel.getSelectedList().size()
                        + " " + constants.selectedActionTable())); //$NON-NLS-1$

                if (mousePosition[0] == 0 && mousePosition[1] == 0) {
                    mousePosition[0] = Window.getClientWidth() / 2;
                    mousePosition[1] = Window.getClientHeight() * 1 / 3;
                }

                tooltip.setPopupPosition(mousePosition[0] + 15, mousePosition[1]);
                tooltip.show();

                Timer t = new Timer() {
                    @Override
                    public void run() {
                        tooltip.hide();
                    }
                };
                t.schedule(500);
            }

        });
    }

    @Override
    protected SearchableTableModelProvider<T, ?> getDataProvider() {
        return (SearchableTableModelProvider<T, ?>) super.getDataProvider();
    }

    @Override
    protected void initWidget(Widget widget) {
        super.initWidget(widget);
        initTable();
    }

    /**
     * Initialize the table widget and attach it to the corresponding panel.
     */
    void initTable() {
        // Set up table data provider
        getDataProvider().addDataDisplay(table);

        // Add default sort handler that delegates to the data provider
        AsyncHandler columnSortHandler = new AsyncHandler(table);
        table.addColumnSortHandler(columnSortHandler);

        // Set up table selection model
        table.setSelectionModel(selectionModel);

        // Enable keyboard selection
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        // Add arrow key handler
        table.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                boolean shiftPageDown = event.isShiftKeyDown() && KeyCodes.KEY_PAGEDOWN == event.getNativeKeyCode();
                boolean shiftPageUp = event.isShiftKeyDown() && KeyCodes.KEY_PAGEUP == event.getNativeKeyCode();
                boolean ctrlA = event.isControlKeyDown()
                        && ('a' == event.getNativeKeyCode() || 'A' == event.getNativeKeyCode());
                boolean arrow = KeyDownEvent.isArrow(event.getNativeKeyCode());

                if (shiftPageUp || shiftPageDown || ctrlA || arrow) {
                    event.preventDefault();
                    event.stopPropagation();
                } else {
                    return;
                }

                if (shiftPageDown) {
                    selectionModel.selectAllNext();
                } else if (shiftPageUp) {
                    selectionModel.selectAllPrev();
                } else if (ctrlA) {
                    selectionModel.selectAll();
                } else if (arrow) {
                    selectionModel.setMultiSelectEnabled(event.isControlKeyDown() && !multiSelectionDisabled);
                    selectionModel.setMultiRangeSelectEnabled(event.isShiftKeyDown() && !multiSelectionDisabled);

                    if (event.isDownArrow()) {
                        selectionModel.selectNext();
                    } else if (event.isUpArrow()) {
                        selectionModel.selectPrev();
                    }
                }
            }
        }, KeyDownEvent.getType());

        // Add context menu handler for table widget
        addContextMenuHandler(tableContainer);

        // Use fixed table layout
        setWidth("100%", true); //$NON-NLS-1$

        // Attach table widget to the corresponding panel
        tableContainer.setWidget(table);
        tableHeaderContainer.setWidget(tableHeader);
        tableHeaderContainer.setVisible(!showDefaultHeader);

        // Use relative positioning for tableHeader, in order to align it with main table
        tableHeader.getElement().getStyle().setPosition(Position.RELATIVE);

        // Attach scroll event handler to main table container, so that the tableHeader widget
        // can have its position aligned with main table container's current scroll position
        tableContainer.addDomHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                tableContainerScrollPosition = tableContainer.getElement().getScrollLeft();
                updateTableHeaderPosition();
            }
        }, ScrollEvent.getType());

        // Reset main table container's scroll position
        enforceScrollPosition();
    }

    void enforceScrollPosition() {
        tableContainer.getElement().setScrollLeft(tableContainerScrollPosition);
        updateTableHeaderPosition();
    }

    void updateTableHeaderPosition() {
        tableHeader.getElement().getStyle().setLeft(-tableContainerScrollPosition, Unit.PX);
    }

    @Override
    public void resetScrollPosition() {
        tableContainerScrollPosition = 0;
        enforceScrollPosition();
    }

    @Override
    protected void onContextMenu(ContextMenuEvent event) {
        super.onContextMenu(event);

        Element target = event.getNativeEvent().getEventTarget().cast();
        T value = getValueFromElement(target);

        if (value != null && !selectionModel.isSelected(value)) {
            selectionModel.setMultiSelectEnabled(false);
            selectionModel.setMultiRangeSelectEnabled(false);
            selectionModel.setSelected(value, true);
        }
    }

    private T getValueFromElement(Element target) {
        TableCellElement tableCell = findNearestParentCell(target);

        if (tableCell != null) {
            Element trElem = tableCell.getParentElement();
            TableRowElement tr = TableRowElement.as(trElem);
            int row = tr.getSectionRowIndex();
            return table.getVisibleItem(row);
        } else {
            return null;
        }
    }

    private TableCellElement findNearestParentCell(Element elem) {
        while ((elem != null) && (elem != table.getElement())) {
            String tagName = elem.getTagName();
            if ("td".equalsIgnoreCase(tagName) || "th".equalsIgnoreCase(tagName)) { //$NON-NLS-1$ //$NON-NLS-2$
                return elem.cast();
            }
            elem = elem.getParentElement();
        }
        return null;
    }

    public void setWidth(String width, boolean isFixedLayout) {
        table.setWidth("100%", isFixedLayout); //$NON-NLS-1$
        tableHeader.setWidth("100%", isFixedLayout); //$NON-NLS-1$
    }

    @UiHandler("prevPageButton")
    public void handlePrevPageButtonClick(ClickEvent event) {
        getDataProvider().goBack();
    }

    @UiHandler("nextPageButton")
    public void handleNextPageButtonClick(ClickEvent event) {
        getDataProvider().goForward();
    }

    void addColumn(Column<T, ?> column, Header<?> header) {
        table.addColumn(column, header);
        tableHeader.addColumn(column, header);

        // Configure column content element ID options
        table.configureElementId(column);

        // Resizable columns require empty, no-width column to be the last table column
        if (columnResizingEnabled) {
            if (emptyNoWidthColumn != null) {
                table.removeColumn(emptyNoWidthColumn);
                tableHeader.removeColumn(emptyNoWidthColumn);
            }

            emptyNoWidthColumn = new EmptyColumn<T>();
            table.addColumn(emptyNoWidthColumn);
            tableHeader.addColumn(emptyNoWidthColumn);
        }
    }

    void setColumnWidth(Column<T, ?> column, String width) {
        table.setColumnWidth(column, width);
        tableHeader.setColumnWidth(column, width);

        // Update cell widths
        int columnIndex = table.getColumnIndex(column);
        for (TableCellElement cell : getTableBodyCells(columnIndex)) {
            cell.getStyle().setProperty("width", width); //$NON-NLS-1$
        }
        for (TableCellElement cell : getTableHeaderCells(columnIndex)) {
            cell.getStyle().setProperty("width", width); //$NON-NLS-1$
        }
    }

    List<TableCellElement> getTableBodyCells(int columnIndex) {
        TableElement tableElement = table.getElement().cast();
        TableSectionElement firstTBodyElement = tableElement.getTBodies().getItem(0);
        return firstTBodyElement != null ? getCells(firstTBodyElement.getRows(), columnIndex)
                : Collections.<TableCellElement> emptyList();
    }

    List<TableCellElement> getTableHeaderCells(int columnIndex) {
        Element tableHeaderElement = showDefaultHeader ? table.getElement() : tableHeader.getElement();
        TableSectionElement tHeadElement = ((TableElement) tableHeaderElement).getTHead();
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

    Header<?> getHeader(Column<T, ?> column, String headerTextOrHtml, boolean allowHtml) {
        SafeHtml text = allowHtml ? SafeHtmlUtils.fromSafeConstant(headerTextOrHtml)
                : SafeHtmlUtils.fromString(headerTextOrHtml);
        return columnResizingEnabled ? new ResizableHeader<T>(text, column, this) : new SafeHtmlHeader(text);
    }

    /**
     * Adds a new table column, without specifying the column width.
     */
    public void addColumn(Column<T, ?> column, String headerText) {
        addColumn(column, getHeader(column, headerText, false));
    }

    /**
     * Adds a new table column, using the given column width.
     */
    public void addColumn(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    /**
     * Adds a new table column with HTML in the header text, without specifying the column width.
     * <p>
     * {@code headerHtml} must honor the {@link SafeHtml} contract as specified in
     * {@link SafeHtmlUtils#fromSafeConstant(String) fromSafeConstant}.
     */
    public void addColumnWithHtmlHeader(Column<T, ?> column, String headerHtml) {
        addColumn(column, getHeader(column, headerHtml, true));
    }

    /**
     * Adds a new table column with HTML in the header text, using the given column width.
     * <p>
     * {@code headerHtml} must honor the {@link SafeHtml} contract as specified in
     * {@link SafeHtmlUtils#fromSafeConstant(String) fromSafeConstant}.
     */
    public void addColumnWithHtmlHeader(Column<T, ?> column, String headerHtml, String width) {
        addColumnWithHtmlHeader(column, headerHtml);
        setColumnWidth(column, width);
    }

    /**
     * Removes the given column.
     */
    void removeColumn(Column<T, ?> column) {
        table.removeColumn(column);
        tableHeader.removeColumn(column);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, String headerText, boolean present) {
        ensureColumnPresent(column, headerText, present, null);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     * <p>
     * This method also sets the width of the column in case the column needs to be added.
     */
    public void ensureColumnPresent(Column<T, ?> column, String headerText, boolean present, String width) {
        if (present) {
            if (table.getColumnIndex(column) != -1) {
                removeColumn(column);
            }

            if (width == null) {
                addColumnWithHtmlHeader(column, headerText);
            } else {
                addColumnWithHtmlHeader(column, headerText, width);
            }
        } else if (!present && table.getColumnIndex(column) != -1) {
            removeColumn(column);
        }
    }

    /**
     * Allows table columns to be resized by dragging their right-hand border using mouse.
     * <p>
     * This method should be called before calling any {@code addColumn} methods.
     * <p>
     * After calling this method, each column must have an explicit width defined in PX units.
     */
    public void enableColumnResizing() {
        // Column resizing is supported only when the tableHeader widget is visible
        columnResizingEnabled = !showDefaultHeader;
    }

    @Override
    public void onResizeStart(Column<T, ?> column, Element headerElement) {
        headerElement.getStyle().setBackgroundColor("#D6DCFF"); //$NON-NLS-1$
    }

    @Override
    public void onResizeEnd(Column<T, ?> column, Element headerElement) {
        headerElement.getStyle().clearBackgroundColor();

        // Redraw main table
        table.redraw();

        // Note: DO NOT redraw tableHeader, as this would cause header cell elements
        // to be re-created, and any event handlers attached to original header cell
        // elements would be lost
    }

    @Override
    public void resizeColumn(Column<T, ?> column, int newWidth) {
        setColumnWidth(column, newWidth + "px"); //$NON-NLS-1$
    }

    @Override
    public int getMinimumColumnWidth(Column<T, ?> column) {
        return RESIZE_MINIMUM_COLUMN_WIDTH;
    }

    @Override
    public OrderedMultiSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public void setTableSelectionModel(SelectionModel<T> selectionModel,
            CellPreviewEvent.Handler<T> selectionEventManager) {
        table.setSelectionModel(selectionModel, selectionEventManager);
    }

    public boolean isMultiSelectionDisabled() {
        return multiSelectionDisabled;
    }

    public void setMultiSelectionDisabled(boolean multiSelectionDisabled) {
        this.multiSelectionDisabled = multiSelectionDisabled;
    }

    @Override
    public List<T> getSelectedItems() {
        return selectionModel.getSelectedList();
    }

    @Override
    public void setLoadingState(LoadingState state) {
        table.setLoadingState(state);
    }

    /**
     * Gets the instance of RowStyles class and sets it to the cell table. Can be used when the rows have special styles
     * according to the data they are displaying.
     */
    public void setExtraRowStyles(RowStyles<T> rowStyles) {
        table.setRowStyles(rowStyles);
    }

    public String getContentTableElementId() {
        return table.getElementId();
    }

}
