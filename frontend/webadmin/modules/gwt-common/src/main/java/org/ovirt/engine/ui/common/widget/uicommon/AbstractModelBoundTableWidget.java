package org.ovirt.engine.ui.common.widget.uicommon;

import java.util.List;

import org.ovirt.engine.ui.common.MainTableResources;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.presenter.PlaceTransitionHandler;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.OvirtSelectionModel;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
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
public abstract class AbstractModelBoundTableWidget<E, T, M extends SearchableListModel> extends Composite {

    private final SearchableTableModelProvider<T, M> modelProvider;

    private final EventBus eventBus;
    private final boolean useMainTableResources;

    // an empty container that can be bound in the view to the ActionPanel
    private final SimplePanel actionPanelContainer = new SimplePanel();
    private final SimpleActionTable<E, T> table;
    private final FlowPanel wrappedWidget;
    private HandlerRegistration registration;
    private PlaceTransitionHandler placeTransitionHandler;

    public AbstractModelBoundTableWidget(SearchableTableModelProvider<T, M> modelProvider,
            EventBus eventBus, DetailActionPanelPresenterWidget<?, ?, ?, M> actionPanel, ClientStorage clientStorage,
            boolean useMainTableResources) {
        this.modelProvider = modelProvider;
        this.eventBus = eventBus;
        this.useMainTableResources = useMainTableResources;
        this.table = createActionTable(eventBus, clientStorage);
        this.wrappedWidget = new FlowPanel();
        if (actionPanel != null) {
            wrappedWidget.add(actionPanel);
            table.setActionMenus(actionPanel.getActionButtons());
        } else {
            wrappedWidget.add(actionPanelContainer);
        }
        wrappedWidget.add(table);
        initWidget(getWrappedWidget());
        OvirtSelectionModel<T> tableSelectionModel = getTable() != null ? getTable().getSelectionModel() : null;
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
        return getTable() != null ? getTable().getSelectionModel().asMultiSelectionModel().getSelectedList() : null;
    }

    SimpleActionTable<E, T> createActionTable(EventBus eventBus, ClientStorage clientStorage) {
        return new SimpleActionTable<E, T>(modelProvider,
                getTableResources(),
                eventBus, clientStorage) {
            {
                if (useTableWidgetForContent()) {
                    enableHeaderContextMenu();
                }
            }
        };
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

    public M getModel() {
        return modelProvider.getModel();
    }

    protected EventBus getEventBus() {
        return eventBus;
    }

    public IsWidget getContainer() {
        return wrappedWidget;
    }

    public SimpleActionTable<E, T> getTable() {
        return table;
    }

    protected SearchableTableModelProvider<T, M> getModelProvider() {
        return modelProvider;
    }

    public void setPlaceTransitionHandler(PlaceTransitionHandler handler) {
        placeTransitionHandler = handler;
    }

    protected PlaceTransitionHandler getPlaceTransitionHandler() {
        return placeTransitionHandler;
    }

    /**
     * Performs initialization of the table widget.
     */
    public abstract void initTable();

    public void addModelListeners() { }

    public SimplePanel getActionPanelContainer() {
        return actionPanelContainer;
    }
}
