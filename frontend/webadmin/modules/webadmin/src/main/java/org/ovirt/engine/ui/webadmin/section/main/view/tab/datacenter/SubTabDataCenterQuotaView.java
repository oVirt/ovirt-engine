package org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterListModel;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.DataCenterQuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterQuotaPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

public class SubTabDataCenterQuotaView extends AbstractSubTabTableView<storage_pool, Quota, DataCenterListModel, DataCenterQuotaListModel>
        implements SubTabDataCenterQuotaPresenter.ViewDef {

    @Inject
    public SubTabDataCenterQuotaView(SearchableDetailModelProvider<Quota, DataCenterListModel, DataCenterQuotaListModel> modelProvider, ApplicationConstants contants) {
        super(modelProvider);
        initTable(contants);
        initWidget(getTable());
    }

    void initTable(ApplicationConstants constants) {
        getTable().addColumn(new QuotaDcStatusColumn(), constants.empty(), "30px"); //$NON-NLS-1$

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName(); //$NON-NLS-1$
            }
        }, constants.nameQuota());

        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        }, constants.descriptionQuota());
//        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
//            @Override
//            public String getValue(Quota object) {
//                return object.getStoragePoolName() == null ? "" : object.getStoragePoolName(); //$NON-NLS-1$
//            }
//        }, constants.dcQuota());
//        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
//            @Override
//            public String getValue(Quota object) {
//                return (object.getMemSizeMBUsage() == null ? "0" : object.getMemSizeMBUsage().toString()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
//                        + (object.getMemSizeMB() == null ? "*" : object.getMemSizeMB().toString()) + " GB"; //$NON-NLS-1$ //$NON-NLS-2$
//            }
//        },
//                constants.usedMemoryQuota());
//        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
//            @Override
//            public String getValue(Quota object) {
//                return (object.getVirtualCpuUsage() == null ? "0" : object.getVirtualCpuUsage().toString()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
//                        + (object.getVirtualCpu() == null ? "*" : object.getVirtualCpu().toString()); //$NON-NLS-1$
//            }
//        },
//                constants.runningCpuQuota());
//        getTable().addColumn(new TextColumnWithTooltip<Quota>() {
//            @Override
//            public String getValue(Quota object) {
//                return (object.getStorageSizeGBUsage() == null ? "0" : object.getStorageSizeGBUsage().toString()) + "/" //$NON-NLS-1$ //$NON-NLS-2$
//                        + (object.getStorageSizeGB() == null ? "*" : object.getStorageSizeGB().toString()) + " GB"; //$NON-NLS-1$ //$NON-NLS-2$
//            }
//        },
//                constants.usedStorageQuota());
    }

}
