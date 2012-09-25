package org.ovirt.engine.ui.webadmin.section.main.view.tab;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
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
    public MainTabQuotaView(MainModelProvider<Quota, QuotaListModel> modelProvider, ApplicationConstants constants) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable(constants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().enableColumnResizing();

        getTable().addColumn(new QuotaDcStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName(); //$NON-NLS-1$
            }
        }, constants.nameQuota(), "150px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        }, constants.descriptionQuota(), "300px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getStoragePoolName() == null ? "" : object.getStoragePoolName(); //$NON-NLS-1$
            }
        }, constants.dcQuota(), "150px"); //$NON-NLS-1$

        //        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
        //            @Override
        //            public String getValue(Quota object) {
        //                return (object.getMemSizeMBUsage() == null ? "0" : object.getMemSizeMBUsage().toString()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
        //                        + (object.getMemSizeMB() == null ? "*" : object.getMemSizeMB().toString()) + " GB"; //$NON-NLS-1$ //$NON-NLS-2$
        //            }
        //        },
        //        constants.usedMemoryQuota());
        //        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
        //            @Override
        //            public String getValue(Quota object) {
        //                return (object.getVirtualCpuUsage() == null ? "0" : object.getVirtualCpuUsage().toString()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
        //                        + (object.getVirtualCpu() == null ? "*" : object.getVirtualCpu().toString()); //$NON-NLS-1$
        //            }
        //        },
        //        constants.runningCpuQuota());
        //        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
        //            @Override
        //            public String getValue(Quota object) {
        //                return (object.getStorageSizeGBUsage() == null ? "0" : object.getStorageSizeGBUsage().toString()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
        //                        + (object.getStorageSizeGB() == null ? "*" : object.getStorageSizeGB().toString()) + " GB"; //$NON-NLS-1$ //$NON-NLS-2$
        //            }
        //        },
        //        constants.usedStorageQuota());

        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.addQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCreateQuotaCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.editQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getEditQuotaCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.copyQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getCloneQuotaCommand();
            }
        });
        getTable().addActionButton(new WebAdminButtonDefinition<Quota>(constants.removeQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getMainModel().getRemoveQuotaCommand();
            }
        });

    }
}
