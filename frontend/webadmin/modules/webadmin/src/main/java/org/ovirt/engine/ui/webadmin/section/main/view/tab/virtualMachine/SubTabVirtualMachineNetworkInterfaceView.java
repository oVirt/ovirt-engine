package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineNetworkInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.SumUpColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class SubTabVirtualMachineNetworkInterfaceView extends AbstractSubTabTableView<VM, VmNetworkInterface, VmListModel, VmInterfaceListModel> implements SubTabVirtualMachineNetworkInterfacePresenter.ViewDef {

    @Inject
    public SubTabVirtualMachineNetworkInterfaceView(SearchableDetailModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    private void initTable() {
        TextColumn<VmNetworkInterface> nameColumn = new TextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<VmNetworkInterface> networkNameColumn = new TextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        getTable().addColumn(networkNameColumn, "Network Name");

        TextColumn<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumn<VmNetworkInterface> macColumn = new TextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        getTable().addColumn(macColumn, "MAC");

        TextColumn<VmNetworkInterface> speedColumn = new TextColumn<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().toString();
                } else {
                    return null;
                }
            }
        };
        getTable().addColumn(speedColumn, "Speed (Mbps)");

        TextColumn<VmNetworkInterface> rxColumn = new RxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getReceiveRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        getTable().addColumn(rxColumn, "Rx (Mbps)");

        TextColumn<VmNetworkInterface> txColumn = new RxTxRateColumn<VmNetworkInterface>() {
            @Override
            protected Double getRate(VmNetworkInterface object) {
                return object.getStatistics().getTransmitRate();
            }

            @Override
            protected Double getSpeed(VmNetworkInterface object) {
                if (object.getSpeed() != null) {
                    return object.getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        getTable().addColumn(txColumn, "Tx (Mbps)");

        TextColumn<VmNetworkInterface> dropsColumn = new SumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                return new Double[] { object.getStatistics().getReceiveDropRate(),
                        object.getStatistics().getTransmitDropRate() };
            }
        };
        getTable().addColumn(dropsColumn, "Drops (Pkts)");
        
        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getDetailModel().getNewCommand(),
                "New"));
        
        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getDetailModel().getEditCommand(),
                "Edit"));
        
        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>(getDetailModel().getRemoveCommand(),
                "Remove"));
    }

}
