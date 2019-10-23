package org.ovirt.engine.ui.webadmin.section.main.view.popup.instancetypes;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.InstanceType;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.instancetypes.InstanceTypeListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class InstanceTypesActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, InstanceType, InstanceTypeListModel> {
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public InstanceTypesActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, InstanceType> view,
            InstanceTypeModelProvider dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Void, InstanceType>(constants.newInstanceType()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewInstanceTypeCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, InstanceType>(constants.editInstanceType()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditInstanceTypeCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, InstanceType>(constants.removeInstanceType()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getDeleteInstanceTypeCommand();
            }
        });
    }

}
