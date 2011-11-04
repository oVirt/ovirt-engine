package org.ovirt.engine.ui.webadmin.widget.table;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.webadmin.widget.FeatureNotImplementedYetPopup;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.ContextMenuEvent;
import com.google.gwt.event.dom.client.ContextMenuHandler;
import com.google.gwt.event.dom.client.KeyDownEvent;
import com.google.gwt.event.dom.client.KeyDownHandler;
import com.google.gwt.event.logical.shared.InitializeEvent;
import com.google.gwt.event.logical.shared.InitializeHandler;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.uibinder.client.UiHandler;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.AsyncHandler;
import com.google.gwt.user.cellview.client.HasKeyboardSelectionPolicy.KeyboardSelectionPolicy;
import com.google.gwt.user.cellview.client.SafeHtmlHeader;
import com.google.gwt.user.cellview.client.TextHeader;
import com.google.gwt.user.client.Command;
import com.google.gwt.user.client.Event;
import com.google.gwt.user.client.ui.ButtonBase;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.MenuBar;
import com.google.gwt.user.client.ui.MenuItem;
import com.google.gwt.user.client.ui.PopupPanel;
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
public abstract class AbstractActionTable<T> extends Composite {

    @UiField
    FlowPanel actionPanel;

    @UiField
    ButtonBase prevPageButton;

    @UiField
    ButtonBase nextPageButton;

    @UiField
    ButtonBase refreshPageButton;

    @UiField
    SimplePanel tableContainer;

    @UiField
    SimplePanel tableHeaderContainer;

    // List of action buttons managed by this action table
    private final List<ActionButtonDefinition<T>> actionButtonList = new ArrayList<ActionButtonDefinition<T>>();

    private final ActionTableDataProvider<T> dataProvider;
    private final OrderedMultiSelectionModel<T> selectionModel;
    private final ActionCellTable<T> table;
    private final ActionCellTable<T> tableHeader;

    private final PopupPanel contextPopupPanel;
    private final MenuBar contextMenuBar;

    private boolean isMultiSelectionDisabled;

    protected boolean showDefaultHeader;

    public AbstractActionTable(final ActionTableDataProvider<T> dataProvider) {
        this(dataProvider, null);
    }

    public AbstractActionTable(final ActionTableDataProvider<T> dataProvider, Resources resources) {
        this(dataProvider, resources, null);
    }

    public AbstractActionTable(final ActionTableDataProvider<T> dataProvider,
            Resources resources,
            Resources headerRresources) {
        this.dataProvider = dataProvider;
        this.selectionModel = new OrderedMultiSelectionModel<T>(dataProvider);

        this.table = new ActionCellTable<T>(dataProvider, resources) {
            @Override
            protected void onBrowserEvent2(Event event) {
                // Enable multiple selection only when Control/Shift key is pressed
                if ("click".equals(event.getType()) && !isMultiSelectionDisabled) {
                    selectionModel.setMultiSelectEnabled(event.getCtrlKey());
                    selectionModel.setMultiRangeSelectEnabled(event.getShiftKey());
                }
                this.setFocus(true);

                super.onBrowserEvent2(event);
            }

            @Override
            public void setRowData(int start, List<? extends T> values) {
                super.setRowData(start, values);

                selectionModel.resolveChanges();

                // Ensure that paging buttons are updated whenever new data is set
                prevPageButton.setEnabled(dataProvider.canGoBack());
                nextPageButton.setEnabled(dataProvider.canGoForward());
            };
        };

        // Create table's header row
        this.tableHeader = new ActionCellTable<T>(dataProvider, headerRresources);
        this.tableHeader.setRowData(new ArrayList<T>());
        showDefaultHeader = headerRresources == null;

        this.contextPopupPanel = new PopupPanel(true);
        this.contextMenuBar = new MenuBar(true);

        this.selectionModel.setDataDisplay(table);
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
        dataProvider.addDataDisplay(table);

        // Add default sort handler that delegates to the data provider
        AsyncHandler columnSortHandler = new AsyncHandler(table);
        table.addColumnSortHandler(columnSortHandler);

        // Set up table selection model
        table.setSelectionModel(selectionModel);

        // Enable keyboard selection
        table.setKeyboardSelectionPolicy(KeyboardSelectionPolicy.ENABLED);

        // Add context menu handler
        table.addDomHandler(new ContextMenuHandler() {
            @Override
            public void onContextMenu(ContextMenuEvent event) {
                event.preventDefault();
                event.stopPropagation();

                // Show context menu only when not empty
                if (hasActionButtons()) {
                    int eventX = event.getNativeEvent().getClientX();
                    int eventY = event.getNativeEvent().getClientY();

                    updateContextMenu();
                    contextPopupPanel.setPopupPosition(eventX, eventY);
                    contextPopupPanel.show();
                }
            }
        }, ContextMenuEvent.getType());
        contextPopupPanel.setWidget(contextMenuBar);

        // Add arrow keys handler
        table.addDomHandler(new KeyDownHandler() {
            @Override
            public void onKeyDown(KeyDownEvent event) {
                if (!KeyDownEvent.isArrow(event.getNativeKeyCode())) {
                    return;
                }
                event.preventDefault();
                event.stopPropagation();

                selectionModel.setMultiSelectEnabled(event.isControlKeyDown() && !isMultiSelectionDisabled);
                selectionModel.setMultiRangeSelectEnabled(event.isShiftKeyDown() && !isMultiSelectionDisabled);

                if (event.isDownArrow()) {
                    selectionModel.selectNext();
                }
                else if (event.isUpArrow()) {
                    selectionModel.selectPrev();
                }
            }
        }, KeyDownEvent.getType());

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
        dataProvider.goBack();
    }

    @UiHandler("nextPageButton")
    void handleNextPageButtonClick(ClickEvent event) {
        dataProvider.goForward();
    }

    @UiHandler("refreshPageButton")
    void handleRefreshPageButtonClick(ClickEvent event) {
        dataProvider.refresh();
    }

    /**
     * Adds a new table column without specifying the column width.
     */
    public void addColumn(Column<T, ?> column, String headerText) {
        table.addColumn(column, new TextHeader(headerText));
        tableHeader.addColumn(column, new TextHeader(headerText));
    }

    /**
     * Adds a new table column using the given column width.
     */
    public void addColumn(Column<T, ?> column, String headerText, String width) {
        addColumn(column, headerText);
        table.setColumnWidth(column, width);
        tableHeader.setColumnWidth(column, width);
    }

    /**
     * Adds a new table column with HTML in the header text using the given column width. <BR>
     * Must honor the {@link SafeHtml} contract as specified in {@link SafeHtmlUtils#fromSafeConstant(String)}
     * 
     * @see SafeHtmlUtils#fromSafeConstant(String)
     */
    public void addColumnWithHtmlHeader(Column<T, ?> column, String headerHtml, String width) {
        SafeHtml fromSafeConstant = SafeHtmlUtils.fromSafeConstant(headerHtml);
        table.addColumn(column, new SafeHtmlHeader(fromSafeConstant));
        table.setColumnWidth(column, width);
    }

    /**
     * Ensures that the given column is added (or removed), unless it's already present (or absent).
     */
    public void ensureColumnPresent(Column<T, ?> column, String headerText, boolean present) {
        if (present && table.getColumnIndex(column) == -1) {
            addColumn(column, headerText);
        } else if (!present && table.getColumnIndex(column) != -1) {
            table.removeColumn(column);
        }
    }

    /**
     * Adds a new button to the table's action panel.
     * 
     * @param <M>
     */
    @SuppressWarnings("unchecked")
    public <M extends SearchableListModel> void addActionButton(final ActionButtonDefinition<T> buttonDef) {
        final ActionButton newActionButton = createNewActionButton(buttonDef);

        // set the button according to its definition
        newActionButton.setEnabledHtml(buttonDef.getEnabledHtml());
        newActionButton.setDisabledHtml(buttonDef.getDisabledHtml());
        newActionButton.setTitle(buttonDef.getTitle());

        if (!buttonDef.isAvailableOnlyFromContext()) {
            actionPanel.add(newActionButton.asWidget());
        }
        actionButtonList.add(buttonDef);

        newActionButton.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (buttonDef.isImplemented()) {
                    buttonDef.onClick(selectionModel.getSelectedList());
                }
                else {
                    FeatureNotImplementedYetPopup fniyp =
                            new FeatureNotImplementedYetPopup((Widget) event.getSource(),
                                    buttonDef.isImplInUserPortal());
                    fniyp.show();
                }
            }
        });

        // Add listener for changes in the List Model "SelectedItem"
        if (dataProvider instanceof SearchableTableModelProvider) {
            ((SearchableTableModelProvider<T, M>) dataProvider).getModel()
                    .getSelectedItemsChangedEvent()
                    .addListener(new IEventListener() {
                        @Override
                        public void eventRaised(org.ovirt.engine.core.compat.Event ev, Object sender, EventArgs args) {
                            // Update the button if it is dynamic
                            if (buttonDef instanceof DynamicUiCommandButtonDefinition) {
                                ((DynamicUiCommandButtonDefinition<T>) buttonDef).updateCommand();
                            }
                        }
                    });
        }

        // Add PropertyChange Listener
        if (buttonDef instanceof UiCommandButtonDefinition) {
            ((UiCommandButtonDefinition<T>) buttonDef).addInitializeHandler(new InitializeHandler() {
                @Override
                public void onInitialize(InitializeEvent event) {
                    updateActionButton(newActionButton, buttonDef);
                }
            });
        }

        updateActionButton(newActionButton, buttonDef);
    }

    /**
     * Ensures that the specified action button is visible or hidden and enabled or disabled as it should.
     */
    void updateActionButton(ActionButton button, ActionButtonDefinition<T> buttonDef) {
        button.asWidget().setVisible(buttonDef.isAccessible());
        button.setEnabled(buttonDef.isEnabled(selectionModel.getSelectedList()));
    }

    /**
     * Ensures that the specified menu item is visible or hidden and enabled or disabled as it should.
     */
    void updateMenuItem(MenuItem item, ActionButtonDefinition<T> buttonDef) {
        item.setVisible(buttonDef.isAccessible());
        item.setEnabled(buttonDef.isEnabled(selectionModel.getSelectedList()));
    }

    /**
     * Rebuilds context menu items to match the action button list.
     */
    void updateContextMenu() {
        contextMenuBar.clearItems();

        for (final ActionButtonDefinition<T> buttonDef : actionButtonList) {
            MenuItem item = new MenuItem(buttonDef.getTitle(), new Command() {
                @Override
                public void execute() {
                    contextPopupPanel.hide();
                    buttonDef.onClick(selectionModel.getSelectedList());
                }
            });

            updateMenuItem(item, buttonDef);
            contextMenuBar.addItem(item);
        }
    }

    public OrderedMultiSelectionModel<T> getSelectionModel() {
        return selectionModel;
    }

    public void setSelectionModel(SelectionModel<T> selectionModel, CellPreviewEvent.Handler<T> selectionEventManager) {
        table.setSelectionModel(selectionModel, selectionEventManager);
    }

    public boolean isMultiSelectionDisabled() {
        return isMultiSelectionDisabled;
    }

    public void setMultiSelectionDisabled(boolean isMultiSelectionDisabled) {
        this.isMultiSelectionDisabled = isMultiSelectionDisabled;
    }

    boolean hasActionButtons() {
        return !actionButtonList.isEmpty();
    }

    /**
     * Returns a new action button widget based on the given definition.
     */
    protected abstract ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef);

}
