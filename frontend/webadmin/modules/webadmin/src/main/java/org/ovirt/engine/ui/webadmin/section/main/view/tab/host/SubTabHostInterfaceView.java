package org.ovirt.engine.ui.webadmin.section.main.view.tab.host;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceLineModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView.SubTableResources;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.host.HostInterfaceForm;
import org.ovirt.engine.ui.webadmin.widget.table.ActionTableDataProvider;
import org.ovirt.engine.ui.webadmin.widget.table.SimpleActionTable;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.gwt.user.cellview.client.CellTable.Resources;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.HasData;

public class SubTabHostInterfaceView extends AbstractSubTabFormView<VDS, HostListModel, HostInterfaceListModel>
        implements SubTabHostInterfacePresenter.ViewDef {

    /**
     * A Column for an empty Grid
     */
    private class EmptyColumn extends TextColumn<HostInterfaceLineModel> {
        @Override
        public String getValue(HostInterfaceLineModel object) {
            return null;
        }
    }

    /**
     * A Provider for an empty grid
     */
    private class EmptyProvider implements ActionTableDataProvider<HostInterfaceLineModel> {

        private final List<HasData<HostInterfaceLineModel>> displays = new ArrayList<HasData<HostInterfaceLineModel>>();
        private final List<HostInterfaceLineModel> values = new ArrayList<HostInterfaceLineModel>();

        @Override
        public void addDataDisplay(HasData<HostInterfaceLineModel> display) {
            displays.add(display);
        }

        @Override
        public boolean canGoBack() {
            return false;
        }

        @Override
        public boolean canGoForward() {
            return false;
        }

        @Override
        public Object getKey(HostInterfaceLineModel item) {
            return null;
        }

        @Override
        public void goBack() {
        }

        @Override
        public void goForward() {

        }

        @Override
        public void refresh() {
        }

        public void setEmptyData() {
            for (HasData<HostInterfaceLineModel> display : displays) {
                display.setRowCount(0);
                display.setRowData(0, values);
            }
        }
    }

    private EmptyProvider dataProvider;

    private final VerticalPanel panel;

    private SimpleActionTable<HostInterfaceLineModel> table;

    @Inject
    public SubTabHostInterfaceView(SearchableDetailModelProvider<HostInterfaceLineModel, HostListModel, HostInterfaceListModel> modelProvider) {
        super(modelProvider);
        initTable();
        panel = new VerticalPanel();
        panel.add(table);
        panel.add(new Label("Empty"));
        initWidget(panel);
    }

    @Override
    public void setMainTabSelectedItem(VDS selectedItem) {
        HostInterfaceForm hostInterfaceForm = new HostInterfaceForm(getDetailModel());
        panel.remove(panel.getWidgetCount() - 1);
        panel.add(hostInterfaceForm);
        dataProvider.setEmptyData();
    }

    void initTable() {
        Resources resources = GWT.<Resources> create(SubTableResources.class);
        dataProvider = new EmptyProvider();
        table = new SimpleActionTable<HostInterfaceLineModel>(dataProvider, resources);

        table.addColumn(new EmptyColumn(), "", "10%");
        table.addColumn(new EmptyColumn(), "Name", "20%");
        table.addColumn(new EmptyColumn(), "Address", "20%");
        table.addColumn(new EmptyColumn(), "MAC", "20%");
        table.addColumnWithHtmlHeader(new EmptyColumn(), "Speed <sub>(Mbps)</sub>", "10%");
        table.addColumnWithHtmlHeader(new EmptyColumn(), "Rx <sub>(Mbps)</sub>", "10%");
        table.addColumnWithHtmlHeader(new EmptyColumn(), "Tx <sub>(Mbps)</sub>", "10%");
        table.addColumnWithHtmlHeader(new EmptyColumn(), "Drops <sub>(Pkts)</sub>", "10%");
        table.addColumn(new EmptyColumn(), "Bond", "20%");
        table.addColumn(new EmptyColumn(), "Vlan", "20%");
        table.addColumn(new EmptyColumn(), "Network Name", "20%");
        table.addActionButton(
                new UiCommandButtonDefinition<HostInterfaceLineModel>(getDetailModel().getEditCommand(), "Add / Edit"));
        table.addActionButton(
                new UiCommandButtonDefinition<HostInterfaceLineModel>(getDetailModel().getEditManagementNetworkCommand(),
                        "Edit Management Network"));
        // TODO: separator
        table.addActionButton(
                new UiCommandButtonDefinition<HostInterfaceLineModel>(getDetailModel().getBondCommand(), "Bond"));
        table.addActionButton(
                new UiCommandButtonDefinition<HostInterfaceLineModel>(getDetailModel().getDetachCommand(), "Detach"));
        // TODO: separator
        table.addActionButton(
                new UiCommandButtonDefinition<HostInterfaceLineModel>(getDetailModel().getSaveNetworkConfigCommand(),
                        "Save Network Configuration"));

        table.showRefreshButton();
    }
}
