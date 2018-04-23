package org.ovirt.engine.ui.webadmin.section.main.view;

import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.core.common.businessentities.QuotaStorage;
import org.ovirt.engine.core.common.utils.SizeConverter;
import org.ovirt.engine.core.searchbackend.QuotaConditionFieldAutoCompleter;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.FragmentParams;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.renderer.DiskSizeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractLinkColumn;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainQuotaPresenter;
import org.ovirt.engine.ui.webadmin.widget.table.column.AbstractQuotaPercentColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.QuotaDcStatusColumn;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.i18n.client.NumberFormat;
import com.google.inject.Inject;

public class MainQuotaView extends AbstractMainWithDetailsTableView<Quota, QuotaListModel> implements MainQuotaPresenter.ViewDef {

    private static final NumberFormat decimalFormat = NumberFormat.getDecimalFormat();
    private static final DiskSizeRenderer<Number> diskSizeRenderer =
            new DiskSizeRenderer<>(SizeConverter.SizeUnit.GiB);

    interface ViewIdHandler extends ElementIdHandler<MainQuotaView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public MainQuotaView(MainModelProvider<Quota, QuotaListModel> modelProvider) {
        super(modelProvider);
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTable();
        initWidget(getTable());
    }

    void initTable() {
        getTable().enableColumnResizing();

        QuotaDcStatusColumn dcStatusColumn = new QuotaDcStatusColumn();
        dcStatusColumn.setContextMenuTitle(constants.dcStatusQuota());
        getTable().addColumn(dcStatusColumn, constants.empty(), "30px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> nameColumn = new AbstractLinkColumn<Quota>(new FieldUpdater<Quota, String>() {

            @Override
            public void update(int index, Quota quota, String value) {
                Map<String, String> parameters = new HashMap<>();
                parameters.put(FragmentParams.NAME.getName(), quota.getName());
                parameters.put(FragmentParams.DATACENTER.getName(), quota.getStoragePoolName());
                //The link was clicked, now fire an event to switch to details.
                getPlaceTransitionHandler().handlePlaceTransition(
                        WebAdminApplicationPlaces.quotaClusterSubTabPlace, parameters);
            }

        }) {
            @Override
            public String getValue(Quota object) {
                return object.getQuotaName() == null ? "" : object.getQuotaName(); //$NON-NLS-1$
            }
        };
        nameColumn.makeSortable(QuotaConditionFieldAutoCompleter.NAME);
        getTable().addColumn(nameColumn, constants.nameQuota(), "120px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> dataCenterColumn = new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getStoragePoolName() == null ? "" : object.getStoragePoolName();
            }
        };
        dataCenterColumn.makeSortable(QuotaConditionFieldAutoCompleter.STORAGEPOOLNAME);
        getTable().addColumn(dataCenterColumn, constants.dcQuota(), "120px"); //$NON-NLS-1$

        AbstractTextColumn<Quota> descriptionColumn = new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                return object.getDescription() == null ? "" : object.getDescription(); //$NON-NLS-1$
            }
        };
        descriptionColumn.makeSortable(QuotaConditionFieldAutoCompleter.DESCRIPTION);
        getTable().addColumn(descriptionColumn, constants.descriptionQuota(), "120px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractQuotaPercentColumn<Quota>() {
            @Override
            protected Integer getProgressValue(Quota object) {
                int value;
                long allocated = 0;
                long used = 0;
                if (object.getGlobalQuotaCluster() != null) {
                    allocated = object.getGlobalQuotaCluster().getMemSizeMB();
                    used = object.getGlobalQuotaCluster().getMemSizeMBUsage();
                } else {
                    for (QuotaCluster quotaCluster : object.getQuotaClusters()) {
                        if (!QuotaCluster.UNLIMITED_MEM.equals(quotaCluster.getMemSizeMB())) {
                            allocated += quotaCluster.getMemSizeMB() != null ? quotaCluster.getMemSizeMB() : 0;
                            used += quotaCluster.getMemSizeMBUsage();
                        } else {
                            allocated = QuotaCluster.UNLIMITED_MEM;
                            break;
                        }
                    }
                }
                if (allocated == 0) {
                    return 0;
                }
                value = (int)(((double)used/allocated) * 100);
                return allocated < 0 ? -1 : value > 100 ? 100 : value;
            }
        },
        constants.usedMemoryQuota(), "145px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                int value;
                long allocated = 0;
                long used = 0;
                if (object.getGlobalQuotaCluster() != null) {
                    allocated = object.getGlobalQuotaCluster().getMemSizeMB();
                    used = object.getGlobalQuotaCluster().getMemSizeMBUsage();
                } else {
                    for (QuotaCluster quotaCluster : object.getQuotaClusters()) {
                        if (!QuotaCluster.UNLIMITED_MEM.equals(quotaCluster.getMemSizeMB())) {
                            allocated += quotaCluster.getMemSizeMB() != null ? quotaCluster.getMemSizeMB() : 0;
                            used += quotaCluster.getMemSizeMBUsage();
                        } else {
                            allocated = QuotaCluster.UNLIMITED_MEM;
                            break;
                        }
                    }
                }
                value = (int)(allocated-used);
                String returnVal;
                if (allocated < 0) {
                    returnVal = constants.unlimited();
                } else if (value <= 0){
                    returnVal = messages.mebibytes("0"); //$NON-NLS-1$
                } else if (value <= 5*1024) {
                    returnVal = messages.mebibytes(String.valueOf(value));
                } else {
                    returnVal = messages.gibibytes(decimalFormat.format((double)value/1024));
                }
                return returnVal;
            }
        }, constants.freeMemory(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractQuotaPercentColumn<Quota>() {
            @Override
            protected Integer getProgressValue(Quota object) {
                int value;
                int allocated = 0;
                int used = 0;
                if (object.getGlobalQuotaCluster() != null) {
                    allocated =
                            object.getGlobalQuotaCluster().getVirtualCpu() != null ? object.getGlobalQuotaCluster()
                                    .getVirtualCpu() : 0;
                    used = object.getGlobalQuotaCluster().getVirtualCpuUsage();
                } else {
                    for (QuotaCluster quotaCluster : object.getQuotaClusters()) {
                        if (!QuotaCluster.UNLIMITED_VCPU.equals(quotaCluster.getVirtualCpu())) {
                            allocated += quotaCluster.getVirtualCpu();
                            used += quotaCluster.getVirtualCpuUsage();
                        } else {
                            allocated = QuotaCluster.UNLIMITED_VCPU;
                            break;
                        }
                    }
                }
                if (allocated == 0) {
                    return 0;
                }
                value = (int)(((double)used/allocated) * 100);
                return allocated < 0 ? -1 : value > 100 ? 100 : value;
            }
        },
        constants.runningCpuQuota(), "135px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                int value;
                int allocated = 0;
                int used = 0;
                if (object.getGlobalQuotaCluster() != null) {
                    allocated =
                            object.getGlobalQuotaCluster().getVirtualCpu() != null ? object.getGlobalQuotaCluster()
                                    .getVirtualCpu() : 0;
                    used = object.getGlobalQuotaCluster().getVirtualCpuUsage();
                } else {
                    for (QuotaCluster quotaCluster : object.getQuotaClusters()) {
                        if (!QuotaCluster.UNLIMITED_VCPU.equals(quotaCluster.getVirtualCpu())) {
                            allocated += quotaCluster.getVirtualCpu();
                            used += quotaCluster.getVirtualCpuUsage();
                        } else {
                            allocated = QuotaCluster.UNLIMITED_VCPU;
                            break;
                        }
                    }
                }
                value = allocated - used;

                String returnVal;
                if (allocated < 0) {
                    returnVal = constants.unlimited();
                } else if (value <= 0) {
                    returnVal = "0"; //$NON-NLS-1$
                } else {
                    returnVal = value + ""; //$NON-NLS-1$
                }
                return returnVal;
            }
        }, constants.freeVcpu(), "100px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractQuotaPercentColumn<Quota>() {
            @Override
            protected Integer getProgressValue(Quota object) {
                int value;
                double allocated = 0;
                double used = 0;
                if (object.getGlobalQuotaStorage() != null) {
                    allocated = object.getGlobalQuotaStorage().getStorageSizeGB();
                    used = object.getGlobalQuotaStorage().getStorageSizeGBUsage();
                } else {
                    for (QuotaStorage quotaStorage : object.getQuotaStorages()) {
                        if (!QuotaStorage.UNLIMITED.equals(quotaStorage.getStorageSizeGB())) {
                            allocated += quotaStorage.getStorageSizeGB();
                            used += quotaStorage.getStorageSizeGBUsage();
                        } else {
                            allocated = QuotaStorage.UNLIMITED;
                            break;
                        }
                    }
                }
                if (allocated == 0) {
                    return 0;
                }
                value = (int)((used/allocated) * 100);
                return allocated < 0 ? -1 : value > 100 ? 100 : value;
            }
        },
        constants.usedStorageQuota(), "155px"); //$NON-NLS-1$

        getTable().addColumn(new AbstractTextColumn<Quota>() {
            @Override
            public String getValue(Quota object) {
                double value;
                double allocated = 0;
                double used = 0;
                if (object.getGlobalQuotaStorage() != null) {
                    allocated = object.getGlobalQuotaStorage().getStorageSizeGB();
                    used = object.getGlobalQuotaStorage().getStorageSizeGBUsage();
                } else {
                    for (QuotaStorage quotaStorage : object.getQuotaStorages()) {
                        if (!QuotaStorage.UNLIMITED.equals(quotaStorage.getStorageSizeGB())) {
                            allocated += quotaStorage.getStorageSizeGB();
                            used += quotaStorage.getStorageSizeGBUsage();
                        } else {
                            allocated = QuotaStorage.UNLIMITED;
                            break;
                        }
                    }
                }
                value = allocated - used;

                String returnVal;
                if (allocated < 0) {
                    returnVal = constants.unlimited();
                } else if (value <= 0) {
                    returnVal = messages.gibibytes("0"); //$NON-NLS-1$
                } else {
                    returnVal = diskSizeRenderer.render(value);
                }
                return returnVal;
            }
        }, constants.freeStorage(), "100px"); //$NON-NLS-1$
    }
}
