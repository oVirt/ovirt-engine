package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.NicActivateStatusColumn;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.VmStatusColumn;

import com.google.gwt.core.client.GWT;

public class SubTabNetworkVmView extends AbstractSubTabTableView<NetworkView, PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel>
        implements SubTabNetworkVmPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkVmView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabNetworkVmView(SearchableDetailModelProvider<PairQueryable<VmNetworkInterface, VM>, NetworkListModel, NetworkVmListModel> modelProvider, ApplicationConstants constants, ApplicationTemplates templates) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants, templates);
        initWidget(getTable());
    }

    void initTable(final ApplicationConstants constants, final ApplicationTemplates templates) {
        getTable().addColumn(new VmStatusColumn<PairQueryable<VmNetworkInterface, VM>>(), constants.empty(), "30px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> nameColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getSecond().getVmName();
            }
        };
        getTable().addColumn(nameColumn, constants.nameVm(), "120px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> clusterColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getSecond().getVdsGroupName();
            }
        };
        getTable().addColumn(clusterColumn, constants.clusterVm(), "120px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> ipColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getSecond().getVmIp();
            }
        };
        getTable().addColumn(ipColumn, constants.ipVm());

        getTable().addColumn(new NicActivateStatusColumn<PairQueryable<VmNetworkInterface, VM>>(), constants.vnicStatusNetworkVM(), "70px"); //$NON-NLS-1$

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> vnicNameColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getFirst().getName();
            }
        };
        getTable().addColumn(vnicNameColumn, constants.vnicNetworkVM());

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> rxColumn = new RxTxRateColumn<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            protected Double getRate(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getFirst().getStatistics().getReceiveRate();
            }

            @Override
            protected Double getSpeed(PairQueryable<VmNetworkInterface, VM> object) {
                if (object.getFirst().getSpeed() != null) {
                    return object.getFirst().getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        getTable().addColumnWithHtmlHeader(rxColumn, templates.sub(constants.rxNetworkVM(), constants.mbps()).asString());

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> txColumn = new RxTxRateColumn<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            protected Double getRate(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getFirst().getStatistics().getTransmitRate();
            }

            @Override
            protected Double getSpeed(PairQueryable<VmNetworkInterface, VM> object) {
                if (object.getFirst().getSpeed() != null) {
                    return object.getFirst().getSpeed().doubleValue();
                } else {
                    return null;
                }
            }
        };
        getTable().addColumnWithHtmlHeader(txColumn, templates.sub(constants.txNetworkVM(), constants.mbps()).asString());

        TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>> descriptionColumn = new TextColumnWithTooltip<PairQueryable<VmNetworkInterface, VM>>() {
            @Override
            public String getValue(PairQueryable<VmNetworkInterface, VM> object) {
                return object.getSecond().getDescription();
            }
        };
        getTable().addColumn(descriptionColumn, constants.descriptionVm());

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VmNetworkInterface, VM>>(constants.removeInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

    }

}

