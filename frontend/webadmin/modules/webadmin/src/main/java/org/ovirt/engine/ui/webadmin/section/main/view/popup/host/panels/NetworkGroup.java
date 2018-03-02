package org.ovirt.engine.ui.webadmin.section.main.view.popup.host.panels;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostSetupNetworksModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.BondNetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.LogicalNetworkModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkInterfaceModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.network.NetworkLabelModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.AutoScrollDisableEvent;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.AutoScrollEnableEvent;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.AutoScrollOverEvent;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NativeEvent;
import com.google.gwt.event.dom.client.DragDropEventBase;
import com.google.gwt.event.dom.client.DragEnterEvent;
import com.google.gwt.event.dom.client.DragLeaveEvent;
import com.google.gwt.event.dom.client.DragOverEvent;
import com.google.gwt.event.dom.client.DropEvent;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;
import com.google.gwt.user.client.ui.FocusPanel;
import com.google.gwt.user.client.ui.HTMLTable.ColumnFormatter;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.web.bindery.event.shared.EventBus;

public class NetworkGroup extends FocusPanel {

    private final NetworkPanelsStyle style;
    private final NetworkInterfaceModel nicModel;
    private final FlexTable table;
    private NetworkItemPanel panel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();


    private int dragCounter = 0; // handle enter/leaves on children, only need to actually enter and leave once.


    public NetworkGroup(NetworkInterfaceModel nicModel, final EventBus eventBus, final NetworkPanelsStyle style) {
        super();
        getElement().setDraggable(Element.DRAGGABLE_FALSE);

        this.style = style;
        this.nicModel = nicModel;
        this.table = new FlexTable();

        List<NetworkLabelModel> labels = nicModel.getLabels();
        SortedSet<LogicalNetworkModel> networks = new TreeSet<>(nicModel.getItems());
        for (NetworkLabelModel label : labels) {
            networks.removeAll(label.getNetworks());
        }
        int networkSize = nicModel.getTotalItemSize();

        // style
        table.addStyleName("ts5"); //$NON-NLS-1$
        table.getElement().addClassName(style.groupPanel());

        // columns
        ColumnFormatter columnFormatter = table.getColumnFormatter();
        columnFormatter.setWidth(0, "45%"); //$NON-NLS-1$
        columnFormatter.setWidth(1, "10%"); //$NON-NLS-1$
        columnFormatter.setWidth(2, "45%"); //$NON-NLS-1$

        // rows
        FlexCellFormatter flexCellFormatter = table.getFlexCellFormatter();
        flexCellFormatter.setRowSpan(0, 1, Math.max(networkSize, 1));

        // nic
        if (nicModel instanceof BondNetworkInterfaceModel) {
            panel = new BondPanel((BondNetworkInterfaceModel) nicModel, style);
            table.setWidget(0, 0, panel);
        } else {
            panel = new NicPanel<>(nicModel, style);
            table.setWidget(0, 0, panel);
        }

        // connector
        ConnectorPanel connector = new ConnectorPanel(nicModel, style);
        table.setWidget(0, 1, connector);

        // labels and networks
        Collections.sort(labels);
        if (networkSize > 0) {
            flexCellFormatter.setRowSpan(0, 0, networkSize);
            FlexTable networkTable = createNetworkTable(labels, networks);
            table.setWidget(0, 2, networkTable);
        } else {
            SimplePanel emptyPanel = new SimplePanel();
            Label label = new Label(constants.noNetworkAssigned());
            label.getElement().addClassName(style.emptyPanelLabel());
            emptyPanel.setWidget(label);
            emptyPanel.setStylePrimaryName(style.emptyPanel());
            table.setWidget(0, 2, emptyPanel);
        }

        // drag over -- check the operation of the thing the user is currently over
        addBitlessDomHandler(event -> {
            NativeEvent ne = event.getNativeEvent();
            eventBus.fireEvent(new AutoScrollOverEvent(NetworkGroup.this,
                    ne.getScreenX(), ne.getScreenY(), ne.getClientX(), ne.getClientY()));
            doDrag(event, false);
        }, DragOverEvent.getType());

        // drag enter
        addBitlessDomHandler(event -> {
            dragCounter++;
            eventBus.fireEvent(new AutoScrollEnableEvent(NetworkGroup.this));
        }, DragEnterEvent.getType());

        // drag leave
        addBitlessDomHandler(event -> {
            dragCounter--;
            if (dragCounter == 0) {
                eventBus.fireEvent(new AutoScrollDisableEvent(NetworkGroup.this));
                table.getElement().removeClassName(style.networkGroupDragOver());
            }
        }, DragLeaveEvent.getType());

        // drop
        addBitlessDomHandler(event -> {
            eventBus.fireEvent(new AutoScrollDisableEvent(NetworkGroup.this));
            event.preventDefault();
            doDrag(event, true);
            table.getElement().removeClassName(style.networkGroupDragOver());
        }, DropEvent.getType());
        setWidget(table);
    }

    private FlexTable createNetworkTable(Iterable<NetworkLabelModel> labels, Iterable<LogicalNetworkModel> networks) {
        FlexTable networkTable = new FlexTable();

        int i = 0;
        Iterator<NetworkLabelModel> labelIterator = labels.iterator();
        Iterator<LogicalNetworkModel> networkIterator = networks.iterator();
        while (labelIterator.hasNext()) {
            networkTable.setWidget(i++, 0, new NetworkLabelPanel(labelIterator.next(), style));
        }
        while (networkIterator.hasNext()) {
            networkTable.setWidget(i++, 0, new InternalNetworkPanel(networkIterator.next(), style));
        }

        networkTable.setWidth("100%"); //$NON-NLS-1$

        return networkTable;
    }

    private void doDrag(DragDropEventBase<?> event, boolean isDrop) {
        HostSetupNetworksModel setupModel = nicModel.getSetupModel();
        String dragDropEventData = NetworkItemPanel.getDragDropEventData(event, isDrop);
        String type = NetworkItemPanel.getType(dragDropEventData);
        String data = NetworkItemPanel.getData(dragDropEventData);
        if (!StringHelper.isNullOrEmpty(data)) {
            if (setupModel.candidateOperation(data, type, nicModel.getName(), HostSetupNetworksModel.NIC, isDrop)) {
                table.getElement().addClassName(style.networkGroupDragOver());
                // allow drag/drop (look at http://www.w3.org/TR/html5/dnd.html#dndevents)
                event.preventDefault();
            }
        }
    }

    public void redrawPanelTooltip() {
        panel.redrawTooltip();
    }
}
