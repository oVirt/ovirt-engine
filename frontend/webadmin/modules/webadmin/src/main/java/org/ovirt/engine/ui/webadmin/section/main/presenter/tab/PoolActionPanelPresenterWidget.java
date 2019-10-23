package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmPool;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.pools.PoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class PoolActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, VmPool, PoolListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, VmPool> newButtonDefinition;

    @Inject
    public PoolActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, VmPool> view,
            MainModelProvider<VmPool, PoolListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, VmPool>(constants.newPool()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<Void, VmPool>(constants.editPool()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, VmPool>(constants.removePool()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, VmPool> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
