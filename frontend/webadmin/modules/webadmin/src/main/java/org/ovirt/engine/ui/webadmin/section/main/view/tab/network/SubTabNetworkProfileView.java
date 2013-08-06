package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.BooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.inject.Inject;

public class SubTabNetworkProfileView extends AbstractSubTabTableView<NetworkView, VnicProfile, NetworkListModel, NetworkProfileListModel>
        implements SubTabNetworkProfilePresenter.ViewDef {

    @Inject
    public SubTabNetworkProfileView(SearchableDetailModelProvider<VnicProfile, NetworkListModel, NetworkProfileListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VnicProfile> nameColumn =
                new TextColumnWithTooltip<VnicProfile>() {
                    @Override
                    public String getValue(VnicProfile object) {
                        return object.getName();
                    }
                };
        getTable().addColumn(nameColumn, constants.nameNetworkProfile(), "400px"); //$NON-NLS-1$

        BooleanColumn<VnicProfile> portMirroringColumn =
                new BooleanColumn<VnicProfile>(constants.portMirroringEnabled()) {
                    @Override
                    public Boolean getRawValue(VnicProfile object) {
                        return object.isPortMirroring();
                    }
                };
        getTable().addColumnWithHtmlHeader(portMirroringColumn, constants.portMirroringNetworkProfile(), "85px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfile>(constants.newNetworkProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfile>(constants.editNetworkProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfile>(constants.removeNetworkProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
