package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabNetworkProfileView extends AbstractSubTabTableView<NetworkView, VnicProfileView, NetworkListModel, NetworkProfileListModel>
        implements SubTabNetworkProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkProfileView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabNetworkProfileView(SearchableDetailModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel> modelProvider,
            ApplicationConstants constants) {
        super(modelProvider);
        initTable(constants);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        AbstractTextColumnWithTooltip<VnicProfileView> nameColumn =
                new AbstractTextColumnWithTooltip<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVnicProfile(), "400px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VnicProfileView> networkColumn = new AbstractTextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkName();
            }
        };
        networkColumn.makeSortable();
        getTable().addColumn(networkColumn, constants.networkVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VnicProfileView> dcColumn = new AbstractTextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getDataCenterName();
            }
        };
        dcColumn.makeSortable();
        getTable().addColumn(dcColumn, constants.dcVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VnicProfileView> compatibilityVersionColumn =
                new AbstractTextColumnWithTooltip<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getCompatibilityVersion().toString();
                    }
                };
        compatibilityVersionColumn.makeSortable();
        getTable().addColumn(compatibilityVersionColumn, constants.compatibilityVersionVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VnicProfileView> qosColumn = new AbstractTextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkQosName();
            }
        };
        qosColumn.makeSortable();
        getTable().addColumn(qosColumn, constants.qosNameVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractBooleanColumn<VnicProfileView> portMirroringColumn =
                new AbstractBooleanColumn<VnicProfileView>(constants.portMirroringEnabled()) {
                    @Override
                    public Boolean getRawValue(VnicProfileView object) {
                        return object.isPortMirroring();
                    }
                };
        portMirroringColumn.makeSortable();
        getTable().addColumnWithHtmlHeader(portMirroringColumn, constants.portMirroringVnicProfile(), "85px"); //$NON-NLS-1$

        AbstractTextColumnWithTooltip<VnicProfileView> descriptionColumn =
                new AbstractTextColumnWithTooltip<VnicProfileView>() {
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
