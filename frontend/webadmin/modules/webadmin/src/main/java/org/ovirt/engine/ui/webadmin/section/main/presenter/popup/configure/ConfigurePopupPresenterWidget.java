package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.configure;

import org.ovirt.engine.core.common.mode.ApplicationMode;
import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ApplicationModeHelper;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyClusterModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.ClusterPolicyModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.InstanceTypeModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RoleModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.RolePermissionModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SharedMacPoolModelProvider;
import org.ovirt.engine.ui.webadmin.uicommon.model.SystemPermissionModelProvider;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the WebAdmin configure dialog.
 */
public class ConfigurePopupPresenterWidget extends AbstractPopupPresenterWidget<ConfigurePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {
        void hideClusterPolicyTab();
    }

    private RoleModelProvider roleModelProvider;
    private RolePermissionModelProvider permissionModelProvider;
    private SystemPermissionModelProvider systemPermissionModelProvider;
    private ClusterPolicyModelProvider clusterPolicyModelProvider;
    private ClusterPolicyClusterModelProvider clusterPolicyClusterModelProvider;
    private InstanceTypeModelProvider instanceTypeModelProvider;
    private SharedMacPoolModelProvider sharedMacPoolModelProvider;

    @Inject
    public ConfigurePopupPresenterWidget(EventBus eventBus, ViewDef view,
            RoleModelProvider roleModelProvider,
            RolePermissionModelProvider permissionModelProvider,
            SystemPermissionModelProvider systemPermissionModelProvider,
            ClusterPolicyModelProvider clusterPolicyModelProvider,
            ClusterPolicyClusterModelProvider clusterPolicyClusterModelProvider,
            InstanceTypeModelProvider instanceTypeModelProvider,
            SharedMacPoolModelProvider sharedMacPoolModelProvider) {
        super(eventBus, view);
        this.roleModelProvider = roleModelProvider;
        this.permissionModelProvider = permissionModelProvider;
        this.systemPermissionModelProvider = systemPermissionModelProvider;
        this.clusterPolicyModelProvider = clusterPolicyModelProvider;
        this.clusterPolicyClusterModelProvider = clusterPolicyClusterModelProvider;
        this.instanceTypeModelProvider = instanceTypeModelProvider;
        this.sharedMacPoolModelProvider = sharedMacPoolModelProvider;
    }

    @Override
    protected void onReveal() {
        super.onReveal();
        roleModelProvider.getModel().search();
        systemPermissionModelProvider.refresh();
        systemPermissionModelProvider.getModel().search();
        if (ApplicationModeHelper.isModeSupported(ApplicationMode.VirtOnly)) {
            clusterPolicyModelProvider.getModel().search();
        } else {
            getView().hideClusterPolicyTab();
        }
        instanceTypeModelProvider.getModel().search();
        sharedMacPoolModelProvider.getModel().search();
    }

    @Override
    protected void onHide() {
        super.onHide();
        roleModelProvider.getModel().stopRefresh();
        permissionModelProvider.getModel().stopRefresh();
        systemPermissionModelProvider.getModel().stopRefresh();
        clusterPolicyModelProvider.getModel().stopRefresh();
        clusterPolicyClusterModelProvider.getModel().stopRefresh();
        instanceTypeModelProvider.getModel().stopRefresh();
        sharedMacPoolModelProvider.getModel().stopRefresh();
    }

}
