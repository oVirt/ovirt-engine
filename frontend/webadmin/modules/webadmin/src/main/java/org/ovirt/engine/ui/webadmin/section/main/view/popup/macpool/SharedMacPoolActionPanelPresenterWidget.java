package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SharedMacPoolModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class SharedMacPoolActionPanelPresenterWidget
    extends ActionPanelPresenterWidget<Void, MacPool, SharedMacPoolListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SharedMacPoolActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, MacPool> view,
            SharedMacPoolModelProvider dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Void, MacPool>(constants.configureMacPoolAddButton()) {

            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, MacPool>(constants.configureMacPoolEditButton()) {

            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<Void, MacPool>(constants.configureMacPoolRemoveButton()) {

            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

}
