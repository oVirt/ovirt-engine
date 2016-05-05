package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractBooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVnicProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabVnicProfileView extends AbstractMainTabWithDetailsTableView<VnicProfileView, VnicProfileListModel> implements MainTabVnicProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVnicProfileView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public MainTabVnicProfileView(MainModelProvider<VnicProfileView, VnicProfileListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<VnicProfileView> nameColumn = new AbstractTextColumn<VnicProfileView>() {
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

        AbstractTextColumn<VnicProfileView> dcColumn = new AbstractTextColumn<VnicProfileView>() {
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
        getTable().addColumn(portMirroringColumn, constants.portMirroringVnicProfile(), "85px"); //$NON-NLS-1$

        AbstractTextColumn<VnicProfileView> descriptionColumn = new AbstractTextColumn<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getDescription();
            }
        };
        descriptionColumn.makeSortable();
        getTable().addColumn(descriptionColumn, constants.descriptionVnicProfile(), "400px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfileView>(constants.newVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfileView>(constants.editVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfileView>(constants.removeVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

    }
}
