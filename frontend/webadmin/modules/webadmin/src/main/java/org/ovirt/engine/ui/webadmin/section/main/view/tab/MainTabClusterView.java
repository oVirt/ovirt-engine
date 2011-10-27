package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabClusterView extends AbstractMainTabWithDetailsTableView<VDSGroup, ClusterListModel> implements MainTabClusterPresenter.ViewDef {

    @Inject
    public MainTabClusterView(MainModelProvider<VDSGroup, ClusterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<VDSGroup> nameColumn = new TextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<VDSGroup> versionColumn = new TextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getcompatibility_version().getValue();
            }
        };
        getTable().addColumn(versionColumn, "Compatiblity Version");

        TextColumn<VDSGroup> descColumn = new TextColumn<VDSGroup>() {
            @Override
            public String getValue(VDSGroup object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<VDSGroup>(getMainModel().getNewCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDSGroup>(getMainModel().getEditCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VDSGroup>(getMainModel().getRemoveCommand()));
    }

}
