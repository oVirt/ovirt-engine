package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaCluster;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.AbstractTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaClusterView extends AbstractSubTabTableView<Quota, QuotaCluster, QuotaListModel, QuotaClusterListModel>
        implements SubTabQuotaClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationMessages messages = AssetProvider.getMessages();

    @Inject
    public SubTabQuotaClusterView(SearchableDetailModelProvider<QuotaCluster, QuotaListModel, QuotaClusterListModel> modelProvider) {
        super(modelProvider);
        initTable();
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable() {
        getTable().enableColumnResizing();

        AbstractTextColumn<QuotaCluster> nameColumn = new AbstractTextColumn<QuotaCluster>() {
            @Override
            public String getValue(QuotaCluster object) {
                return object.getClusterName() == null || object.getClusterName().equals("") ?
                        constants.ultQuotaForAllClustersQuotaPopup() : object.getClusterName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameCluster(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<QuotaCluster> usedMemColumn = new AbstractTextColumn<QuotaCluster>() {
            @Override
            public String getValue(QuotaCluster object) {
                if (object.getMemSizeMB() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getMemSizeMB().equals(QuotaCluster.UNLIMITED_MEM)) {
                    return messages.unlimitedMemConsumption(object.getMemSizeMBUsage());
                } else {
                    return messages.limitedMemConsumption(object.getMemSizeMBUsage(), object.getMemSizeMB());
                }
            }
        };
        usedMemColumn.makeSortable();
        getTable().addColumn(usedMemColumn, constants.usedMemoryTotalCluster(), "300px"); //$NON-NLS-1$

        AbstractTextColumn<QuotaCluster> virtualCpuColumn = new AbstractTextColumn<QuotaCluster>() {
            @Override
            public String getValue(QuotaCluster object) {
                if (object.getVirtualCpu() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getVirtualCpu().equals(QuotaCluster.UNLIMITED_VCPU)) {
                    return messages.unlimitedVcpuConsumption(object.getVirtualCpuUsage());
                } else {
                    return messages.limitedVcpuConsumption(object.getVirtualCpuUsage(), object.getVirtualCpu());
                }
            }
        };
        virtualCpuColumn.makeSortable();
        getTable().addColumn(virtualCpuColumn, constants.runningCpuTotalCluster(), "300px"); //$NON-NLS-1$
    }

}
