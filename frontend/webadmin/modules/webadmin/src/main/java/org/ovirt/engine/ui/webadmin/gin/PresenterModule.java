package org.ovirt.engine.ui.webadmin.gin;

import org.ovirt.engine.ui.webadmin.presenter.ErrorPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.login.presenter.LoginSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.login.view.LoginPopupView;
import org.ovirt.engine.ui.webadmin.section.login.view.LoginSectionView;
import org.ovirt.engine.ui.webadmin.section.main.presenter.AboutPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainContentPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.SearchPanelPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DefaultConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.DetachConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.RemoveConfirmationPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.bookmark.BookmarkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterNewNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPolicyPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.ClusterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterNetworkPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.DataCenterPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostBondPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInstallPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostManagementPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.host.HostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindMultiDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindSingleDcPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StoragePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.StorageRemovePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.template.TemplateNewPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmAssignTagsPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDesktopNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmServerNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabPoolPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabUserPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.MainTabVirtualMachinePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.ClusterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterHostPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.cluster.SubTabClusterVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.DataCenterSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterClusterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterNetworkPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.datacenter.SubTabDataCenterStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.HostSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostHookPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.host.SubTabHostVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.PoolSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.pool.SubTabPoolVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.StorageSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageDataCenterPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageIsoPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStoragePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplateBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageTemplatePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmBackupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage.SubTabStorageVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplatePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateStoragePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.SubTabTemplateVmPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.template.TemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventNotifierPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserGroupPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.SubTabUserPermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.user.UserSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineApplicationPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineEventPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineGeneralPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineNetworkInterfacePresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachinePermissionPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineSnapshotPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.SubTabVirtualMachineVirtualDiskPresenter;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.virtualMachine.VirtualMachineSubTabPanelPresenter;
import org.ovirt.engine.ui.webadmin.section.main.view.AboutPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.HeaderView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainContentView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainSectionView;
import org.ovirt.engine.ui.webadmin.section.main.view.MainTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.SearchPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.DefaultConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.DetachConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.PermissionsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.RemoveConfirmationPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.bookmark.BookmarkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ClusterNewNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ClusterPolicyPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster.ClusterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.DataCenterNetworkPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.DataCenterPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.FindMultiStoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter.FindSingleStoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostBondPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostInstallPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostManagementPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.host.HostPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.FindMultiDcPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.FindSingleDcPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StoragePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.storage.StorageRemovePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.tag.TagPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.TemplateInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.template.TemplateNewPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmAssignTagsPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmChangeCDPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDesktopNewPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmDiskPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmInterfacePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmMakeTemplatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmRunOncePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmServerNewPopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.vm.VmSnapshotCreatePopupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabDataCenterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabPoolView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabUserView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.MainTabVirtualMachineView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.ClusterSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterHostView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.cluster.SubTabClusterVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.DataCenterSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterClusterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterNetworkView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.datacenter.SubTabDataCenterStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.HostSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostHookView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostInterfaceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.host.SubTabHostVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.PoolSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.pool.SubTabPoolVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.StorageSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageDataCenterView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageIsoView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStoragePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageTemplateBackupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageTemplateView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageVmBackupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.storage.SubTabStorageVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateInterfaceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplatePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateStorageView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.SubTabTemplateVmView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.template.TemplateSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserEventNotifierView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserGroupView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.SubTabUserPermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.user.UserSubTabPanelView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineApplicationView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineEventView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineGeneralView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineNetworkInterfaceView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachinePermissionView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineSnapshotView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.SubTabVirtualMachineVirtualDiskView;
import org.ovirt.engine.ui.webadmin.section.main.view.tab.virtualMachine.VirtualMachineSubTabPanelView;
import org.ovirt.engine.ui.webadmin.view.ErrorPopupView;

import com.gwtplatform.mvp.client.gin.AbstractPresenterModule;

/**
 * GIN module containing GWTP presenter bindings.
 */
public class PresenterModule extends AbstractPresenterModule {

    @Override
    protected void configure() {
        // Common stuff
        bindSingletonPresenterWidget(ErrorPopupPresenterWidget.class,
                ErrorPopupPresenterWidget.ViewDef.class,
                ErrorPopupView.class);

        // Login section
        bindPresenter(LoginSectionPresenter.class,
                LoginSectionPresenter.ViewDef.class,
                LoginSectionView.class,
                LoginSectionPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(LoginPopupPresenterWidget.class,
                LoginPopupPresenterWidget.ViewDef.class,
                LoginPopupView.class);

        // Main section: common stuff
        bindPresenter(MainSectionPresenter.class,
                MainSectionPresenter.ViewDef.class,
                MainSectionView.class,
                MainSectionPresenter.ProxyDef.class);
        bindPresenter(MainContentPresenter.class,
                MainContentPresenter.ViewDef.class,
                MainContentView.class,
                MainContentPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(HeaderPresenterWidget.class,
                HeaderPresenterWidget.ViewDef.class,
                HeaderView.class);
        bindSingletonPresenterWidget(SearchPanelPresenterWidget.class,
                SearchPanelPresenterWidget.ViewDef.class,
                SearchPanelView.class);
        bindSingletonPresenterWidget(AboutPopupPresenterWidget.class,
                AboutPopupPresenterWidget.ViewDef.class,
                AboutPopupView.class);

        // Main section: main tabs
        bindPresenter(MainTabPanelPresenter.class,
                MainTabPanelPresenter.ViewDef.class,
                MainTabPanelView.class,
                MainTabPanelPresenter.ProxyDef.class);
        bindPresenter(MainTabDataCenterPresenter.class,
                MainTabDataCenterPresenter.ViewDef.class,
                MainTabDataCenterView.class,
                MainTabDataCenterPresenter.ProxyDef.class);
        bindPresenter(MainTabClusterPresenter.class,
                MainTabClusterPresenter.ViewDef.class,
                MainTabClusterView.class,
                MainTabClusterPresenter.ProxyDef.class);
        bindPresenter(MainTabHostPresenter.class,
                MainTabHostPresenter.ViewDef.class,
                MainTabHostView.class,
                MainTabHostPresenter.ProxyDef.class);
        bindPresenter(MainTabStoragePresenter.class,
                MainTabStoragePresenter.ViewDef.class,
                MainTabStorageView.class,
                MainTabStoragePresenter.ProxyDef.class);
        bindPresenter(MainTabVirtualMachinePresenter.class,
                MainTabVirtualMachinePresenter.ViewDef.class,
                MainTabVirtualMachineView.class,
                MainTabVirtualMachinePresenter.ProxyDef.class);
        bindPresenter(MainTabPoolPresenter.class,
                MainTabPoolPresenter.ViewDef.class,
                MainTabPoolView.class,
                MainTabPoolPresenter.ProxyDef.class);
        bindPresenter(MainTabTemplatePresenter.class,
                MainTabTemplatePresenter.ViewDef.class,
                MainTabTemplateView.class,
                MainTabTemplatePresenter.ProxyDef.class);
        bindPresenter(MainTabUserPresenter.class,
                MainTabUserPresenter.ViewDef.class,
                MainTabUserView.class,
                MainTabUserPresenter.ProxyDef.class);
        bindPresenter(MainTabEventPresenter.class,
                MainTabEventPresenter.ViewDef.class,
                MainTabEventView.class,
                MainTabEventPresenter.ProxyDef.class);

        // Main section: sub tabs

        // DataCenter
        bindPresenter(DataCenterSubTabPanelPresenter.class,
                DataCenterSubTabPanelPresenter.ViewDef.class,
                DataCenterSubTabPanelView.class,
                DataCenterSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterStoragePresenter.class,
                SubTabDataCenterStoragePresenter.ViewDef.class,
                SubTabDataCenterStorageView.class,
                SubTabDataCenterStoragePresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterNetworkPresenter.class,
                SubTabDataCenterNetworkPresenter.ViewDef.class,
                SubTabDataCenterNetworkView.class,
                SubTabDataCenterNetworkPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterClusterPresenter.class,
                SubTabDataCenterClusterPresenter.ViewDef.class,
                SubTabDataCenterClusterView.class,
                SubTabDataCenterClusterPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterPermissionPresenter.class,
                SubTabDataCenterPermissionPresenter.ViewDef.class,
                SubTabDataCenterPermissionView.class,
                SubTabDataCenterPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabDataCenterEventPresenter.class,
                SubTabDataCenterEventPresenter.ViewDef.class,
                SubTabDataCenterEventView.class,
                SubTabDataCenterEventPresenter.ProxyDef.class);

        // Storage
        bindPresenter(StorageSubTabPanelPresenter.class,
                StorageSubTabPanelPresenter.ViewDef.class,
                StorageSubTabPanelView.class,
                StorageSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageGeneralPresenter.class,
                SubTabStorageGeneralPresenter.ViewDef.class,
                SubTabStorageGeneralView.class,
                SubTabStorageGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageDataCenterPresenter.class,
                SubTabStorageDataCenterPresenter.ViewDef.class,
                SubTabStorageDataCenterView.class,
                SubTabStorageDataCenterPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageVmBackupPresenter.class,
                SubTabStorageVmBackupPresenter.ViewDef.class,
                SubTabStorageVmBackupView.class,
                SubTabStorageVmBackupPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageTemplateBackupPresenter.class,
                SubTabStorageTemplateBackupPresenter.ViewDef.class,
                SubTabStorageTemplateBackupView.class,
                SubTabStorageTemplateBackupPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageVmPresenter.class,
                SubTabStorageVmPresenter.ViewDef.class,
                SubTabStorageVmView.class,
                SubTabStorageVmPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageTemplatePresenter.class,
                SubTabStorageTemplatePresenter.ViewDef.class,
                SubTabStorageTemplateView.class,
                SubTabStorageTemplatePresenter.ProxyDef.class);
        bindPresenter(SubTabStorageIsoPresenter.class,
                SubTabStorageIsoPresenter.ViewDef.class,
                SubTabStorageIsoView.class,
                SubTabStorageIsoPresenter.ProxyDef.class);
        bindPresenter(SubTabStoragePermissionPresenter.class,
                SubTabStoragePermissionPresenter.ViewDef.class,
                SubTabStoragePermissionView.class,
                SubTabStoragePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabStorageEventPresenter.class,
                SubTabStorageEventPresenter.ViewDef.class,
                SubTabStorageEventView.class,
                SubTabStorageEventPresenter.ProxyDef.class);

        // Cluster
        bindPresenter(ClusterSubTabPanelPresenter.class,
                ClusterSubTabPanelPresenter.ViewDef.class,
                ClusterSubTabPanelView.class,
                ClusterSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterGeneralPresenter.class,
                SubTabClusterGeneralPresenter.ViewDef.class,
                SubTabClusterGeneralView.class,
                SubTabClusterGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterHostPresenter.class,
                SubTabClusterHostPresenter.ViewDef.class,
                SubTabClusterHostView.class,
                SubTabClusterHostPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterVmPresenter.class,
                SubTabClusterVmPresenter.ViewDef.class,
                SubTabClusterVmView.class,
                SubTabClusterVmPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterNetworkPresenter.class,
                SubTabClusterNetworkPresenter.ViewDef.class,
                SubTabClusterNetworkView.class,
                SubTabClusterNetworkPresenter.ProxyDef.class);
        bindPresenter(SubTabClusterPermissionPresenter.class,
                SubTabClusterPermissionPresenter.ViewDef.class,
                SubTabClusterPermissionView.class,
                SubTabClusterPermissionPresenter.ProxyDef.class);

        // Host
        bindPresenter(HostSubTabPanelPresenter.class,
                HostSubTabPanelPresenter.ViewDef.class,
                HostSubTabPanelView.class,
                HostSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabHostGeneralPresenter.class,
                SubTabHostGeneralPresenter.ViewDef.class,
                SubTabHostGeneralView.class,
                SubTabHostGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabHostVmPresenter.class,
                SubTabHostVmPresenter.ViewDef.class,
                SubTabHostVmView.class,
                SubTabHostVmPresenter.ProxyDef.class);
        bindPresenter(SubTabHostInterfacePresenter.class,
                SubTabHostInterfacePresenter.ViewDef.class,
                SubTabHostInterfaceView.class,
                SubTabHostInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabHostHookPresenter.class,
                SubTabHostHookPresenter.ViewDef.class,
                SubTabHostHookView.class,
                SubTabHostHookPresenter.ProxyDef.class);
        bindPresenter(SubTabHostPermissionPresenter.class,
                SubTabHostPermissionPresenter.ViewDef.class,
                SubTabHostPermissionView.class,
                SubTabHostPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabHostEventPresenter.class,
                SubTabHostEventPresenter.ViewDef.class,
                SubTabHostEventView.class,
                SubTabHostEventPresenter.ProxyDef.class);

        // VirtualMachine
        bindPresenter(VirtualMachineSubTabPanelPresenter.class,
                VirtualMachineSubTabPanelPresenter.ViewDef.class,
                VirtualMachineSubTabPanelView.class,
                VirtualMachineSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineGeneralPresenter.class,
                SubTabVirtualMachineGeneralPresenter.ViewDef.class,
                SubTabVirtualMachineGeneralView.class,
                SubTabVirtualMachineGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineNetworkInterfacePresenter.class,
                SubTabVirtualMachineNetworkInterfacePresenter.ViewDef.class,
                SubTabVirtualMachineNetworkInterfaceView.class,
                SubTabVirtualMachineNetworkInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineVirtualDiskPresenter.class,
                SubTabVirtualMachineVirtualDiskPresenter.ViewDef.class,
                SubTabVirtualMachineVirtualDiskView.class,
                SubTabVirtualMachineVirtualDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineSnapshotPresenter.class,
                SubTabVirtualMachineSnapshotPresenter.ViewDef.class,
                SubTabVirtualMachineSnapshotView.class,
                SubTabVirtualMachineSnapshotPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineApplicationPresenter.class,
                SubTabVirtualMachineApplicationPresenter.ViewDef.class,
                SubTabVirtualMachineApplicationView.class,
                SubTabVirtualMachineApplicationPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachinePermissionPresenter.class,
                SubTabVirtualMachinePermissionPresenter.ViewDef.class,
                SubTabVirtualMachinePermissionView.class,
                SubTabVirtualMachinePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabVirtualMachineEventPresenter.class,
                SubTabVirtualMachineEventPresenter.ViewDef.class,
                SubTabVirtualMachineEventView.class,
                SubTabVirtualMachineEventPresenter.ProxyDef.class);

        // Pool
        bindPresenter(PoolSubTabPanelPresenter.class,
                PoolSubTabPanelPresenter.ViewDef.class,
                PoolSubTabPanelView.class,
                PoolSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabPoolGeneralPresenter.class,
                SubTabPoolGeneralPresenter.ViewDef.class,
                SubTabPoolGeneralView.class,
                SubTabPoolGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabPoolVmPresenter.class,
                SubTabPoolVmPresenter.ViewDef.class,
                SubTabPoolVmView.class,
                SubTabPoolVmPresenter.ProxyDef.class);
        bindPresenter(SubTabPoolPermissionPresenter.class,
                SubTabPoolPermissionPresenter.ViewDef.class,
                SubTabPoolPermissionView.class,
                SubTabPoolPermissionPresenter.ProxyDef.class);

        // Template
        bindPresenter(TemplateSubTabPanelPresenter.class,
                TemplateSubTabPanelPresenter.ViewDef.class,
                TemplateSubTabPanelView.class,
                TemplateSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateGeneralPresenter.class,
                SubTabTemplateGeneralPresenter.ViewDef.class,
                SubTabTemplateGeneralView.class,
                SubTabTemplateGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateVmPresenter.class,
                SubTabTemplateVmPresenter.ViewDef.class,
                SubTabTemplateVmView.class,
                SubTabTemplateVmPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateInterfacePresenter.class,
                SubTabTemplateInterfacePresenter.ViewDef.class,
                SubTabTemplateInterfaceView.class,
                SubTabTemplateInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateDiskPresenter.class,
                SubTabTemplateDiskPresenter.ViewDef.class,
                SubTabTemplateDiskView.class,
                SubTabTemplateDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateStoragePresenter.class,
                SubTabTemplateStoragePresenter.ViewDef.class,
                SubTabTemplateStorageView.class,
                SubTabTemplateStoragePresenter.ProxyDef.class);
        bindPresenter(SubTabTemplatePermissionPresenter.class,
                SubTabTemplatePermissionPresenter.ViewDef.class,
                SubTabTemplatePermissionView.class,
                SubTabTemplatePermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabTemplateEventPresenter.class,
                SubTabTemplateEventPresenter.ViewDef.class,
                SubTabTemplateEventView.class,
                SubTabTemplateEventPresenter.ProxyDef.class);

        // User
        bindPresenter(UserSubTabPanelPresenter.class,
                UserSubTabPanelPresenter.ViewDef.class,
                UserSubTabPanelView.class,
                UserSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabUserGeneralPresenter.class,
                SubTabUserGeneralPresenter.ViewDef.class,
                SubTabUserGeneralView.class,
                SubTabUserGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabUserPermissionPresenter.class,
                SubTabUserPermissionPresenter.ViewDef.class,
                SubTabUserPermissionView.class,
                SubTabUserPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabUserEventNotifierPresenter.class,
                SubTabUserEventNotifierPresenter.ViewDef.class,
                SubTabUserEventNotifierView.class,
                SubTabUserEventNotifierPresenter.ProxyDef.class);
        bindPresenter(SubTabUserEventPresenter.class,
                SubTabUserEventPresenter.ViewDef.class,
                SubTabUserEventView.class,
                SubTabUserEventPresenter.ProxyDef.class);
        bindPresenter(SubTabUserGroupPresenter.class,
                SubTabUserGroupPresenter.ViewDef.class,
                SubTabUserGroupView.class,
                SubTabUserGroupPresenter.ProxyDef.class);

        // MAIN SECTION: -POPUPS-
        bindPresenterWidget(DefaultConfirmationPopupPresenterWidget.class,
                DefaultConfirmationPopupPresenterWidget.ViewDef.class,
                DefaultConfirmationPopupView.class);
        bindPresenterWidget(RemoveConfirmationPopupPresenterWidget.class,
                RemoveConfirmationPopupPresenterWidget.ViewDef.class,
                RemoveConfirmationPopupView.class);

        // Permissions
        bindPresenterWidget(PermissionsPopupPresenterWidget.class,
                PermissionsPopupPresenterWidget.ViewDef.class,
                PermissionsPopupView.class);

        // Bookmarks
        bindPresenterWidget(BookmarkPopupPresenterWidget.class,
                BookmarkPopupPresenterWidget.ViewDef.class,
                BookmarkPopupView.class);

        // Tags
        bindPresenterWidget(TagPopupPresenterWidget.class,
                TagPopupPresenterWidget.ViewDef.class,
                TagPopupView.class);

        // DataCenter
        bindPresenterWidget(DataCenterPopupPresenterWidget.class,
                DataCenterPopupPresenterWidget.ViewDef.class,
                DataCenterPopupView.class);
        bindPresenterWidget(FindMultiStoragePopupPresenterWidget.class,
                FindMultiStoragePopupPresenterWidget.ViewDef.class,
                FindMultiStoragePopupView.class);
        bindPresenterWidget(FindSingleStoragePopupPresenterWidget.class,
                FindSingleStoragePopupPresenterWidget.ViewDef.class,
                FindSingleStoragePopupView.class);
        bindPresenterWidget(DataCenterNetworkPopupPresenterWidget.class,
                DataCenterNetworkPopupPresenterWidget.ViewDef.class,
                DataCenterNetworkPopupView.class);

        // Cluster
        bindPresenterWidget(ClusterNewNetworkPopupPresenterWidget.class,
                ClusterNewNetworkPopupPresenterWidget.ViewDef.class,
                ClusterNewNetworkPopupView.class);

        bindPresenterWidget(ClusterPopupPresenterWidget.class,
                ClusterPopupPresenterWidget.ViewDef.class,
                ClusterPopupView.class);

        bindPresenterWidget(ClusterPolicyPopupPresenterWidget.class,
                ClusterPolicyPopupPresenterWidget.ViewDef.class,
                ClusterPolicyPopupView.class);

        // Host
        bindPresenterWidget(HostPopupPresenterWidget.class,
                HostPopupPresenterWidget.ViewDef.class,
                HostPopupView.class);
        bindPresenterWidget(HostInstallPopupPresenterWidget.class,
                HostInstallPopupPresenterWidget.ViewDef.class,
                HostInstallPopupView.class);
        bindPresenterWidget(HostInterfacePopupPresenterWidget.class,
                HostInterfacePopupPresenterWidget.ViewDef.class,
                HostInterfacePopupView.class);
        bindPresenterWidget(HostManagementPopupPresenterWidget.class,
                HostManagementPopupPresenterWidget.ViewDef.class,
                HostManagementPopupView.class);
        bindPresenterWidget(HostBondPopupPresenterWidget.class,
                HostBondPopupPresenterWidget.ViewDef.class,
                HostBondPopupView.class);
        bindPresenterWidget(DetachConfirmationPopupPresenterWidget.class,
                DetachConfirmationPopupPresenterWidget.ViewDef.class,
                DetachConfirmationPopupView.class);

        // Storage
        bindPresenterWidget(StoragePopupPresenterWidget.class,
                StoragePopupPresenterWidget.ViewDef.class,
                StoragePopupView.class);
        bindPresenterWidget(FindMultiDcPopupPresenterWidget.class,
                FindMultiDcPopupPresenterWidget.ViewDef.class,
                FindMultiDcPopupView.class);
        bindPresenterWidget(FindSingleDcPopupPresenterWidget.class,
                FindSingleDcPopupPresenterWidget.ViewDef.class,
                FindSingleDcPopupView.class);

        // Storage Remove
        bindPresenterWidget(StorageRemovePopupPresenterWidget.class,
                StorageRemovePopupPresenterWidget.ViewDef.class,
                StorageRemovePopupView.class);

        bindPresenterWidget(VmDesktopNewPopupPresenterWidget.class,
                VmDesktopNewPopupPresenterWidget.ViewDef.class,
                VmDesktopNewPopupView.class);
        
        bindPresenterWidget(VmServerNewPopupPresenterWidget.class,
                VmServerNewPopupPresenterWidget.ViewDef.class,
                VmServerNewPopupView.class);

     // VM Snapshot Create
        bindPresenterWidget(VmSnapshotCreatePopupPresenterWidget.class,
                VmSnapshotCreatePopupPresenterWidget.ViewDef.class,
                VmSnapshotCreatePopupView.class);

        // VM Assign Tags
        bindPresenterWidget(VmAssignTagsPopupPresenterWidget.class,
                VmAssignTagsPopupPresenterWidget.ViewDef.class,
                VmAssignTagsPopupView.class);

        // VM RunOnce
        bindPresenterWidget(VmRunOncePopupPresenterWidget.class,
                VmRunOncePopupPresenterWidget.ViewDef.class,
                VmRunOncePopupView.class);

        // VM Make Template
        bindPresenterWidget(VmMakeTemplatePopupPresenterWidget.class,
                VmMakeTemplatePopupPresenterWidget.ViewDef.class,
                VmMakeTemplatePopupView.class);

        // VM Change CD
        bindPresenterWidget(VmChangeCDPopupPresenterWidget.class,
                VmChangeCDPopupPresenterWidget.ViewDef.class,
                VmChangeCDPopupView.class);

        // VM Add/Edit Interface
        bindPresenterWidget(VmInterfacePopupPresenterWidget.class,
                VmInterfacePopupPresenterWidget.ViewDef.class,
                VmInterfacePopupView.class);

        // VM Add/Edit Disk
        bindPresenterWidget(VmDiskPopupPresenterWidget.class,
                VmDiskPopupPresenterWidget.ViewDef.class,
                VmDiskPopupView.class);

        // Edit Template
        bindPresenterWidget(TemplateNewPresenterWidget.class,
                TemplateNewPresenterWidget.ViewDef.class,
                TemplateNewPopupView.class);
        
        // Add/Edit Template's NIC
        bindPresenterWidget(TemplateInterfacePopupPresenterWidget.class,
                TemplateInterfacePopupPresenterWidget.ViewDef.class,
                TemplateInterfacePopupView.class);
    }

}
