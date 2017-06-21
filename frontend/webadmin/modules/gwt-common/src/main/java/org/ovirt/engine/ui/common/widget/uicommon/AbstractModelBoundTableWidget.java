package org.ovirt.engine.ui.common.widget.uicommon;

import java.util.List;

import org.ovirt.engine.ui.common.MainTableHeaderlessResources;
import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.SubTableHeaderlessResources;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.PatternflyActionPanel;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.OrderedMultiSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.Widget;
import com.google.web.bindery.event.shared.HandlerRegistration;

/**
 * Base class for widgets that use {@link SimpleActionTable} to represent UiCommon list models.
 *
 * @param <T>
 *            Table row data type.
 * @param <M>
 *            List model type.
 */
public abstract class AbstractModelBoundTableWidget<T, M extends SearchableListModel> extends Composite {

    private final SearchableTableModelProvider<T, M> modelProvider;

    private final EventBus eventBus;
    private final boolean useMainTableResources;

    private final SimpleActionTable<T> table;
    private final FlowPanel wrappedWidget;
    private HandlerRegistration registration;
    protected PatternflyActionPanel actionPanel;

    public AbstractModelBoundTableWidget(SearchableTableModelProvider<T, M> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, boolean useMainTableResources) {
        this.modelProvider = modelProvider;
        this.eventBus = eventBus;
        this.useMainTableResources = useMainTableResources;
        this.table = createActionTable(eventBus, clientStorage);
        this.actionPanel = createActionPanel();
        this.wrappedWidget = new FlowPanel();
        wrappedWidget.add(actionPanel);
        wrappedWidget.add(table);
        initWidget(getWrappedWidget());
        OrderedMultiSelectionModel<T> tableSelectionModel = getTable() != null ? getTable().getSelectionModel() : null;
        if (tableSelectionModel != null) {
            registration = tableSelectionModel.addSelectionChangeHandler(event -> {
                // Update detail model selection
                updateDetailModelSelection();
            });
        }
    }

    @Override
    public void onUnload() {
        super.onUnload();
        registration.removeHandler();
    }

    protected void updateDetailModelSelection() {
        if (modelProvider instanceof SearchableDetailModelProvider) {
            modelProvider.setSelectedItems(getSelectedItems());
        }
    }

    private List<T> getSelectedItems() {
        return getTable() != null ? getTable().getSelectionModel().getSelectedList() : null;
    }

    SimpleActionTable<T> createActionTable(EventBus eventBus, ClientStorage clientStorage) {
        return new SimpleActionTable<T>(modelProvider,
                getTableHeaderlessResources(), getTableResources(),
                eventBus, clientStorage) {
            {
                if (useTableWidgetForContent()) {
                    enableHeaderContextMenu();
                }
            }
        };
    }

    protected PatternflyActionPanel createActionPanel() {
        return new PatternflyActionPanel();
    }

    /**
     * Returns {@code true} if table content is provided by the {@link #table} widget itself.
     * Returns {@code false} if table content is provided by a custom widget, e.g. a tree.
     */
    protected boolean useTableWidgetForContent() {
        return true;
    }

    /**
     * @return Widget passed to the {@linkplain Composite#initWidget initWidget} method.
     */
    protected Widget getWrappedWidget() {
        return wrappedWidget;
    }

    private Resources getTableResources() {
        return useMainTableResources ? GWT.create(MainTableResources.class) : GWT.create(SubTableResources.class);
    }

    private Resources getTableHeaderlessResources() {
        return useMainTableResources ? GWT.create(MainTableHeaderlessResources.class)
                : GWT.create(SubTableHeaderlessResources.class);
    }

    public M getModel() {
        return modelProvider.getModel();
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    public IsWidget getContainer() {
        return wrappedWidget;
    }

    public SimpleActionTable<T> getTable() {
        return table;
    }

    public void addButtonToActionGroup(ActionButton button) {
        actionPanel.addButtonToActionGroup(button);
    }

    public void addMenuItemToKebab(ActionButton menuItem) {
        actionPanel.addMenuItemToKebab(menuItem);
    }

    public void addDividerToKebab() {
        actionPanel.addDividerToKebab();
    }

    protected SearchableTableModelProvider<T, M> getModelProvider() {
        return modelProvider;
    }

    /**
     * Performs initialization of the table widget.
     */
    public abstract void initTable();

    public void addModelListeners() { }
}
