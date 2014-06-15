package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.BooleanColumn;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVnicProfilePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabVnicProfileView extends AbstractMainTabWithDetailsTableView<VnicProfileView, VnicProfileListModel> implements MainTabVnicProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVnicProfileView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;

    @Inject
    public MainTabVnicProfileView(MainModelProvider<VnicProfileView, VnicProfileListModel> modelProvider,
            ApplicationConstants constants,
            ApplicationResources resources) {
        super(modelProvider);
        this.constants = constants;
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<VnicProfileView> nameColumn = new TextColumnWithTooltip<VnicProfileView>() {
            @Override
            public String getValue(VnicProfileView object) {
                return object.getName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameVnicProfile(), "200px"); //$NON-NLS-1$

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

        TextColumnWithTooltip<VnicProfileView> descriptionColumn = new TextColumnWithTooltip<VnicProfileView>() {
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
