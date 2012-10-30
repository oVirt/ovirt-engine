package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.Arrays;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Network;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.view.ViewRadioGroup;
import org.ovirt.engine.ui.common.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostFilter;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkHostListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.HostStatusColumn;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.RadioButton;

public class SubTabNetworkHostView extends AbstractSubTabTableView<Network, VDS, NetworkListModel, NetworkHostListModel>
        implements SubTabNetworkHostPresenter.ViewDef {

    private final ViewRadioGroup<NetworkHostFilter> viewRadioGroup;
    static ApplicationConstants constants;

    @Inject
    public SubTabNetworkHostView(SearchableDetailModelProvider<VDS, NetworkListModel, NetworkHostListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        SubTabNetworkHostView.constants = constants;
        viewRadioGroup = new ViewRadioGroup<NetworkHostFilter>(Arrays.asList(NetworkHostFilter.values()));
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

    private final HostStatusColumn hostStatus = new HostStatusColumn();

    private final TextColumnWithTooltip<VDS> nameColumn = new TextColumnWithTooltip<VDS>() {
        @Override
        public String getValue(VDS object) {
            return object.getvds_name();
        }
    };



    private final TextColumnWithTooltip<VDS> hostColumn = new TextColumnWithTooltip<VDS>() {
        @Override
        public String getValue(VDS object) {
            return object.gethost_name();
        }
    };


    private final TextColumnWithTooltip<VDS> statusColumn = new EnumColumn<VDS, VDSStatus>() {
        @Override
        public VDSStatus getRawValue(VDS object) {
            return object.getstatus();
        }
    };

    private void handleRadioButtonClick(ClickEvent event) {
        getDetailModel().setViewFilterType((viewRadioGroup.getSelectedValue()));

        getTable().ensureColumnPresent(hostStatus, constants.empty(), true, "30px"); //$NON-NLS-1$
        getTable().ensureColumnPresent(nameColumn, constants.nameHost(), true);
        getTable().ensureColumnPresent(hostColumn, constants.ipHost(), true);
        getTable().ensureColumnPresent(statusColumn, constants.statusHost(), true);

    }

    void initTable() {
        initTableOverhead();
        handleRadioButtonClick(null);
    }
}

