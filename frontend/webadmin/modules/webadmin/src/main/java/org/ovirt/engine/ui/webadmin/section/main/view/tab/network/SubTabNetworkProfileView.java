package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
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

public class SubTabNetworkProfileView extends AbstractSubTabTableView<NetworkView, VnicProfileView, NetworkListModel, NetworkProfileListModel>
        implements SubTabNetworkProfilePresenter.ViewDef {

    @Inject
    public SubTabNetworkProfileView(SearchableDetailModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VnicProfileView> nameColumn =
                new TextColumnWithTooltip<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVnicProfile(), "400px"); //$NON-NLS-1$

        TextColumnWithTooltip<VnicProfileView> networkColumn = new TextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkName();
            }
        };
        networkColumn.makeSortable();
        getTable().addColumn(networkColumn, constants.networkVnicProfile(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<VnicProfileView> dcColumn = new TextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getDataCenterName();
            }
        };
        dcColumn.makeSortable();
        getTable().addColumn(dcColumn, constants.dcVnicProfile(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<VnicProfileView> compatibilityVersionColumn =
                new TextColumnWithTooltip<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getCompatibilityVersion().toString();
                    }
                };
        compatibilityVersionColumn.makeSortable();
        getTable().addColumn(compatibilityVersionColumn, constants.compatibilityVersionVnicProfile(), "200px"); //$NON-NLS-1$

        TextColumnWithTooltip<VnicProfileView> qosColumn = new TextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkQosName();
            }
        };
        qosColumn.makeSortable();
        getTable().addColumn(qosColumn, constants.qosNameVnicProfile(), "200px"); //$NON-NLS-1$

        BooleanColumn<VnicProfileView> portMirroringColumn =
                new BooleanColumn<VnicProfileView>(constants.portMirroringEnabled()) {
                    @Override
                    public Boolean getRawValue(VnicProfileView object) {
                        return object.isPortMirroring();
                    }
                };
        portMirroringColumn.makeSortable();
        getTable().addColumnWithHtmlHeader(portMirroringColumn, constants.portMirroringVnicProfile(), "85px"); //$NON-NLS-1$

        TextColumnWithTooltip<VnicProfileView> descriptionColumn =
                new TextColumnWithTooltip<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getDescription();
                    }
                };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionVnicProfile(), "400px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfileView>(constants.newNetworkProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfileView>(constants.editNetworkProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfileView>(constants.removeNetworkProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
    }

}
