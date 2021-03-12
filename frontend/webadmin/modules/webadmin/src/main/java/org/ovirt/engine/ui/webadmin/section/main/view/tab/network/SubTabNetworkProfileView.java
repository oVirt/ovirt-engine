package org.ovirt.engine.ui.webadmin.section.main.view.tab.network;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.NetworkView;
import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkListModel;
import org.ovirt.engine.ui.uicommonweb.models.networks.NetworkProfileListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.network.SubTabNetworkProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class SubTabNetworkProfileView extends AbstractSubTabTableView<NetworkView, VnicProfileView, NetworkListModel, NetworkProfileListModel>
        implements SubTabNetworkProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabNetworkProfileView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabNetworkProfileView(SearchableDetailModelProvider<VnicProfileView, NetworkListModel, NetworkProfileListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTableContainer());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VnicProfileView> nameColumn =
                new AbstractTextColumn<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getName();
                    }
                };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVnicProfile(), "400px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> networkColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkName();
            }
        };
        networkColumn.makeSortable();
        getTable().addColumn(networkColumn, constants.networkVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> dcColumn = new AbstractLinkColumn<VnicProfileView>(
                new FieldUpdater<VnicProfileView, String>() {
            @Override
            public void update(int index, VnicProfileView profileView, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), profileView.getDataCenterName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.dataCenterStorageSubTabPlace, parameters);
            }
        }) {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getDataCenterName();
            }
        };
        dcColumn.makeSortable();
        getTable().addColumn(dcColumn, constants.dcVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> compatibilityVersionColumn =
                new AbstractTextColumn<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getCompatibilityVersion().toString();
                    }
                };
        compatibilityVersionColumn.makeSortable();
        getTable().addColumn(compatibilityVersionColumn, constants.compatibilityVersionVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> qosColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkQosName();
            }
        };
        qosColumn.makeSortable();
        getTable().addColumn(qosColumn, constants.qosNameVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> networkFilterColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getNetworkFilterName();
            }
        };
        networkFilterColumn.makeSortable();
        getTable().addColumn(networkFilterColumn, constants.networkFilterNameVnicProfile(), "200px"); //$NON-NLS-1$

        AbstractBooleanColumn<VnicProfileView> portMirroringColumn =
                new AbstractBooleanColumn<VnicProfileView>(constants.portMirroringEnabled()) {
                    @Override
                    public Boolean getRawValue(VnicProfileView object) {
                        return object.isPortMirroring();
                    }
                };
        portMirroringColumn.makeSortable();
        getTable().addColumn(portMirroringColumn, constants.portMirroringVnicProfile(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> passthroughColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.isPassthrough() ? constants.yes() : constants.no();
            }
        };
        passthroughColumn.makeSortable();
        getTable().addColumn(passthroughColumn, constants.passthroughVnicProfile(), "100px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> failoverColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getFailoverVnicProfileName();
            }
        };
        failoverColumn.makeSortable();
        getTable().addColumn(failoverColumn, constants.failoverVnicProfile(), "150px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> descriptionColumn =
                new AbstractTextColumn<VnicProfileView>() {
                    @Override
                    public String getValue(VnicProfileView object) {
                        return object.getDescription();
                    }
                };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionVnicProfile(), "400px"); //$NON-NLS-1$
    }
}
