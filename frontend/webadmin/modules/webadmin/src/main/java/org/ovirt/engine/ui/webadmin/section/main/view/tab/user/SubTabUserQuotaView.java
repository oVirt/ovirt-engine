package org.ovirt.engine.ui.webadmin.section.main.view.tab.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.users.UserListModel;
import org.ovirt.engine.ui.uicommonweb.models.users.UserQuotaListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

public class SubTabUserQuotaView extends AbstractSubTabTableView<DbUser, Quota, UserListModel, UserQuotaListModel>
        implements SubTabUserQuotaPresenter.ViewDef {

    @Inject
    public SubTabUserQuotaView(SearchableDetailModelProvider<Quota, UserListModel, UserQuotaListModel> modelProvider) {
        super(modelProvider);
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
                return (object.getGlobalQuotaVdsGroup().getMemSizeMBUsage() == null ? "0" : object.getGlobalQuotaVdsGroup().getMemSizeMBUsage().toString()) + "/"
                        + (object.getGlobalQuotaVdsGroup().getMemSizeMB() == null ? "*" : object.getGlobalQuotaVdsGroup().getMemSizeMB().toString()) + " GB";
            }
        },
                "Used Memory/Total");
        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return (object.getGlobalQuotaVdsGroup().getVirtualCpuUsage() == null ? "0" : object.getGlobalQuotaVdsGroup().getVirtualCpuUsage().toString()) + "/"
                        + (object.getGlobalQuotaVdsGroup().getVirtualCpu() == null ? "*" : object.getGlobalQuotaVdsGroup().getVirtualCpu().toString());
            }
        },
                "Running CPU/Total");
        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return (object.getGlobalQuotaStorage().getStorageSizeGBUsage() == null ? "0" : object.getGlobalQuotaStorage().getStorageSizeGBUsage().toString()) + "/"
                        + (object.getGlobalQuotaStorage().getStorageSizeGB() == null ? "*" : object.getGlobalQuotaStorage().getStorageSizeGB().toString()) + " GB";
            }
        },
                "Used Storage/Total");
    }

}
