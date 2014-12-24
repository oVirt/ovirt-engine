package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.network.VmInterfaceType;
import org.ovirt.engine.core.common.businessentities.network.VmNetworkInterface;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.uicommon.model.SearchableTableModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.BooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.CheckboxColumn;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.SumUpColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTableWidget;
import org.ovirt.engine.ui.uicommonweb.models.SearchableListModel;

import com.google.gwt.event.shared.EventBus;

public class PoolInterfaceListModelTable<T extends SearchableListModel> extends AbstractModelBoundTableWidget<VmNetworkInterface, T> {

    private final CommonApplicationTemplates templates;

    public PoolInterfaceListModelTable(
            SearchableTableModelProvider<VmNetworkInterface, T> modelProvider,
            EventBus eventBus, ClientStorage clientStorage, CommonApplicationTemplates templates) {
        super(modelProvider, eventBus, clientStorage, false);
        this.templates = templates;
    }

    @Override
    public void initTable(final CommonApplicationConstants constants) {
        getTable().addColumn(new NicActivateStatusColumn<VmNetworkInterface>(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> nameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameInterface());

        CheckboxColumn<VmNetworkInterface> pluggedColumn = new CheckboxColumn<VmNetworkInterface>() {
            @Override
            public Boolean getValue(VmNetworkInterface object) {
                return object.isPlugged();
            }

            @Override
            protected boolean canEdit(VmNetworkInterface object) {
                return false;
            }
        };

        getTable().addColumnWithHtmlHeader(pluggedColumn, constants.plugged(), "60px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> networkNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getNetworkName();
            }
        };
        getTable().addColumn(networkNameColumn, constants.networkNameInterface());

        TextColumnWithTooltip<VmNetworkInterface> profileNameColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getVnicProfileName();
            }
        };

        getTable().addColumn(profileNameColumn, constants.profileNameInterface());

        BooleanColumn<VmNetworkInterface> linkStateColumn =
                new BooleanColumn<VmNetworkInterface>(constants.linkedNetworkInterface(),
                        constants.unlinkedNetworkInterface()) {
                    @Override
                    protected Boolean getRawValue(VmNetworkInterface object) {
                        return object.isLinked();
                    }
                };

        getTable().addColumnWithHtmlHeader(linkStateColumn, constants.linkStateNetworkInterface(), "65px"); //$NON-NLS-1$

        TextColumnWithTooltip<VmNetworkInterface> typeColumn = new EnumColumn<VmNetworkInterface, VmInterfaceType>() {
            @Override
            protected VmInterfaceType getRawValue(VmNetworkInterface object) {
                return VmInterfaceType.forValue(object.getType());
            }
        };
        getTable().addColumn(typeColumn, constants.typeInterface());

        TextColumnWithTooltip<VmNetworkInterface> macColumn = new TextColumnWithTooltip<VmNetworkInterface>() {
            @Override
            public String getValue(VmNetworkInterface object) {
                return object.getMacAddress();
            }
        };
        getTable().addColumn(macColumn, constants.macInterface());

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
        getTable().addColumnWithHtmlHeader(speedColumn,
                templates.sub(constants.speedInterface(), constants.mbps()).asString());

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
        getTable().addColumnWithHtmlHeader(rxColumn,
                templates.sub(constants.rxRate(), constants.mbps()).asString());

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
        getTable().addColumnWithHtmlHeader(txColumn,
                templates.sub(constants.txRate(), constants.mbps()).asString());

        TextColumnWithTooltip<VmNetworkInterface> dropsColumn = new SumUpColumn<VmNetworkInterface>() {
            @Override
            protected Double[] getRawValue(VmNetworkInterface object) {
                Double receiveDropRate = object != null ? object.getStatistics().getReceiveDropRate() : null;
                Double transmitDropRate = object != null ? object.getStatistics().getTransmitDropRate() : null;
                return new Double[] { receiveDropRate, transmitDropRate };
            }
        };
        getTable().addColumnWithHtmlHeader(dropsColumn,
                templates.sub(constants.dropsInterface(), constants.pkts()).asString());
    }

}
