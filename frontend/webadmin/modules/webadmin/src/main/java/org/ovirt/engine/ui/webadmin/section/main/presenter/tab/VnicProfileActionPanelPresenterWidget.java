package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.network.VnicProfileView;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.profiles.VnicProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class VnicProfileActionPanelPresenterWidget extends
    ActionPanelPresenterWidget<VnicProfileView, VnicProfileView, VnicProfileListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<VnicProfileView, VnicProfileView> newButtonDefinition;

    @Inject
    public VnicProfileActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<VnicProfileView, VnicProfileView> view,
            MainModelProvider<VnicProfileView, VnicProfileListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<VnicProfileView, VnicProfileView>(constants.newVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);
        addActionButton(new WebAdminButtonDefinition<VnicProfileView, VnicProfileView>(constants.editVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<VnicProfileView, VnicProfileView>(constants.removeVnicProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }

    public WebAdminButtonDefinition<VnicProfileView, VnicProfileView> getNewButtonDefinition() {
        return newButtonDefinition;
    }

}
