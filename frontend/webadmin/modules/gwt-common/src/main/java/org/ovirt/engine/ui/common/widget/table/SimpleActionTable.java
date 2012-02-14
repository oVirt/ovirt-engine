package org.ovirt.engine.ui.common.widget.table;

import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButton;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.common.widget.action.SimpleActionButton;
import org.ovirt.engine.ui.common.widget.refresh.RefreshPanel;
import org.ovirt.engine.ui.common.widget.refresh.SimpleRefreshManager;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.HTMLPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.MenuItem;
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

    @UiField
    @WithElementId
    public Label itemsCountLabel;

    private final SimpleRefreshManager refreshManager;

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, null, null, eventBus, clientStorage);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, EventBus eventBus, ClientStorage clientStorage) {
        this(dataProvider, resources, null, eventBus, clientStorage);
    }

    public SimpleActionTable(SearchableTableModelProvider<T, ?> dataProvider,
            Resources resources, Resources headerResources,
            EventBus eventBus, ClientStorage clientStorage) {
        super(dataProvider, resources, headerResources, eventBus);
        this.refreshManager = new SimpleRefreshManager(dataProvider, eventBus, clientStorage);
        this.refreshPanel = refreshManager.getRefreshPanel();
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        initStyles();
        refreshPanel.setVisible(false);
        prevPageButton.setVisible(false);
        nextPageButton.setVisible(false);
        itemsCountLabel.setVisible(false);
    }

    @Override
    protected void updateTableControls() {
        super.updateTableControls();
        itemsCountLabel.setText(getDataProvider().getItemsCount());
    }

    void initStyles() {
        tableContainer.setStyleName(showDefaultHeader ? style.contentWithDefaultHeader() : style.content());
    }

    public void showRefreshButton() {
        refreshPanel.setVisible(true);
    }

    public void showItemsCount() {
        itemsCountLabel.setVisible(true);
    }

    public void setBarStyle(String barStyle) {
        barPanel.setStyleName(barStyle);
    }

    @Override
    protected ActionButton createNewActionButton(ActionButtonDefinition<T> buttonDef) {
        return new SimpleActionButton();
    }

    @Override
    protected void updateMenuItem(MenuItem item, ActionButtonDefinition<T> buttonDef) {
        super.updateMenuItem(item, buttonDef);

        if (buttonDef.isSubTitledAction()) {
            item.addStyleName(style.subTitledButton());
        }
    }

    interface Style extends CssResource {
        String content();

        String contentWithDefaultHeader();

        String subTitledButton();
    }

}
