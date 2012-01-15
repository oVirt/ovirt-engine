package org.ovirt.engine.ui.webadmin.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.AbstractActionPanel;
import org.ovirt.engine.ui.webadmin.widget.table.refresh.RefreshManager;
import org.ovirt.engine.ui.webadmin.widget.table.refresh.RefreshPanel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.core.client.Scheduler.ScheduledCommand;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
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
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.CellPreviewEvent;
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
public abstract class AbstractActionTable<T> extends AbstractActionPanel<T> {

    @UiField
    @WithElementId
    public ButtonBase prevPageButton;

    @UiField
    @WithElementId
    public ButtonBase nextPageButton;

    @UiField(provided = true)
    RefreshPanel refreshPanel;

    @UiField
    SimplePanel tableContainer;

    @UiField
    SimplePanel tableHeaderContainer;

    private final OrderedMultiSelectionModel<T> selectionModel;

    @WithElementId("content")
    public final ActionCellTable<T> table;
    private final ActionCellTable<T> tableHeader;

    private boolean multiSelectionDisabled;
    protected boolean showDefaultHeader;

    private final RefreshManager refreshManager;

    public AbstractActionTable(SearchableTableModelProvider<T, ?> dataProvider) {
        this(dataProvider, null);
    }

    public AbstractActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources) {
        this(dataProvider, resources, null);
    }

    public AbstractActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerRresources) {
        super(dataProvider);
        this.selectionModel = new OrderedMultiSelectionModel<T>(dataProvider);
        refreshManager = new RefreshManager(dataProvider.getModel());
        refreshPanel = refreshManager.getRefreshPanel();

        this.table = new ActionCellTable<T>(dataProvider, resources) {

            @Override
            protected void onBrowserEvent2(Event event) {
                // Enable multiple selection only when Control/Shift key is pressed
                if ("click".equals(event.getType()) && !multiSelectionDisabled) {
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

                // Ensure that paging buttons are updated whenever new data is set
                prevPageButton.setEnabled(getDataProvider().canGoBack());
                nextPageButton.setEnabled(getDataProvider().canGoForward());
            };
        };

        // Create table header row
        this.tableHeader = new ActionCellTable<T>(dataProvider, headerRresources);
        this.tableHeader.setRowData(new ArrayList<T>());
        showDefaultHeader = headerRresources == null;

        this.selectionModel.setDataDisplay(table);
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

        // Add context menu handler for table widget
        addContextMenuHandler(table);

        // Add arrow key handler
        table.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                boolean shiftPageDown = event.isShiftKeyDown() && KeyCodes.KEY_PAGEDOWN == event.getNativeKeyCode();
                boolean shiftPageUp = event.isShiftKeyDown() && KeyCodes.KEY_PAGEUP == event.getNativeKeyCode();
                boolean ctrlA =
                        event.isControlKeyDown()
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

        // Add right click handler
        table.addCellPreviewHandler(new CellPreviewEvent.Handler<T>() {
            @Override
            public void onCellPreview(CellPreviewEvent<T> event) {
                if (event.getNativeEvent().getButton() != NativeEvent.BUTTON_RIGHT
                        || !"mousedown".equals(event.getNativeEvent().getType())) {
                    return;
                }

                final T value = event.getValue();

                if (!selectionModel.isSelected(value)) {
                    Scheduler.get().scheduleDeferred(new ScheduledCommand() {
                        @Override
                        public void execute() {
                            selectionModel.setMultiSelectEnabled(false);
                            selectionModel.setMultiRangeSelectEnabled(false);
                            selectionModel.setSelected(value, true);
                        }
                    });
                }
            }
        });

        // Use fixed table layout
        table.setWidth("100%", true);
        tableHeader.setWidth("100%", true);

        // Attach table widget to the corresponding panel
        tableContainer.setWidget(table);
        tableHeaderContainer.setWidget(tableHeader);
        tableHeaderContainer.setVisible(!showDefaultHeader);
    }

    @UiHandler("prevPageButton")
    void handlePrevPageButtonClick(ClickEvent event) {
        getDataProvider().goBack();
    }

    @UiHandler("nextPageButton")
    void handleNextPageButtonClick(ClickEvent event) {
        getDataProvider().goForward();
    }

    @UiHandler("refreshPanel")
    void handleRefreshPanelClick(ClickEvent event) {
        getDataProvider().refresh();
    }

    void addColumn(Column<T, ?> column, Header<?> header) {
        table.addColumn(column, header);
        tableHeader.addColumn(column, header);

        // Configure column content element ID options
        table.configureElementId(column);
    }

    void setColumnWidth(Column<T, ?> column, String width) {
        table.setColumnWidth(column, width);
        tableHeader.setColumnWidth(column, width);
    }

    /**
     * Adds a new table column, without specifying the column width.
     */
    public void addColumn(Column<T, ?> column, String headerText) {
        addColumn(column, new TextHeader(headerText));
    }

    /**
     * Adds a new table column, using the given column width.
     */
    public void addColumn(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    /**
     * Adds a new table column with HTML in the header text, using the given column width.
     * <p>
     * {@code headerHtml} must honor the {@link SafeHtml} contract as specified in
     * {@link SafeHtmlUtils#fromSafeConstant(String) fromSafeConstant}.
     *
     * @see SafeHtmlUtils#fromSafeConstant(String)
     */
    public void addColumnWithHtmlHeader(Column<T, ?> column, String headerHtml, String width) {
        SafeHtml headerValue = SafeHtmlUtils.fromSafeConstant(headerHtml);
        SafeHtmlHeader header = new SafeHtmlHeader(headerValue);

        addColumn(column, header);
        setColumnWidth(column, width);
    }

    /**
     * Removes the given column.
     */
    public void removeColumn(Column<T, ?> column) {
        table.removeColumn(column);
        tableHeader.removeColumn(column);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, String headerText, boolean present) {
        if (present && table.getColumnIndex(column) == -1) {
            addColumn(column, headerText);
        } else if (!present && table.getColumnIndex(column) != -1) {
            removeColumn(column);
        }
    }

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

    public void onFocus() {
        refreshManager.onFocus();
    }

    public void onBlur() {
        refreshManager.onBlur();
    }

    @Override
    protected List<T> getSelectedItems() {
        return new ArrayList<T>(selectionModel.getSelectedList());
    }

    public void setLoadingState(LoadingState state) {
        table.setLoadingState(state);
    }

}
