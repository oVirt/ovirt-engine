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
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
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

public class SubTabHostInterfaceView extends AbstractSubTabFormView<VDS, HostListModel<Void>, HostInterfaceListModel>
        implements SubTabHostInterfacePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabHostInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    /**
     * An empty column, used to render Host NIC table header.
     */
    private static class EmptyColumn extends TextColumn<HostInterfaceLineModel> {
        @Override
        public String getValue(HostInterfaceLineModel object) {
            return null;
        }
    }

    @WithElementId
    final SimpleActionTable<HostInterfaceLineModel> table;
    private final VerticalPanel contentPanel;
    HostInterfaceForm hostInterfaceForm = null;

    private static final ApplicationTemplates templates = AssetProvider.getTemplates();
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabHostInterfaceView(SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel<Void>, HostInterfaceListModel> modelProvider,
            EventBus eventBus,
            ClientStorage clientStorage) {
        super(modelProvider);
        table = new SimpleActionTable<>(modelProvider, getTableResources(), eventBus, clientStorage);
        initTable();

        contentPanel = new VerticalPanel();
        contentPanel.add(table);
        contentPanel.add(new Label(constants.emptyInterface()));
        initWidget(contentPanel);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    Resources getTableResources() {
        return GWT.create(SubTableResources.class);
    }

    void initTable() {
        // Interface Panel
        table.addColumn(new EmptyColumn(), constants.empty(), "30px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.nameInterface(), "200px"); //$NON-NLS-1$

        // Bond Panel
        table.addColumn(new EmptyColumn(), constants.bondInterface(), "200px"); //$NON-NLS-1$

        // Vlan Panel
        table.addColumn(new EmptyColumn(), constants.vlanInterface(), "200px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.hostOutOfSync(), "75px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.networkNameInterface(), "200px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.ipv4AddressInterface(), "120px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.ipv6AddressInterface(), "200px"); //$NON-NLS-1$

        // Statistics Panel
        table.addColumn(new EmptyColumn(), constants.macInterface(), "120px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.speedInterface(), constants.mbps()), "100px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.rxRate(), constants.mbps()), "100px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.txRate(), constants.mbps()), "100px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.rxTotal(), constants.bytes()), "150px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.txTotal(), constants.bytes()), "150px"); //$NON-NLS-1$
        table.addColumnWithHtmlHeader(new EmptyColumn(), templates.sub(constants.dropsInterface(), constants.pkts()), "100px"); //$NON-NLS-1$

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

        table.addActionButton(new WebAdminButtonDefinition<HostInterfaceLineModel>(constants.syncAllHostNetworks()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSyncAllHostNetworksCommand();
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
        hostInterfaceForm = new HostInterfaceForm(getDetailModel(), selectedItem);
        contentPanel.remove(contentPanel.getWidgetCount() - 1);
        contentPanel.add(hostInterfaceForm);
    }

    @Override
    public void setParentOverflow() {
        if (contentPanel.getParent() != null) {
            contentPanel.getParent().getElement().getStyle().setOverflowX(Overflow.AUTO);
        }
    }

}
