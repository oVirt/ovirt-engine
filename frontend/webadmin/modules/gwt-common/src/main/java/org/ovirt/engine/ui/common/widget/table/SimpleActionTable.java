package org.ovirt.engine.ui.common.widget.table;

import org.gwtbootstrap3.client.ui.AnchorListItem;
import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.SimpleActionButton;
import org.ovirt.engine.ui.common.widget.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.common.widget.refresh.RefreshPanel;
import org.ovirt.engine.ui.common.widget.refresh.SimpleRefreshManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleActionTable<T> extends AbstractActionTable<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    @UiField
    SimplePanel tableOverhead;

    @UiField(provided = true)
    @WithElementId
    public RefreshPanel refreshPanel;

    @UiField
    HTMLPanel fromCount;

    @UiField
    HTMLPanel toCount;

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, null, null, eventBus, clientStorage);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            EventBus eventBus, ClientStorage clientStorage,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        this(dataProvider, null, null, eventBus, clientStorage, refreshManager);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, resources, null, eventBus, clientStorage);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, EventBus eventBus, ClientStorage clientStorage,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        this(dataProvider, resources, null, eventBus, clientStorage, refreshManager);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerResources,
            EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, resources, headerResources, eventBus, clientStorage,
                new SimpleRefreshManager(dataProvider, eventBus, clientStorage));
    }

    public SimpleActionTable(final SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerResources,
            EventBus eventBus, ClientStorage clientStorage,
            AbstractRefreshManager<RefreshPanel> refreshManager) {
        super(dataProvider, resources, headerResources, clientStorage);
        this.refreshPanel = refreshManager.getRefreshPanel();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        refreshPanel.setVisible(false);

        refreshManager.setManualRefreshCallback(() -> {
            //Do any special refresh options.
            dataProvider.onManualRefresh();
            setLoadingState(LoadingState.LOADING);
        });
    }

    public IsWidget getOuterWidget() {
        return getWidget();
    }

    @Override
    protected void updateTableControls() {
        super.updateTableControls();
        fromCount.getElement().setInnerText(String.valueOf(getDataProvider().getFromCount()));
        toCount.getElement().setInnerText(String.valueOf(getDataProvider().getToCount()));
    }

    public void showRefreshButton() {
        if (getDataProvider().getModel().getIsTimerDisabled()) {
            refreshPanel.hideRefreshMenuButton();
        }
        refreshPanel.setVisible(true);
    }

    public void hideRefreshButton() {
        refreshPanel.setVisible(false);
    }

    public void showItemsCount() {
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        SimpleActionButton result = new SimpleActionButton();
        if (buttonDef.getIcon() instanceof IconType) {
            result.setIcon((IconType) buttonDef.getIcon());
        }
        return result;
    }

    @Override
    protected void updateMenuItem(AnchorListItem item, ActionButtonDefinition<T> buttonDef) {
        super.updateMenuItem(item, buttonDef);

        if (buttonDef.isSubTitledAction()) {
            item.addStyleName(style.subTitledButton());
        }
    }

    public void setTableOverhead(Widget widget) {
        tableOverhead.setWidget(widget);
    }

    interface Style extends CssResource {
        String subTitledButton();
    }

}
