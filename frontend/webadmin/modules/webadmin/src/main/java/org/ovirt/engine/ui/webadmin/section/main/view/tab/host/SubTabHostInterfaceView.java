package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.SubTableResources;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostInterfacePresenter;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.host.HostInterfaceForm;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Overflow;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;

public class SubTabHostInterfaceView extends AbstractSubTabFormView<VDS, HostListModel, HostInterfaceListModel>
        implements SubTabHostInterfacePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    /**
     * An empty column, used to render Host NIC table header.
     */
    private class EmptyColumn extends TextColumn<HostInterfaceLineModel> {
        @Override
        public String getValue(HostInterfaceLineModel object) {
            return null;
        }
    }

    @WithElementId
    final SimpleActionTable<HostInterfaceLineModel> table;
    private final VerticalPanel contentPanel;
    HostInterfaceForm hostInterfaceForm = null;

    @Inject
    public SubTabHostInterfaceView(SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage,
            ApplicationConstants constants,
            ApplicationTemplates templates) {
        super(modelProvider);
        table =
                new SimpleActionTable<HostInterfaceLineModel>(modelProvider,
                        getTableResources(),
                        eventBus,
                        clientStorage);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants, templates);

        contentPanel = new VerticalPanel();
        contentPanel.add(table);
        contentPanel.add(new Label(constants.emptyInterface()));
        initWidget(contentPanel);
    }

    Resources getTableResources() {
        return GWT.<Resources> create(SubTableResources.class);
    }

    void initTable(ApplicationConstants constants, ApplicationTemplates templates) {
        // Interface Panel
        table.addColumn(new EmptyColumn(), constants.empty(), "30px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.nameInterface(), "200px"); //$NON-NLS-1$

        // Bond Panel
        table.addColumn(new EmptyColumn(), constants.bondInterface(), "200px"); //$NON-NLS-1$

        // Vlan Panel
        table.addColumn(new EmptyColumn(), constants.vlanInterface(), "200px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.networkNameInterface(), "200px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.addressInterface(), "120px"); //$NON-NLS-1$

        // Statistics Panel
        table.addColumn(new EmptyColumn(), constants.macInterface(), "120px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.speedInterface(), constants.mbps())
                .asString(), "100px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.rxInterface(), constants.mbps())
                .asString(), "100px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.txInterface(), constants.mbps())
                .asString(), "100px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.dropsInterface(), constants.pkts())
                .asString(), "100px"); //$NON-NLS-1$

        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.addEditInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.editManageNetInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditManagementNetworkCommand();
            }
        });
        // TODO: separator
        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.bondInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getBondCommand();
            }
        });
        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.detachInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
        // TODO: separator
        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.setupHostNetworksInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetupNetworksCommand();
            }
        });

        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.saveNetConfigInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSaveNetworkConfigCommand();
            }
        });

        // The table items are in the form, so the table itself will never have items, so don't display the 'empty
        // message'
        table.table.setEmptyTableWidget(null);
    }

    @Override
    public void removeContent() {
        if (hostInterfaceForm != null) {
            hostInterfaceForm.setVisible(false);
        }
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        // TODO(vszocs) possible performance optimization: don't create HostInterfaceForm upon each selection
        hostInterfaceForm = new HostInterfaceForm(getDetailModel());
        contentPanel.remove(contentPanel.getWidgetCount() - 1);
        contentPanel.add(hostInterfaceForm);
    }

    @Override
    public void setRefreshButtonVisibility(boolean visible) {
        if (visible) {
            table.showRefreshButton();
        } else {
            table.hideRefreshButton();
        }
    }

    @Override
    public void setParentOverflow() {
        if (contentPanel.getParent() != null) {
            contentPanel.getParent().getElement().getStyle().setOverflowX(Overflow.AUTO);
        }
    }

}
