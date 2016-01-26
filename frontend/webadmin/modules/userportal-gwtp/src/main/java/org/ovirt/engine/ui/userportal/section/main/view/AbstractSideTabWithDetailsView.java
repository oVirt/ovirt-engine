package org.ovirt.engine.ui.userportal.section.main.view;

import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.view.AbstractView;
import org.ovirt.engine.ui.common.view.SubTabHelper;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.gin.AssetProvider;
import org.ovirt.engine.ui.userportal.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.userportal.section.main.presenter.AbstractSideTabWithDetailsPresenter;
import org.ovirt.engine.ui.userportal.widget.extended.ExtendedViewSplitLayoutPanel;
import org.ovirt.engine.ui.userportal.widget.table.UserPortalSimpleActionTable;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.client.ui.IsWidget;
import com.google.gwt.user.client.ui.SimplePanel;

public abstract class AbstractSideTabWithDetailsView<T, M extends SearchableListModel> extends AbstractView implements AbstractSideTabWithDetailsPresenter.ViewDef<T> {

    protected final SearchableTableModelProvider<T, M> modelProvider;

    @WithElementId
    public final SimpleActionTable<T> table;

    private final ExtendedViewSplitLayoutPanel splitPanel;
    private final SimplePanel subTabPanelContainer = new SimplePanel();
    private final ClientStorage clientStorage;
    private boolean subTabPanelVisible;

    private static final ApplicationResources resources = AssetProvider.getResources();

    public AbstractSideTabWithDetailsView(
            SearchableTableModelProvider<T, M> modelProvider, final ClientStorage clientStorage) {
        this.modelProvider = modelProvider;
        this.table = createActionTable();
        this.table.showRefreshButton();
        this.clientStorage = clientStorage;

        resources.sideTabWithDetailsViewStyle().ensureInjected();
        subTabPanelContainer.setStyleName(resources.sideTabWithDetailsViewStyle().detailsContentPanel());

        splitPanel = new ExtendedViewSplitLayoutPanel(resources.extendedViewSplitterSnap()) {
            @Override
            public void onResize() {
                super.onResize();
                if (subTabPanelVisible) {
                    SubTabHelper.storeSubTabHeight(clientStorage, subTabPanelContainer);
                }
            }
        };

        initWidget(splitPanel);
        initSplitPanel();
    }

    protected SimpleActionTable<T> createActionTable() {
        return new UserPortalSimpleActionTable<T>(modelProvider,
                getTableResources(),
                getTableHeaderResources(),
                ClientGinjectorProvider.getEventBus(),
                ClientGinjectorProvider.getClientStorage()) {
            @Override
            protected String getTableContainerStyleName() {
                return AbstractSideTabWithDetailsView.this.getTableContainerStyleName() == null
                        ? super.getTableContainerStyleName()
                        : AbstractSideTabWithDetailsView.this.getTableContainerStyleName();
            }
        };
    }

    protected Resources getTableResources() {
        return null;
    }

    protected Resources getTableHeaderResources() {
        return null;
    }

    protected String getTableContainerStyleName() {
        return null;
    }

    void initSplitPanel() {
        splitPanel.add(table);
        subTabPanelVisible = false;
    }

    @Override
    public void setInSlot(Object slot, IsWidget content) {
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
                splitPanel.addSouth(subTabPanelContainer, SubTabHelper.getSubTabHeight(clientStorage, splitPanel));
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
