package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.SumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;

public class BaseInterfaceListModelTable<T extends SearchableListModel> extends AbstractModelBoundTableWidget<VmNetworkInterface, T> {

    public BaseInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, T> modelProvider,
            EventBus eventBus, ClientStorage clientStorage) {
        super(modelProvider, eventBus, clientStorage, false);
    }

    @Override
    public void initTable() {
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
    }

}
