package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.List;

import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.webadmin.widget.form.DnDPanel;

import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragEnterHandler;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragLeaveHandler;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DragOverHandler;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.event.dom.client.DropHandler;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;

public class NetworkGroup extends DnDPanel {

    private final NetworkPanelsStyle style;
    private final NetworkInterfaceModel nicModel;
    private final FlexTable table;

    public NetworkGroup(NetworkInterfaceModel nicModel, final NetworkPanelsStyle style) {
        super(false);
        this.style = style;
        this.nicModel = nicModel;
        table = new FlexTable();
        List<LogicalNetworkModel> networks = nicModel.getItems();
        int networkSize = networks.size();

        // style
        table.setCellSpacing(5);
        table.getElement().addClassName(style.groupPanel());

        // columns
        ColumnFormatter columnFormatter = table.getColumnFormatter();
        columnFormatter.setWidth(0, "45%");
        columnFormatter.setWidth(1, "10%");
        columnFormatter.setWidth(2, "45%");

        // rows
        FlexCellFormatter flexCellFormatter = table.getFlexCellFormatter();
        flexCellFormatter.setRowSpan(0, 1, networkSize > 1 ? networkSize : 1);

        // nic
        if (nicModel instanceof BondNetworkInterfaceModel) {
            table.setWidget(0, 0, new BondPanel((BondNetworkInterfaceModel) nicModel, style));
        } else {
            table.setWidget(0, 0, new NicPanel(nicModel, style));
        }

        // connector
        ConnectorPanel connector = new ConnectorPanel(nicModel, style);
        table.setWidget(0, 1, connector);

        // network
        Collections.sort(networks);
        if (networkSize > 0) {
            flexCellFormatter.setRowSpan(0, 0, networkSize);
            for (int i = 0; i < networkSize; i++) {
                table.setWidget(i, i > 0 ? 1 : 2, new NetworkPanel(networks.get(i), style));
                if (i > 0) {
                    table.removeCell(i, 0);
                }
            }
        } else {
            SimplePanel emptyPanel = new SimplePanel();
            Label label = new Label("no netowrk assigned");
            label.getElement().getStyle().setPadding(10, Unit.PX);
            emptyPanel.setWidget(label);
            emptyPanel.setStylePrimaryName(style.emptyPanel());
            table.setWidget(0, 2, emptyPanel);
        }

        // drag enter
        addBitlessDomHandler(new DragEnterHandler() {
            @Override
            public void onDragEnter(DragEnterEvent event) {
                doDrag(event, false);
            }
        }, DragEnterEvent.getType());

        // drag over
        addBitlessDomHandler(new DragOverHandler() {

            @Override
            public void onDragOver(DragOverEvent event) {
                doDrag(event, false);
            }
        }, DragOverEvent.getType());

        // drag leave
        addBitlessDomHandler(new DragLeaveHandler() {

            @Override
            public void onDragLeave(DragLeaveEvent event) {
                table.getElement().removeClassName(style.networkGroupDragOver());
            }
        }, DragLeaveEvent.getType());

        // drop
        addBitlessDomHandler(new DropHandler() {

            @Override
            public void onDrop(DropEvent event) {
                doDrag(event, true);
                table.getElement().removeClassName(style.networkGroupDragOver());
            }
        }, DropEvent.getType());
        setWidget(table);
    }

    private void doDrag(DragDropEventBase<?> event, boolean isDrop) {
        HostSetupNetworksModel setupModel = nicModel.getSetupModel();
        String data = event.getData(NetworkItemPanel.SETUP_NETWORKS_DATA);
        if (data != null) {
            if (setupModel.candidateOperation(data, nicModel.getName(), isDrop)) {
                table.getElement().addClassName(style.networkGroupDragOver());
                // allow drag/drop (look at http://www.w3.org/TR/html5/dnd.html#dndevents)
                event.preventDefault();
            }
        }
    }

}
