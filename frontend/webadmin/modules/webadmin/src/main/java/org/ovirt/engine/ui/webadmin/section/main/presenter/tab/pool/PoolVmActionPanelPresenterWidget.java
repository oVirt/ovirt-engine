package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class PoolVmActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VmPool, VM, PoolListModel, PoolVmListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public PoolVmActionPanelPresenterWidget(EventBus eventBus,
           DetailActionPanelPresenterWidget.ViewDef<VmPool, VM> view,
           SearchableDetailModelProvider<VM, PoolListModel, PoolVmListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<VmPool, VM>(constants.detachVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDetachCommand();
            }
        });
    }

}
