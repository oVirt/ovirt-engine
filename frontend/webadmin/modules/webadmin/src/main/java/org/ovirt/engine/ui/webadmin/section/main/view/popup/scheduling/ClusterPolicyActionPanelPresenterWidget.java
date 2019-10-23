package org.ovirt.engine.ui.webadmin.section.main.view.popup.scheduling;

import javax.inject.Inject;

import org.ovirt.engine.core.common.scheduling.ClusterPolicy;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.configure.scheduling.ClusterPolicyListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyModelProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class ClusterPolicyActionPanelPresenterWidget extends ActionPanelPresenterWidget<Object, ClusterPolicy, ClusterPolicyListModel> {
    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public ClusterPolicyActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Object, ClusterPolicy> view,
            ClusterPolicyModelProvider dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Object, ClusterPolicy>(constants.newClusterPolicy()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Object, ClusterPolicy>(constants.editClusterPolicy()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Object, ClusterPolicy>(constants.copyClusterPolicy()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCloneCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Object, ClusterPolicy>(constants.removeClusterPolicy()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Object, ClusterPolicy>(constants.managePolicyUnits()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getManagePolicyUnitCommand();
            }
        });
    }

}
