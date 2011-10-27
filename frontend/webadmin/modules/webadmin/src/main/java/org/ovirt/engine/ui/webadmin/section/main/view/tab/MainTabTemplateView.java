package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import java.util.Date;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.core.common.businessentities.VmTemplateStatus;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.webadmin.widget.table.UiCommandButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.GeneralDateTimeColumn;

import com.google.gwt.user.cellview.client.TextColumn;
import com.google.inject.Inject;

public class MainTabTemplateView extends AbstractMainTabWithDetailsTableView<VmTemplate, TemplateListModel> implements MainTabTemplatePresenter.ViewDef {

    @Inject
    public MainTabTemplateView(MainModelProvider<VmTemplate, TemplateListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        TextColumn<VmTemplate> nameColumn = new TextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getname();
            }
        };
        getTable().addColumn(nameColumn, "Name");

        TextColumn<VmTemplate> domainColumn = new TextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getdomain();
            }
        };
        getTable().addColumn(domainColumn, "Domain");

        TextColumn<VmTemplate> creationDateColumn = new GeneralDateTimeColumn<VmTemplate>() {
            @Override
            protected Date getRawValue(VmTemplate object) {
                return object.getcreation_date();
            }
        };
        getTable().addColumn(creationDateColumn, "Creation Date");

        TextColumn<VmTemplate> statusColumn = new EnumColumn<VmTemplate, VmTemplateStatus>() {
            @Override
            protected VmTemplateStatus getRawValue(VmTemplate object) {
                return object.getstatus();
            }
        };
        getTable().addColumn(statusColumn, "Status");

        TextColumn<VmTemplate> clusterColumn = new TextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getvds_group_name();
            }
        };
        getTable().addColumn(clusterColumn, "Cluster");

        TextColumn<VmTemplate> descriptionColumn = new TextColumn<VmTemplate>() {
            @Override
            public String getValue(VmTemplate object) {
                return object.getdescription();
            }
        };
        getTable().addColumn(descriptionColumn, "Description");

        getTable().addActionButton(new UiCommandButtonDefinition<VmTemplate>(getMainModel().getEditCommand()));
        getTable().addActionButton(new UiCommandButtonDefinition<VmTemplate>(getMainModel().getRemoveCommand()));
        // TODO: separator
        getTable().addActionButton(new UiCommandButtonDefinition<VmTemplate>(getMainModel().getExportCommand(),false,false));
        getTable().addActionButton(new UiCommandButtonDefinition<VmTemplate>(getMainModel().getCopyCommand(),false,false));
    }

}
