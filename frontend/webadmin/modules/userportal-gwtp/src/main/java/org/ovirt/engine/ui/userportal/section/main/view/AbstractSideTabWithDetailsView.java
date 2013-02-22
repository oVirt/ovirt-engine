package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractSideTabWithDetailsPresenter;
import org.ovirt.engine.ui.userportal.widget.extended.ExtendedViewSplitLayoutPanel;
import org.ovirt.engine.ui.userportal.widget.table.column.UserPortalSimpleActionTable;

import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSideTabWithDetailsView<T, M extends SearchableListModel> extends AbstractView implements AbstractSideTabWithDetailsPresenter.ViewDef<T> {

    protected final SearchableTableModelProvider<T, M> modelProvider;

    @WithElementId
    public final SimpleActionTable<T> table;

    private static final int subTabPanelHeight = 300;

    private ExtendedViewSplitLayoutPanel splitPanel;
    private final SimplePanel subTabPanelContainer = new SimplePanel();
    private boolean subTabPanelVisible;

    public AbstractSideTabWithDetailsView(
            SearchableTableModelProvider<T, M> modelProvider,
            ApplicationResources applicationResources) {
        this.modelProvider = modelProvider;
        this.table = createActionTable();
        this.table.showRefreshButton();

        applicationResources.sideTabWithDetailsViewStyle().ensureInjected();
        subTabPanelContainer.setStyleName(applicationResources.sideTabWithDetailsViewStyle().detailsContentPanel());

        splitPanel = new ExtendedViewSplitLayoutPanel(applicationResources.extendedViewSplitterSnap());

        initWidget(splitPanel);
        initSplitPanel();
    }

    protected SimpleActionTable<T> createActionTable() {
        return new UserPortalSimpleActionTable<T>(modelProvider,
                getTableResources(),
                ClientGinjectorProvider.instance().getEventBus(),
                ClientGinjectorProvider.instance().getClientStorage());
    }

    protected Resources getTableResources() {
        return null;
    }

    void initSplitPanel() {
        splitPanel.add(table);
        subTabPanelVisible = false;
    }

    @Override
    public void setInSlot(Object slot, Widget content) {
        if (slot == getSubTabPanelContentSlot()) {
            setPanelContent(subTabPanelContainer, content);
        } else {
            super.setInSlot(slot, content);
        }
    }

    /**
     * Returns the slot object associated with the sub tab panel content area.
     */
    protected abstract Object getSubTabPanelContentSlot();

    @Override
    public void setSubTabPanelVisible(boolean subTabPanelVisible) {
        if (this.subTabPanelVisible != subTabPanelVisible) {
            splitPanel.clear();

            if (subTabPanelVisible) {
                splitPanel.addSouth(subTabPanelContainer, subTabPanelHeight);
                splitPanel.add(table);
                splitPanel.init();
            } else {
                splitPanel.add(table);
            }

            this.subTabPanelVisible = subTabPanelVisible;
        }
    }

    @Override
    public SimpleActionTable<T> getTable() {
        return table;
    }

    protected M getModel() {
        return modelProvider.getModel();
    }

}
