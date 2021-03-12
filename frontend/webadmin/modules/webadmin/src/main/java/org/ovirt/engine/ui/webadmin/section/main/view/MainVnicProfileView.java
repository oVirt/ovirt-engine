package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainVnicProfilePresenter;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainVnicProfileView extends AbstractMainWithDetailsTableView<VnicProfileView, VnicProfileListModel> implements MainVnicProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainVnicProfileView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainVnicProfileView(MainModelProvider<VnicProfileView, VnicProfileListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VnicProfileView> nameColumn = new AbstractLinkColumn<VnicProfileView>(
                new FieldUpdater<VnicProfileView, String>() {

            @Override
            public void update(int index, VnicProfileView vnicProfile, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), vnicProfile.getName());
                parameters.put(FragmentParams.NETWORK.getName(), vnicProfile.getNetworkName());
                parameters.put(FragmentParams.DATACENTER.getName(),
                        vnicProfile.getDataCenterName());
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.vnicProfileVmSubTabPlace, parameters);
            }

        }) {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVnicProfile(), "200px"); //$NON-NLS-1$

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
            public void update(int index, VnicProfileView vnicProfile, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), vnicProfile.getDataCenterName());
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

        AbstractTextColumn<VnicProfileView> descriptionColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionVnicProfile(), "400px"); //$NON-NLS-1$
    }
}
