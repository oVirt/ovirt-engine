package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.NetworkView;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VdsNetworkInterface;
import org.ovirt.engine.core.common.utils.PairQueryable;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.table.column.RxTxRateColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostFilter;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.host.InterfaceStatusImage;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.WebAdminImageResourceColumn;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.resources.client.ImageResource;
import com.google.gwt.user.client.ui.RadioButton;

public class SubTabNetworkHostView extends AbstractSubTabTableView<NetworkView, PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel>
        implements SubTabNetworkHostPresenter.ViewDef {

    private final ViewRadioGroup<NetworkHostFilter> viewRadioGroup;
    private final ApplicationConstants constants;
    private final ApplicationTemplates templates;

    @Inject
    public SubTabNetworkHostView(SearchableDetailModelProvider<PairQueryable<VdsNetworkInterface, VDS>, NetworkListModel, NetworkHostListModel> modelProvider, ApplicationConstants constants, ApplicationTemplates templates) {
        super(modelProvider);
        this.constants = constants;
        this.templates = templates;
        viewRadioGroup = new ViewRadioGroup<NetworkHostFilter>(Arrays.asList(NetworkHostFilter.values()));
        viewRadioGroup.setSelectedValue(NetworkHostFilter.attached);
        initTable();
        initWidget(getTable());
    }

    void initTableOverhead() {
        viewRadioGroup.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                if (((RadioButton) event.getSource()).getValue()) {
                    handleRadioButtonClick(event);
                }
            }
        });

        getTable().setTableOverhead(viewRadioGroup);
        getTable().setTableTopMargin(20);
    }

    private final HostStatusColumn<PairQueryable<VdsNetworkInterface, VDS>> hostStatus = new HostStatusColumn<PairQueryable<VdsNetworkInterface, VDS>>();

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nameColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getvds_name();
        }
    };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> clusterColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getvds_group_name();
        }
    };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> dcColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            return object.getSecond().getstorage_pool_name();
        }
    };

    WebAdminImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>> nicStatusColumn = new WebAdminImageResourceColumn<PairQueryable<VdsNetworkInterface, VDS>>(){

        @Override
        public ImageResource getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return InterfaceStatusImage.getResource(object.getFirst().getStatistics().getStatus());
            }
            return null;
        }
    };


    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nicColumn = new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return object.getFirst().getName();
            }
            return null;
        }
    };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> speedColumn =
            new TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>>() {
                @Override
                public String getValue(PairQueryable<VdsNetworkInterface, VDS> object) {
                    if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                        return String.valueOf(object.getFirst().getSpeed());
                    }
                    return null;
                }
            };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nicRxColumn = new RxTxRateColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        protected Double getRate(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return object.getFirst().getStatistics().getReceiveRate();
            }
            return null;
        }

        @Override
        protected Double getSpeed(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                return object.getFirst().getSpeed().doubleValue();
            } else {
                return null;
            }
        }
    };

    private final TextColumnWithTooltip<PairQueryable<VdsNetworkInterface, VDS>> nicTxColumn = new RxTxRateColumn<PairQueryable<VdsNetworkInterface, VDS>>() {
        @Override
        protected Double getRate(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null){
                return object.getFirst().getStatistics().getTransmitRate();
            }else{
                return null;
            }
        }

        @Override
        protected Double getSpeed(PairQueryable<VdsNetworkInterface, VDS> object) {
            if (object.getFirst() != null && object.getFirst().getSpeed() != null) {
                return object.getFirst().getSpeed().doubleValue();
            } else {
                return null;
            }
        }
    };

    private void handleRadioButtonClick(ClickEvent event) {
        getDetailModel().setViewFilterType((viewRadioGroup.getSelectedValue()));

        boolean all = viewRadioGroup.getSelectedValue() == NetworkHostFilter.all;
        boolean attached = viewRadioGroup.getSelectedValue() == NetworkHostFilter.attached;
        boolean unattached = viewRadioGroup.getSelectedValue() == NetworkHostFilter.unattached;

        getTable().ensureColumnPresent(hostStatus, constants.empty(), all || attached || unattached, "30px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nameColumn, constants.nameHost(), all || attached || unattached);
        getTable().ensureColumnPresent(clusterColumn, constants.clusterHost(), all || attached || unattached);
        getTable().ensureColumnPresent(dcColumn, constants.dcHost(), all || attached || unattached);
        getTable().ensureColumnPresent(nicStatusColumn, constants.statusNetworkHost(), attached, "140px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nicColumn, constants.nicNetworkHost(), attached);
        getTable().ensureColumnPresent(speedColumn, constants.speedNetworkHost(), attached);
        getTable().ensureColumnPresent(nicRxColumn, templates.sub(constants.rxNetworkHost(), constants.mbps()).asString(), attached);
        getTable().ensureColumnPresent(nicTxColumn, templates.sub(constants.txNetworkHost(), constants.mbps()).asString(), attached);
    }

    void initTable() {
        initTableOverhead();
        handleRadioButtonClick(null);

        getTable().addActionButton(new WebAdminButtonDefinition<PairQueryable<VdsNetworkInterface, VDS>>(constants.setupHostNetworksInterface()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSetupNetworksCommand();
            }
        });
    }
}

