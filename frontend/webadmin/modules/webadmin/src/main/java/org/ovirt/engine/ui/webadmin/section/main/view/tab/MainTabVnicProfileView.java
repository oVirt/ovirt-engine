package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.network.VnicProfile;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
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

public class MainTabVnicProfileView extends AbstractMainTabWithDetailsTableView<VnicProfile, VnicProfileListModel> implements MainTabVnicProfilePresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabVnicProfileView> {

        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final ApplicationConstants constants;

    @Inject
    public MainTabVnicProfileView(MainModelProvider<VnicProfile, VnicProfileListModel> modelProvider,
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

        TextColumnWithTooltip<VnicProfile> nameColumn = new TextColumnWithTooltip<VnicProfile>() {
            @Override
            public String getValue(VnicProfile object) {
                return object.getName();
            }
        };

        getTable().addColumn(nameColumn, constants.nameNetworkProfile(), "200px"); //$NON-NLS-1$

        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfile>(constants.newVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getNewCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfile>(constants.editVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<VnicProfile>(constants.removeVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveCommand();
            }
        });

    }
}
