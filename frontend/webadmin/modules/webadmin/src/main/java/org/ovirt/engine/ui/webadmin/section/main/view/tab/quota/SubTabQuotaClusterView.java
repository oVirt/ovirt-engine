package org.ovirt.engine.ui.webadmin.section.main.view.tab.quota;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.common.businessentities.QuotaVdsGroup;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.table.column.TextColumnWithTooltip;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.quota.SubTabQuotaClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AbstractSubTabTableView;

import com.google.gwt.core.client.GWT;

public class SubTabQuotaClusterView extends AbstractSubTabTableView<Quota, QuotaVdsGroup, QuotaListModel, QuotaClusterListModel>
        implements SubTabQuotaClusterPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabQuotaClusterView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public SubTabQuotaClusterView(SearchableDetailModelProvider<QuotaVdsGroup, QuotaListModel, QuotaClusterListModel> modelProvider,
            ApplicationConstants constants, ApplicationMessages messages) {
        super(modelProvider);
        initTable(constants, messages);
        initWidget(getTable());
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    private void initTable(final ApplicationConstants constants, final ApplicationMessages messages) {
        getTable().enableColumnResizing();

        TextColumnWithTooltip<QuotaVdsGroup> nameColumn = new TextColumnWithTooltip<QuotaVdsGroup>() {
            @Override
            public String getValue(QuotaVdsGroup object) {
                return object.getVdsGroupName() == null || object.getVdsGroupName().equals("") ?
                        constants.ultQuotaForAllClustersQuotaPopup() : object.getVdsGroupName();
            }
        };
        nameColumn.makeSortable();
        getTable().addColumn(nameColumn, constants.nameCluster(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<QuotaVdsGroup> usedMemColumn = new TextColumnWithTooltip<QuotaVdsGroup>() {
            @Override
            public String getValue(QuotaVdsGroup object) {
                if (object.getMemSizeMB() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getMemSizeMB().equals(QuotaVdsGroup.UNLIMITED_MEM)) {
                    return messages.unlimitedMemConsumption(object.getMemSizeMBUsage());
                } else {
                    return messages.limitedMemConsumption(object.getMemSizeMBUsage(), object.getMemSizeMB());
                }
            }
        };
        usedMemColumn.makeSortable();
        getTable().addColumn(usedMemColumn, constants.usedMemoryTotalCluster(), "300px"); //$NON-NLS-1$

        TextColumnWithTooltip<QuotaVdsGroup> virtualCpuColumn = new TextColumnWithTooltip<QuotaVdsGroup>() {
            @Override
            public String getValue(QuotaVdsGroup object) {
                if (object.getVirtualCpu() == null) {
                    return ""; //$NON-NLS-1$
                } else if (object.getVirtualCpu().equals(QuotaVdsGroup.UNLIMITED_VCPU)) {
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
