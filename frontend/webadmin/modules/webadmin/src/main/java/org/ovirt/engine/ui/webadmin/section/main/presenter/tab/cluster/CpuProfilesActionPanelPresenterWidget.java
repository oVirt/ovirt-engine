package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.profiles.CpuProfile;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.clusters.ClusterListModel;
import org.ovirt.engine.ui.uicommonweb.models.profiles.CpuProfileListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class CpuProfilesActionPanelPresenterWidget extends ActionPanelPresenterWidget<CpuProfile, CpuProfileListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public CpuProfilesActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<CpuProfile> view,
            SearchableDetailModelProvider<CpuProfile, ClusterListModel<Void>, CpuProfileListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<CpuProfile>(constants.newProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<CpuProfile>(constants.editProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });
        addActionButton(new WebAdminButtonDefinition<CpuProfile>(constants.removeProfile()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });
    }
}
