package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.gluster;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterServerService;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.gluster.HostGlusterSwiftListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class HostGlusterSwiftActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VDS, GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostGlusterSwiftActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VDS, GlusterServerService> view,
            SearchableDetailModelProvider<GlusterServerService, HostListModel<Void>, HostGlusterSwiftListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<VDS, GlusterServerService>(constants.startGlusterSwiftInHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStartSwiftCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<VDS, GlusterServerService>(constants.stopGlusterSwiftInHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopSwiftCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<VDS, GlusterServerService>(constants.restartGlusterSwiftInHost()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRestartSwiftCommand();
            }
        });
    }

}
