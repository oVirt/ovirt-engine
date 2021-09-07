package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host;

import javax.inject.Inject;

import org.gwtbootstrap3.client.ui.constants.IconType;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VM;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostListModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.HostVmListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminImageButtonDefinition;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.web.bindery.event.shared.EventBus;

public class HostVmActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<VDS, VM, HostListModel<Void>, HostVmListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public HostVmActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<VDS, VM> view,
            SearchableDetailModelProvider<VM, HostListModel<Void>, HostVmListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminImageButtonDefinition<VDS, VM>(constants.runVm(),
                IconType.PLAY) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRunCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.runVm());
            }
        });

        addActionButton(new WebAdminImageButtonDefinition<VDS, VM>(constants.suspendVm(),
                IconType.MOON_O) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getSuspendCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.suspendVm());
            }
        });

        addActionButton(new WebAdminImageButtonDefinition<VDS, VM>(constants.shutDownVm(),
                IconType.STOP) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getShutdownCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.shutDownVm());
            }
        });

        addActionButton(new WebAdminImageButtonDefinition<VDS, VM>(constants.powerOffVm(),
                IconType.POWER_OFF) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getStopCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.powerOffVm());
            }
        });

        addActionButton(new WebAdminImageButtonDefinition<VDS, VM>(constants.consoleVm(),
                IconType.DESKTOP) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getConsoleConnectCommand();
            }

            @Override
            public SafeHtml getTooltip() {
                return SafeHtmlUtils.fromSafeConstant(constants.consoleVm());
            }

        });

        addActionButton(new WebAdminButtonDefinition<VDS, VM>(constants.cancelMigrationVm()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCancelMigrateCommand();
            }
        });
    }

}
