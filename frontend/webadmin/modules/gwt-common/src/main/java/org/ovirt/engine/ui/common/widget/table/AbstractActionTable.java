package org.ovirt.engine.ui.common.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.AbstractActionPanel;
import org.ovirt.engine.ui.common.widget.label.NoItemsLabel;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableCellElement;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.DoubleClickHandler;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.dom.client.ScrollEvent;
import com.google.gwt.event.dom.client.ScrollHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList.ColumnSortInfo;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.cellview.client.RowStyles;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.CellPreviewEvent.Handler;
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
 * <li>{@link #tableContainer} widget for displaying the actual table
 * <li>{@link #tableHeaderContainer} widget for displaying the table header
 * </ul>
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractActionTable<T> extends AbstractActionPanel<T> implements ActionTable<T>, HasColumns<T> {

    /**
     * Allows customizing table row elements after the table finished loading its items.
     *
     * @param <T>
     *            Table row data type.
     */
    public interface RowVisitor<T> {

        /**
         * @param row
         *            Table row element.
         * @param item
         *            Value associated with this row.
         */
        void visit(TableRowElement row, T item);

    }

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    @WithElementId
    public ButtonBase prevPageButton;

    @UiField
    @WithElementId
    public ButtonBase nextPageButton;

    @UiField
    public ScrollPanel tableContainer;

    @UiField
    public SimplePanel tableHeaderContainer;

    private final OrderedMultiSelectionModel<T> selectionModel;

    @WithElementId("content")
    public final ActionCellTable<T> table;
    protected final ActionCellTable<T> tableHeader;

    // If false, tableHeader widget will be visible, providing a separate table header UI.
    // If true, tableHeader widget will be hidden, with header UI provided by the main table widget.
    protected final boolean showDefaultHeader;

    private boolean multiSelectionDisabled;
    private final int[] mousePosition = new int[2];

    private HandlerRegistration scrollHandlerRegistration;
    private HandlerRegistration selectionModelScrollHandlerRegistration;

    // Table container's horizontal scroll position, used to align table header with main table
    private int tableContainerHorizontalScrollPosition = 0;

    private boolean doAutoSelect;

    private int scrollOffset = 0;

    private RowVisitor<T> rowVisitor;

    public AbstractActionTable(final SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerResources, EventBus eventBus, ClientStorage clientStorage) {
        super(dataProvider, eventBus);
        this.selectionModel = new OrderedMultiSelectionModel<>(dataProvider);
        this.table = new ActionCellTable<T>(dataProvider, resources) {

            @Override
            protected void onBrowserEvent2(Event event) {
                // Enable multiple selection only when Control/Shift key is pressed
                mousePosition[0] = event.getClientX();
                mousePosition[1] = event.getClientY();
                if (BrowserEvents.CLICK.equals(event.getType()) && !multiSelectionDisabled) {
                    selectionModel.setMultiSelectEnabled(event.getCtrlKey());
                    selectionModel.setMultiRangeSelectEnabled(event.getShiftKey());
                }
                // Remove focus from the table so refreshes won't try to focus on the
                // selected row. This is important when the user has scrolled the selected
                // row off the screen, we don't want the browser to scroll back.
                table.setFocus(false);

                super.onBrowserEvent2(event);
            }

            @Override
            public int getKeyboardSelectedRow() {
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
            public void setRowData(int start, final List<? extends T> values) {
                super.setRowData(start, values);
                selectionModel.resolveChanges();
                if (isAttached() && isVisible()) {
                    autoSelectFirst();
                }
                updateTableControls();
                enforceScrollPosition();
            }

            @Override
            protected void onLoadingStateChanged(LoadingState state) {
                super.onLoadingStateChanged(state);
                enforceScrollPosition();
                if (state == LoadingState.LOADING) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            doAutoSelect = true;
                        }
                    });
                } else if (state == LoadingState.LOADED) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            if (rowVisitor != null) {
                                int count = getVisibleItemCount();
                                for (int i = 0; i < count; i++) {
                                    TableRowElement row = getChildElement(i);
                                    T item = getVisibleItem(i);
                                    rowVisitor.visit(row, item);
                                }
                            }
                        }
                    });
                }
            }

            @Override
            protected String getGridElementId() {
                return AbstractActionTable.this.getElementId();
            }

            @Override
            protected void pushColumnSort(ColumnSortInfo columnSortInfo) {
                AbstractActionTable.this.pushColumnSort(columnSortInfo);
            }

            @Override
            protected void clearColumnSort() {
                AbstractActionTable.this.clearColumnSort();
            }

        };

        // Can't do this in the onBrowserEvent, as GWT CellTable doesn't support double click.
        this.table.addDomHandler(new DoubleClickHandler() {
            @Override
            public void onDoubleClick(DoubleClickEvent event) {
                SearchableListModel model = dataProvider.getModel();
                UICommand command = model.getDoubleClickCommand();
                if (command != null && command.getIsExecutionAllowed()) {
                    DeferredModelCommandInvoker invoker = new DeferredModelCommandInvoker(model);
                    invoker.invokeCommand(command);
                }
            }
        }, DoubleClickEvent.getType());

        // Create table header row
        this.tableHeader = new ActionCellTable<T>(dataProvider, headerResources) {

            @Override
            public void onResizeEnd(Column<T, ?> column, Element headerElement) {
                super.onResizeEnd(column, headerElement);

                // Redraw main table
                table.redraw();
            }

            @Override
            public void resizeColumn(Column<T, ?> column, int newWidth) {
                super.resizeColumn(column, newWidth);

                // Resize the corresponding column in main table
                table.resizeColumn(column, newWidth);
            }

            @Override
            protected void configureElementId(Column<T, ?> column) {
                // No-op, don't set element ID here, since column
                // instances are shared between main and header table
            }

            @Override
            protected String getGridElementId() {
                return AbstractActionTable.this.getElementId();
            }

            @Override
            protected void pushColumnSort(ColumnSortInfo columnSortInfo) {
                AbstractActionTable.this.pushColumnSort(columnSortInfo);
            }

            @Override
            protected void clearColumnSort() {
                AbstractActionTable.this.clearColumnSort();
            }

            @Override
            public void setColumnVisible(Column<T, ?> column, boolean visible) {
                super.setColumnVisible(column, visible);

                // Update main table
                table.setColumnVisible(column, visible);
            }

            @Override
            public void swapColumns(Column<T, ?> columnOne, Column<T, ?> columnTwo) {
                super.swapColumns(columnOne, columnTwo);

                // Update main table
                table.swapColumns(columnOne, columnTwo);
            }
        };

        this.tableHeader.setRowData(new ArrayList<T>());
        this.showDefaultHeader = headerResources == null;

        // Apply selection model to the table widget
        this.selectionModel.setDataDisplay(table);

        // Default to 'no items to display'
        this.table.setEmptyTableWidget(new NoItemsLabel());

        // column resizing persistence -- can be enabled only when the tableHeader widget is visible
        if (isTableHeaderVisible()) {
            tableHeader.enableColumnWidthPersistence(clientStorage, dataProvider.getModel());
            table.enableColumnWidthPersistence(clientStorage, dataProvider.getModel());
        }
        addModelSearchStringChangeListener(dataProvider.getModel());
        addScrollSelectionModelChangeListener();
    }

    public void setRowVisitor(RowVisitor<T> rowVisitor) {
        this.rowVisitor = rowVisitor;
    }

    @Override
    public void onLoad() {
        super.onLoad();
        autoSelectFirst();
    }

    private void autoSelectFirst() {
        if (table.getRowCount() == 1 && selectionModel.getSelectedList().isEmpty() && doAutoSelect) {
            Scheduler.get().scheduleDeferred(new ScheduledCommand() {

                @Override
                public void execute() {
                    if (table.getVisibleItemCount() > 0) {
                        selectionModel.setSelected(table.getVisibleItems().get(0), true);
                    }
                }
            });
            doAutoSelect = false;
        }
    }

    private void addScrollSelectionModelChangeListener() {
        if (selectionModelScrollHandlerRegistration != null) {
            selectionModelScrollHandlerRegistration.removeHandler();
        }
        selectionModelScrollHandlerRegistration = selectionModel.addSelectionChangeHandler(
                new SelectionChangeEvent.Handler() {

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (selectionModel.getSelectedList().isEmpty()) {
                    scrollOffset = 0;
                } else {
                    updateScrollPosition();
                }
                enforceScrollPosition();
            }

        });
    }

    private void addScrollListener() {
        if (scrollHandlerRegistration != null) {
            scrollHandlerRegistration.removeHandler();
        }
        scrollHandlerRegistration = tableContainer.addScrollHandler(new ScrollHandler() {

            @Override
            public void onScroll(ScrollEvent event) {
                updateScrollPosition();
            }
        });
    }

    void addModelSearchStringChangeListener(final SearchableListModel<?, ?> model) {
        if (model.supportsServerSideSorting()) {
            model.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {
                @Override
                public void eventRaised(org.ovirt.engine.ui.uicompat.Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                    if ("SearchString".equals(args.propertyName)) { //$NON-NLS-1$
                        if (!model.isSearchValidForServerSideSorting()) {
                            model.clearSortOptions();
                            clearColumnSort();
                        }
                    }
                }
            });
        }
    }

    protected void updateTableControls() {
        prevPageButton.setEnabled(getDataProvider().canGoBack());
        nextPageButton.setEnabled(getDataProvider().canGoForward());

        prevPageButton.addStyleName("prevPageButton_pfly_fix"); //$NON-NLS-1$
        nextPageButton.addStyleName("nextPageButton_pfly_fix"); //$NON-NLS-1$
    }

    public void showPagingButtons() {
        prevPageButton.setVisible(true);
        nextPageButton.setVisible(true);
    }

    public void showSelectionCountTooltip() {
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
        addScrollListener();
    }

    /**
     * Initialize the table widget and attach it to the corresponding panel.
     */
    void initTable() {
        // Set up table data provider
        getDataProvider().addDataDisplay(table);

        // Set up sort handler
        initSortHandler();

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
        tableHeaderContainer.setVisible(isTableHeaderVisible());

        // Use relative positioning for tableHeader, in order to align it with main table
        tableHeader.getElement().getStyle().setPosition(Position.RELATIVE);

        // Attach scroll event handler to main table container, so that the tableHeader widget
        // can have its position aligned with main table container's current scroll position
        tableContainer.addDomHandler(new ScrollHandler() {
            @Override
            public void onScroll(ScrollEvent event) {
                tableContainerHorizontalScrollPosition = tableContainer.getElement().getScrollLeft();
                updateTableHeaderPosition();
            }
        }, ScrollEvent.getType());

        // Reset main table container's scroll position
        enforceScrollPosition();
    }

    void initSortHandler() {
        // Allow sorting by one column at a time
        tableHeader.getColumnSortList().setLimit(1);
        table.getColumnSortList().setLimit(1);

        // Attach column sort handler
        ActionCellTable<T> tableWithHeader = isTableHeaderVisible() ? tableHeader : table;
        tableWithHeader.initModelSortHandler(getDataProvider().getModel());
    }

    void pushColumnSort(ColumnSortInfo columnSortInfo) {
        tableHeader.getColumnSortList().push(columnSortInfo);
        table.getColumnSortList().push(columnSortInfo);
    }

    void clearColumnSort() {
        tableHeader.getColumnSortList().clear();
        table.getColumnSortList().clear();
    }

    void enforceScrollPosition() {
        tableContainer.getElement().setScrollLeft(tableContainerHorizontalScrollPosition);
        updateTableHeaderPosition();
        if (table.getParent() != null && table.getParent().getElement() != null) {
            table.getParent().getElement().setScrollTop(scrollOffset);
        }
    }

    void updateTableHeaderPosition() {
        tableHeader.getElement().getStyle().setLeft(-tableContainerHorizontalScrollPosition, Unit.PX);
    }

    @Override
    public void resetScrollPosition() {
        tableContainerHorizontalScrollPosition = 0;
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
            return table.getVisibleItemCount() > 0 ? table.getVisibleItem(row) : null;
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
        table.setWidth(width, isFixedLayout);
        tableHeader.setWidth(width, isFixedLayout);
    }

    @UiHandler("prevPageButton")
    public void handlePrevPageButtonClick(ClickEvent event) {
        getDataProvider().goBack();
    }

    @UiHandler("nextPageButton")
    public void handleNextPageButtonClick(ClickEvent event) {
        getDataProvider().goForward();
    }

    public void setColumnWidth(Column<T, ?> column, String width) {
        table.setColumnWidth(column, width);
        tableHeader.setColumnWidth(column, width);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.HasAddableColumns#addColumn(com.google.gwt.user.cellview.client.Column, java.lang.String)
     */
    @Override
    public void addColumn(Column<T, ?> column, String headerText) {
        table.addColumn(column, headerText);
        tableHeader.addColumn(column, headerText);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.HasAddableColumns#addColumn(com.google.gwt.user.cellview.client.Column, java.lang.String, java.lang.String)
     */
    @Override
    public void addColumn(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.HasAddableColumns#addColumnWithHtmlHeader(com.google.gwt.user.cellview.client.Column, com.google.gwt.safehtml.shared.SafeHtml)
     */
    @Override
    public void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml) {
        table.addColumnWithHtmlHeader(column, headerHtml);
        tableHeader.addColumnWithHtmlHeader(column, headerHtml);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.HasAddableColumns#addColumnWithHtmlHeader(com.google.gwt.user.cellview.client.Column, com.google.gwt.safehtml.shared.SafeHtml, java.lang.String)
     */
    @Override
    public void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml, String width) {
        table.addColumnWithHtmlHeader(column, headerHtml, width);
        tableHeader.addColumnWithHtmlHeader(column, headerHtml, width);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.HasAddableColumns#addColumn(com.google.gwt.user.cellview.client.Column, org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader)
     */
    @Override
    public void addColumn(Column<T, ?> column, SafeHtmlHeader header) {
        table.addColumn(column, header);
        tableHeader.addColumn(column, header);
    }

    /* (non-Javadoc)
     * @see org.ovirt.engine.ui.common.widget.table.HasAddableColumns#addColumn(com.google.gwt.user.cellview.client.Column, org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader, java.lang.String)
     */
    @Override
    public void addColumn(Column<T, ?> column, SafeHtmlHeader header, String width) {
        addColumn(column, header);
        setColumnWidth(column, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     */
    public void ensureColumnVisible(Column<T, ?> column, String headerText, boolean visible) {
        table.ensureColumnVisible(column, headerText, visible);
        tableHeader.ensureColumnVisible(column, headerText, visible);
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     */
    public void ensureColumnVisible(Column<T, ?> column, String headerText, boolean visible, String width) {
        table.ensureColumnVisible(column, headerText, visible, width);
        tableHeader.ensureColumnVisible(column, headerText, visible, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtml headerHtml, boolean visible, String width) {
        table.ensureColumnVisible(column, headerHtml, visible, width);
        tableHeader.ensureColumnVisible(column, headerHtml, visible, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtmlHeader header, boolean visible, String width) {
        table.ensureColumnVisible(column, header, visible, width);
        tableHeader.ensureColumnVisible(column, header, visible, width);
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
        ActionCellTable<T> tableWithHeader = isTableHeaderVisible() ? tableHeader : table;
        tableWithHeader.enableHeaderContextMenu();
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
        // Column resizing is supported only when the tableHeader widget is visible
        if (isTableHeaderVisible()) {
            table.enableColumnResizing();
            tableHeader.enableColumnResizing();
        }
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

    boolean isTableHeaderVisible() {
        return !showDefaultHeader;
    }

    public String getColumnWidth(Column<T, ?> column) {
        return table.getColumnWidth(column);
    }

    private void updateScrollPosition() {
        if (tableContainer.getMaximumVerticalScrollPosition() > 0) {
            scrollOffset = tableContainer.getVerticalScrollPosition();
        }
    }

    public void setVisibleRange(int start, int length) {
        this.table.setVisibleRange(start, length);
    }

    public void addCellPreviewHandler(Handler<T> handler) {
        table.addCellPreviewHandler(handler);
    }
}
