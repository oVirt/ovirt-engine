package org.ovirt.engine.ui.common.widget.table;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.Kebab;
import org.ovirt.engine.ui.common.widget.PaginationControl;
import org.ovirt.engine.ui.common.widget.action.ActionAnchorListItem;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.common.widget.refresh.RefreshPanel;
import org.ovirt.engine.ui.common.widget.refresh.SimpleRefreshManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid.Resources;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleActionTable<E, T> extends AbstractActionTable<E, T> {

    interface WidgetUiBinder extends UiBinder<FlowPanel, SimpleActionTable<?, ?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    Container tableOverheadContainer;

    @UiField
    SimplePanel tableOverhead;

    @UiField(provided = true)
    @WithElementId
    public RefreshPanel refreshPanel;

    @UiField
    Kebab actionKebab;

    @UiField
    PaginationControl paginationControl;

    @UiField
    FlowPanel controlsContainer;

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, null, eventBus, clientStorage);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            EventBus eventBus, ClientStorage clientStorage,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        this(dataProvider, null, eventBus, clientStorage, refreshManager);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources,
            EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, resources, eventBus, clientStorage,
                new SimpleRefreshManager(dataProvider, eventBus, clientStorage));
    }

    public SimpleActionTable(final SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, EventBus eventBus, ClientStorage clientStorage,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        super(dataProvider, resources, clientStorage);
        this.refreshPanel = refreshManager.getRefreshPanel();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        refreshPanel.setVisible(false);
        paginationControl.setDataProvider(dataProvider);

        refreshManager.setManualRefreshCallback(() -> {
            //Do any special refresh options.
            dataProvider.onManualRefresh();
            setLoadingState(LoadingState.LOADING);
        });

        createActionKebab();
        showActionKebab();
    }

    private void createActionKebab() {
        ActionButton changeBtn = new ActionAnchorListItem(constants.changeColumnsVisibilityOrder());
        changeBtn.addClickHandler(event -> showColumnModificationDialog(event));
        actionKebab.addMenuItem(changeBtn);

        ActionButton resetBtn = new ActionAnchorListItem(constants.resetGridSettings());
        resetBtn.addClickHandler(event -> resetGridSettings());
        actionKebab.addMenuItem(resetBtn);
    }

    private void showColumnModificationDialog(ClickEvent event) {
        closeOtherPopups();
        table.ensureContextMenuHandler().onContextMenu(event.getNativeEvent());
    }

    private void resetGridSettings() {
        table.resetGridSettings();
    }

    public void showActionKebab() {
        actionKebab.setVisible(true);
    }

    public FlowPanel getOuterWidget() {
        return (FlowPanel) getWidget();
    }

    @Override
    protected void updateTableControls() {
        super.updateTableControls();
        paginationControl.updateTableControls();
    }

    public int getTableControlsHeight() {
        return controlsContainer.getOffsetHeight();
    }

    @Override
    public void setVisible(boolean value) {
        super.setVisible(value);
    }
    /**
     * Show the refresh buttons if the data provider's refresh timer is enabled.
     */
    public void showRefreshButton() {
        refreshPanel.setVisible( !getDataProvider().getModel().getIsTimerDisabled() );
    }

    public void hideRefreshButton() {
        refreshPanel.setVisible(false);
    }

    public void showItemsCount() {
    }

    public void setTableOverhead(Widget widget) {
        tableOverheadContainer.setVisible(true);
        tableOverhead.setWidget(widget);
    }

}
