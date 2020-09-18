package org.ovirt.engine.ui.common.widget.table;

import static org.ovirt.engine.ui.common.widget.table.AbstractActionTable.ColumnSortListHelper.moveHeaderSortState;

import java.util.List;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.Divider;
import org.gwtbootstrap3.client.ui.DropDownHeader;
import org.gwtbootstrap3.client.ui.DropDownMenu;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.DeferredModelCommandInvoker;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.action.AbstractActionPanel;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.UiMenuBarButtonDefinition;
import org.ovirt.engine.ui.common.widget.label.NoItemsLabel;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.OvirtSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Style.Position;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.DoubleClickEvent;
import com.google.gwt.event.dom.client.KeyCodes;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortList;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionModel;

/**
 * Base class used to implement action table widgets.
 * <p>
 * Subclasses are free to style the UI, given that they declare:
 * <ul>
 * <li>{@link #tableContainer} widget for displaying the actual table
 * </ul>
 *
 * @param <T>
 *            Table row data type.
 */
public abstract class AbstractActionTable<E, T> extends AbstractActionPanel<T> implements ActionTable<T>, HasColumns<T> {

    private static final String ARIA_EXPANDED = "aria-expanded"; //$NON-NLS-1$
    private static final String OPEN = "open";  //$NON-NLS-1$

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
    public FlowPanel tableContainer;

    private final OvirtSelectionModel<T> selectionModel;

    @WithElementId("content")
    public final ActionCellTable<T> table;
    private List<ActionButtonDefinition<E, T>> actionDefinitions;

    private final int[] mousePosition = new int[2];

    private boolean doAutoSelect;

    private RowVisitor<T> rowVisitor;

    public AbstractActionTable(final SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, ClientStorage clientStorage) {
        super(dataProvider);
        this.selectionModel = dataProvider.getModel().getSelectionModel();
        this.table = new ActionCellTable<T>(dataProvider, resources) {

            @Override
            protected void onBrowserEvent2(Event event) {
                // Enable multiple selection only when Control/Shift key is pressed
                mousePosition[0] = event.getClientX();
                mousePosition[1] = event.getClientY();
                if (BrowserEvents.CLICK.equals(event.getType())) {
                    selectionModel.asMultiSelectionModel().setMultiSelectEnabled(event.getCtrlKey());
                    selectionModel.asMultiSelectionModel().setMultiRangeSelectEnabled(event.getShiftKey());
                }
                // Remove focus from the table so refreshes won't try to focus on the
                // selected row. This is important when the user has scrolled the selected
                // row off the screen, we don't want the browser to scroll back.
                table.setFocus(false);

                super.onBrowserEvent2(event);
            }

            @Override
            public int getKeyboardSelectedRow() {
                if (selectionModel.asMultiSelectionModel().getLastSelectedRow() == -1) {
                    return super.getKeyboardSelectedRow();
                }

                return selectionModel.asMultiSelectionModel().getLastSelectedRow();
            }

            @Override
            protected void onLoad() {
                super.onLoad();
                if (selectionModel.asMultiSelectionModel().getLastSelectedRow() == -1) {
                    return;
                }

                Scheduler.get().scheduleDeferred(() -> setFocus(true));
            }

            @Override
            public void setRowData(int start, final List<? extends T> values) {
                super.setRowData(start, values);
                selectionModel.asMultiSelectionModel().resolveChanges();
                if (isAttached() && isVisible()) {
                    autoSelectFirst();
                }
                updateTableControls();
            }

            @Override
            protected void onLoadingStateChanged(LoadingState state) {
                super.onLoadingStateChanged(state);
                if (state == LoadingState.LOADING) {
                    Scheduler.get().scheduleDeferred(() -> doAutoSelect = true);
                } else if (state == LoadingState.LOADED) {
                    Scheduler.get().scheduleDeferred(() -> {
                        if (rowVisitor != null) {
                            int count = getVisibleItemCount();
                            for (int i = 0; i < count; i++) {
                                TableRowElement row = getChildElement(i);
                                T item = getVisibleItem(i);
                                rowVisitor.visit(row, item);
                            }
                        }
                    });
                }
            }

            @Override
            protected String getGridElementId() {
                return AbstractActionTable.this.getElementId();
            }
        };

        // Can't do this in the onBrowserEvent, as GWT CellTable doesn't support double click.
        this.table.addDomHandler(event -> {
            SearchableListModel model = dataProvider.getModel();
            UICommand command = model.getDoubleClickCommand();
            if (command != null && command.getIsExecutionAllowed()) {
                DeferredModelCommandInvoker invoker = new DeferredModelCommandInvoker(model);
                invoker.invokeCommand(command);
            }
        }, DoubleClickEvent.getType());

        // Apply selection model to the table widget
        this.selectionModel.asMultiSelectionModel().setDataDisplay(table);

        // Default to 'no items to display'
        this.table.setEmptyTableWidget(new NoItemsLabel());

        table.enableColumnWidthPersistence(clientStorage, dataProvider.getModel());

        addModelSearchStringChangeListener(dataProvider.getModel());
    }

    public void updateGridSize() {
        this.table.updateGridSize();
    }

    public void setMaxGridHeight(int maxHeight) {
        this.table.setMaxGridHeight(maxHeight);
    }

    public int getTableAbsoluteTop() {
        return this.table.getElement().getAbsoluteTop() + this.table.getGridHeaderHeight();
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
        if (table.getRowCount() == 1 && selectionModel.asMultiSelectionModel().getSelectedList().isEmpty() && doAutoSelect) {
            Scheduler.get().scheduleDeferred(() -> {
                if (table.getVisibleItemCount() > 0) {
                    selectionModel.setSelected(table.getVisibleItems().get(0), true);
                }
            });
            doAutoSelect = false;
        }
    }

    void addModelSearchStringChangeListener(final SearchableListModel<?, ?> model) {
        if (model.supportsServerSideSorting()) {
            model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
                if ("SearchString".equals(args.propertyName)) { //$NON-NLS-1$
                    if (!model.isSearchValidForServerSideSorting()) {
                        model.clearSortOptions();
                        table.clearColumnSort();
                    }
                }
            });
        }
    }

    protected void updateTableControls() {
    }

    public void showSelectionCountTooltip() {
        this.selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

            private PopupPanel tooltip = null;

            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                int selectedItems = selectionModel.asMultiSelectionModel().getSelectedList().size();
                if (selectedItems < 2) {
                    return;
                }

                if (tooltip != null) {
                    tooltip.hide();
                }

                tooltip = new PopupPanel(true);
                tooltip.setWidget(new Label(selectionModel.asMultiSelectionModel().getSelectedList().size()
                        + " " + constants.selectedActionTable())); //$NON-NLS-1$

                if (mousePosition[0] == 0 && mousePosition[1] == 0) {
                    mousePosition[0] = Window.getClientWidth() / 2;
                    mousePosition[1] = Window.getClientHeight() / 3;
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

        // Set up sort handler
        initSortHandler();

        // Set up table selection model
        table.setSelectionModel(selectionModel);

        // Enable keyboard selection
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        // Add arrow key handler
        table.addDomHandler(event -> {
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
                selectionModel.asMultiSelectionModel().selectAllNext();
            } else if (shiftPageUp) {
                selectionModel.asMultiSelectionModel().selectAllPrev();
            } else if (ctrlA) {
                selectionModel.asMultiSelectionModel().selectAll();
            } else if (arrow) {
                selectionModel.asMultiSelectionModel().setMultiSelectEnabled(event.isControlKeyDown());
                selectionModel.asMultiSelectionModel().setMultiRangeSelectEnabled(event.isShiftKeyDown());

                if (event.isDownArrow()) {
                    selectionModel.asMultiSelectionModel().selectNext();
                } else if (event.isUpArrow()) {
                    selectionModel.asMultiSelectionModel().selectPrev();
                }
            }
        }, KeyDownEvent.getType());
        table.addCellPreviewHandler(event -> {
            if (BrowserEvents.CONTEXTMENU.equals(event.getNativeEvent().getType())) {
                HasData<T> display = event.getDisplay();
                SelectionModel<? super T> selectionModel = display.getSelectionModel();
                if (selectionModel == null) {
                  return;
                }
                T value = event.getValue();
                if(!this.selectionModel.asMultiSelectionModel().isMultiSelectEnabled() &&
                   !this.selectionModel.asMultiSelectionModel().isMultiRangeSelectEnabled()) {
                    selectionModel.setSelected(value, true);
                }
            }
        });
        addContextMenuHandler(tableContainer);
        setWidth("100%"); //$NON-NLS-1$

        // Attach table widget to the corresponding panel
        tableContainer.add(table);

        table.getElement().addClassName("aat-table"); //$NON-NLS-1$
    }

    void initSortHandler() {
        // Attach column sort handler
        table.initModelSortHandler(getDataProvider().getModel());
    }

    public void setWidth(String width) {
        table.setWidth(width);
    }

    public void setColumnWidth(Column<T, ?> column, String width) {
        table.setColumnWidth(column, width);
    }

    @Override
    public void addColumn(Column<T, ?> column, String headerText) {
        table.addColumn(column, headerText);
    }

    @Override
    public void addColumn(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        setColumnWidth(column, width);
    }

    @Override
    public void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml) {
        table.addColumnWithHtmlHeader(column, headerHtml);
    }

    @Override
    public void addColumnWithHtmlHeader(Column<T, ?> column, SafeHtml headerHtml, String width) {
        table.addColumnWithHtmlHeader(column, headerHtml, width);
    }

    @Override
    public void addColumn(Column<T, ?> column, SafeHtmlHeader header) {
        table.addColumn(column, header);
    }

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
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     */
    public void ensureColumnVisible(Column<T, ?> column, String headerText, boolean visible, String width) {
        table.ensureColumnVisible(column, headerText, visible, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtml headerHtml, boolean visible, String width) {
        table.ensureColumnVisible(column, headerHtml, visible, width);
    }

    /**
     * Ensures that the given column is visible or hidden.
     * <p>
     * This method also sets the column width in case the column needs to be added.
     */
    public void ensureColumnVisible(Column<T, ?> column, SafeHtmlHeader header, boolean visible, String width) {
        table.ensureColumnVisible(column, header, visible, width);
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
        table.enableHeaderContextMenu();
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
        table.enableColumnResizing();
    }

    @Override
    public OvirtSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public E getParentEntity() {
        return (E) getDataProvider().getModel().getEntity();
    }

    public List<T> getSelectedItems() {
        return selectionModel.asMultiSelectionModel().getSelectedList();
    }

    @Override
    public void setLoadingState(LoadingState state) {
        table.setLoadingState(state);
    }

    public String getColumnWidth(Column<T, ?> column) {
        return table.getColumnWidth(column);
    }

    /**
     * Adds a context menu handler to the given widget.
     * @param widget The widget.
     */
    public void addContextMenuHandler(Widget widget) {
        widget.addDomHandler(event -> AbstractActionTable.this.onContextMenu(event), ContextMenuEvent.getType());
    }

    /**
     * Show the context menu.
     * @param event The {@code ContextMenuEvent}
     */
    protected void onContextMenu(ContextMenuEvent event) {
        final int eventX = event.getNativeEvent().getClientX();
        final int eventY = event.getNativeEvent().getClientY();

        // Suppress default browser context menu
        event.preventDefault();
        event.stopPropagation();

        // Use deferred command to ensure that the context menu
        // is shown only after other event handlers do their job
        Scheduler.get().scheduleDeferred(() -> {
            // Avoid showing empty context menu
            if (actionDefinitions != null && anyContextMenuItemsVisible()) {
                updateContextMenu(menu, actionDefinitions);
                styleAndPositionMenuContainer(eventX, eventY);
            }
        });
    }

    private boolean anyContextMenuItemsVisible() {
        return actionDefinitions.stream().anyMatch(buttonDef ->
            buttonDef.isAccessible(getParentEntity(), getSelectedItems()) && buttonDef.isVisible(getParentEntity(), getSelectedItems()));
    }

    private void styleAndPositionMenuContainer(final int eventX, final int eventY) {
        menuContainer.addStyleName(OPEN);
        clickButton.getElement().setAttribute(ARIA_EXPANDED, Boolean.TRUE.toString());
        menuContainer.getElement().getStyle().setPosition(Position.ABSOLUTE);
        menuContainer.getElement().getStyle().setTop(Window.getScrollTop() + eventY, Unit.PX);
        menuContainer.getElement().getStyle().setLeft(eventX, Unit.PX);
    }

    private DropDownMenu updateContextMenu(DropDownMenu menuBar, List<ActionButtonDefinition<E, T>> actions) {
        return updateContextMenu(menuBar, actions, true);
    }

    /**
     * Rebuilds context menu items to match the action button list.
     * @param dropDownMenu The menu bar to populate.
     * @param actions A list of {@code ActionButtonDefinition}s used to populate the {@code MenuBar}.
     * @param removeOldItems A flag to indicate if we should remove old items.
     * @return A {@code MenuBar} containing all the action buttons as menu items.
     */
    private DropDownMenu updateContextMenu(DropDownMenu dropDownMenu,
            List<ActionButtonDefinition<E, T>> actions,
            boolean removeOldItems) {
        if (removeOldItems) {
            ElementTooltipUtils.destroyMenuItemTooltips(dropDownMenu);
            dropDownMenu.clear();
            // Close any other open popups as well.
            closeOtherPopups();
        }

        for (final ActionButtonDefinition<E, T> buttonDef : actions) {
            if (buttonDef instanceof UiMenuBarButtonDefinition) {
                UiMenuBarButtonDefinition<E, T> menuBarDef = (UiMenuBarButtonDefinition<E, T>) buttonDef;
                DropDownHeader subMenuHeader = new DropDownHeader(buttonDef.getText());
                dropDownMenu.add(new Divider());
                subMenuHeader.setVisible(buttonDef.isVisible(getParentEntity(), getSelectedItems()));
                dropDownMenu.add(subMenuHeader);
                updateContextMenu(dropDownMenu, menuBarDef.getSubActions(), false);
            } else {
                AnchorListItem item = new AnchorListItem(buttonDef.getText());
                item.addClickHandler(e -> buttonDef.onClick(getParentEntity(), getSelectedItems()));

                updateMenuItem(item, buttonDef);
                dropDownMenu.add(item);
            }
        }

        return dropDownMenu;
    }

    /**
     * Close all other open bootstrap popups by removing the 'open' class from them.
     */
    protected native void closeOtherPopups() /*-{
        $wnd.jQuery(".open").removeClass("open");
    }-*/;

    protected void updateMenuItem(AnchorListItem item, ActionButtonDefinition<E, T> buttonDef) {
        item.setVisible(buttonDef.isAccessible(getParentEntity(), getSelectedItems()) && buttonDef.isVisible(getParentEntity(), getSelectedItems()));
        item.setEnabled(buttonDef.isEnabled(getParentEntity(), getSelectedItems()));

        if (buttonDef.getMenuItemTooltip() != null) {
            ElementTooltipUtils.setTooltipOnElement(item.getElement(), buttonDef.getMenuItemTooltip());
        }
    }

    public void setActionMenus(List<?> actionDefinitions) {
        this.actionDefinitions = (List<ActionButtonDefinition<E, T>>) actionDefinitions;
    }

    @Override
    public void hideContextMenu() {
        this.menuContainer.removeStyleName(OPEN);
    }

    public void hideColumnByDefault(Column<T, ?> column) {
        table.markColumnAsHiddenByDefault(column);
        if (!table.isVisibleOnUserRequest(column)) {
            table.setColumnVisible(column, false);
        }
    }

    /**
     * Moves visual header sort state (arrows visible in the table header).
     * <p>
     * Note that underlying model is not affected. Primary use case: 2 view columns backed by the same model column.
     * </p>
     *
     * @param from
     *            source of the sort state
     * @param to
     *            target column that should receive the sort state
     */
    public void moveSortHeaderState(AbstractTextColumn<?> from,
            AbstractTextColumn<?> to) {
        moveHeaderSortState(table.getColumnSortList(), from, to);
    }

    public static class ColumnSortListHelper {

        /**
         * Moves sort state taken from source column to target column.
         * <p>
         * The source sort state is cleared. The target state is overridden. Not sortable or null columns are treated as
         * non-existing columns and are ignored.
         * </p>
         *
         * @param sortList
         *            list that will be operated on
         * @param from
         *            source of sort state
         * @param to
         *            target to which sort state will be copied
         */
        public static void moveHeaderSortState(ColumnSortList sortList,
                AbstractTextColumn<?> from,
                AbstractTextColumn<?> to) {
            int existingToIndex = findInColumnSortList(sortList, to);
            int existingFromIndex = findInColumnSortList(sortList, from);

            if (existingToIndex != -1) {
                // reset target sort state to none
                sortList.remove(sortList.get(existingToIndex));
            }

            if (existingFromIndex != -1) {
                ColumnSortList.ColumnSortInfo existingFrom = sortList.get(existingFromIndex);
                if (to != null && to.isSortable()) {
                    // take the sort index previously occupied by the source and it's sort direction
                    sortList.insert(existingFromIndex, new ColumnSortList.ColumnSortInfo(to, existingFrom.isAscending()));
                }
                // remove the source
                sortList.remove(existingFrom);
            }
        }

        /**
         * Returns the index of <code>column</code> inside <code>sortList</code>.
         * <p>
         * Null column or sortList are treated as not found columns.
         * </p>
         *
         * @param sortList
         *            list that will be traversed
         * @param column
         *            column to be searched
         * @return index or <code>-1</code> if <column>column</column> was not found
         */
        public static int findInColumnSortList(ColumnSortList sortList, AbstractTextColumn<?> column) {
            if (sortList == null || column == null) {
                return -1;
            }

            for (int i = 0; i < sortList.size(); i++) {
                ColumnSortList.ColumnSortInfo sortInfo = sortList.get(i);
                if (column.equals(sortInfo.getColumn())) {
                    return i;
                }
            }
            return -1;
        }
    }
}
