package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractMainTabWithDetailsTableView;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

import com.google.gwt.core.client.GWT;
import com.google.inject.Inject;

public class MainTabQuotaView extends AbstractMainTabWithDetailsTableView<Quota, QuotaListModel> implements MainTabQuotaPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<MainTabQuotaView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public MainTabQuotaView(MainModelProvider<Quota, QuotaListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().addColumn(new QuotaDcStatusColumn(), "", "30px");

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName();
            }
        }, "Name");

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription();
            }
        }, "Description");
        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getStoragePoolName() == null ? "" : object.getStoragePoolName();
            }
        }, "Data Center");
        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return (object.getMemSizeMBUsage() == null ? "0" : object.getMemSizeMBUsage().toString()) + "/"
                        + (object.getMemSizeMB() == null ? "*" : object.getMemSizeMB().toString()) + " GB";
            }
        },
                "Used Memory/Total");
        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return (object.getVirtualCpuUsage() == null ? "0" : object.getVirtualCpuUsage().toString()) + "/"
                        + (object.getVirtualCpu() == null ? "*" : object.getVirtualCpu().toString());
            }
        },
                "Running CPU/Total");
        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return (object.getStorageSizeGBUsage() == null ? "0" : object.getStorageSizeGBUsage().toString()) + "/"
                        + (object.getStorageSizeGB() == null ? "*" : object.getStorageSizeGB().toString()) + " GB";
            }
        },
                "Used Storage/Total");

        getTable().addActionButton(new WebAdminButtonDefinition<Quota>("Add") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCreateQuotaCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>("Edit") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditQuotaCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>("Remove") {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveQuotaCommand();
            }
        });

    }
}
