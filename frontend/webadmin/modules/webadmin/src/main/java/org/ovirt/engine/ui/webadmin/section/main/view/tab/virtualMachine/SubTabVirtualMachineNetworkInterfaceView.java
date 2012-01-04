package org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmInterfaceListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineNetworkInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.SumUpColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabVirtualMachineNetworkInterfaceView extends AbstractSubTabTableView<VM, VmNetworkInterface, VmListModel, VmInterfaceListModel> implements SubTabVirtualMachineNetworkInterfacePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabVirtualMachineNetworkInterfaceView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabVirtualMachineNetworkInterfaceView(SearchableDetailModelProvider<VmNetworkInterface, VmListModel, VmInterfaceListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    private void initTable() {
        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumnWithTooltip<VmNetworkInterface> networkNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        getTable().addColumn(networkNameColumn, "Network Name");

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        getTable().addColumn(typeColumn, "Type");

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        getTable().addColumn(macColumn, "MAC");

        TextColumnWithTooltip<VmNetworkInterface> speedColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
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

        TextColumnWithTooltip<VmNetworkInterface> rxColumn = new RxTxRateColumn<VmNetworkInterface>() {
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

        TextColumnWithTooltip<VmNetworkInterface> txColumn = new RxTxRateColumn<VmNetworkInterface>() {
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

        TextColumnWithTooltip<VmNetworkInterface> dropsColumn = new SumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                return new Double[] { object.getStatistics().getReceiveDropRate(),
                        object.getStatistics().getTransmitDropRate() };
            }
        };
        getTable().addColumn(dropsColumn, "Drops (Pkts)");

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>("New") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });

        getTable().addActionButton(new UiCommandButtonDefinition<VmNetworkInterface>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
