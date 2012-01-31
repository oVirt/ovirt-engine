package org.ovirt.engine.ui.webadmin.widget.table;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.table.AbstractActionTable;
import org.ovirt.engine.ui.common.widget.table.refresh.AbstractRefreshManager;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.action.SimpleActionButton;
import org.ovirt.engine.ui.webadmin.widget.table.refresh.RefreshManager;
import org.ovirt.engine.ui.webadmin.widget.table.refresh.RefreshPanel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Widget;

public class SimpleActionTable<T> extends AbstractActionTable<T> {

    interface WidgetUiBinder extends UiBinder<Widget, SimpleActionTable<?>> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    Style style;

    @UiField
    HTMLPanel barPanel;

    @UiField(provided = true)
    RefreshPanel refreshPanel;

    private final AbstractRefreshManager<RefreshPanel> refreshManager;

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider) {
        this(dataProvider, null, null);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources) {
        this(dataProvider, resources, null);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerResources) {
        super(dataProvider, resources, headerResources, getEventBus(), getApplicationConstants());
        this.refreshManager = new RefreshManager(dataProvider.getModel(), getClientStorage());
        this.refreshPanel = refreshManager.getRefreshPanel();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        initStyles();
        refreshPanel.setVisible(false);
        prevPageButton.setVisible(false);
        nextPageButton.setVisible(false);
    }

    static CommonApplicationConstants getApplicationConstants() {
        return ClientGinjectorProvider.instance().getApplicationConstants();
    }

    static EventBus getEventBus() {
        return ClientGinjectorProvider.instance().getEventBus();
    }

    static ClientStorage getClientStorage() {
        return ClientGinjectorProvider.instance().getClientStorage();
    }

    void localize(ApplicationConstants constants) {
        prevPageButton.setText(constants.actionTablePrevPageButtonLabel());
        nextPageButton.setText(constants.actionTableNextPageButtonLabel());
    }

    void initStyles() {
        tableContainer.setStyleName(showDefaultHeader ? style.contentWithDefaultHeader() : style.content());
    }

    @Override
    public void onFocus() {
        refreshManager.onFocus();
    }

    @Override
    public void onBlur() {
        refreshManager.onBlur();
    }

    public void showRefreshButton() {
        refreshPanel.setVisible(true);
    }

    public void showPagingButtons() {
        prevPageButton.setVisible(true);
        nextPageButton.setVisible(true);
    }

    public void setBarStyle(String barStyle) {
        barPanel.setStyleName(barStyle);
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        return new SimpleActionButton();
    }

    interface Style extends CssResource {
        String content();

        String contentWithDefaultHeader();
    }

}
