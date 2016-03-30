package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.gin.BasePresenterModule;
import org.ovirt.engine.ui.userportal.section.main.presenter.AboutPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.permissions.PermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateEditPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.template.TemplateInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.CloneVmPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.SingleSelectionVmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmClonePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskAttachPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmDiskRemovePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmInterfacePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmMakeTemplatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmNextRunConfigurationPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmRunOncePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCreatePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotCustomPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VmSnapshotPreviewPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.vm.VncInfoPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicDetailsPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListItemPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedResourcePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedVirtualMachinePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.ExtendedTemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateEventsPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateGeneralPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateNetworkInterfacesPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplatePermissionsPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateVirtualDisksPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.ExtendedVmSubTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedPoolGeneralPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedPoolNetworkInterfacePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedPoolVirtualDiskPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmApplicationPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmEventPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGeneralPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGuestContainerPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGuestInfoPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmMonitorPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmNetworkInterfacePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmPermissionPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmSnapshotPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmVirtualDiskPresenter;
import org.ovirt.engine.ui.userportal.section.main.view.AboutPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.HeaderView;
import org.ovirt.engine.ui.userportal.section.main.view.MainSectionView;
import org.ovirt.engine.ui.userportal.section.main.view.MainTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.permissions.PermissionsPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.template.TemplateEditPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.template.TemplateInterfacePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.CloneVmPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.SingleSelectionVmDiskAttachPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmChangeCDPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmClonePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmDiskAttachPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmDiskPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmDiskRemovePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmInterfacePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmMakeTemplatePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmNextRunConfigurationPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmRunOncePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmSnapshotCreatePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmSnapshotCustomPreviewPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VmSnapshotPreviewPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.vm.VncInfoPopupView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.MainTabBasicView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.MainTabExtendedView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicDetailsView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.SideTabExtendedResourceView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.SideTabExtendedTemplateView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.SideTabExtendedVirtualMachineView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.ExtendedTemplateSubTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.SubTabExtendedTemplateEventsView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.SubTabExtendedTemplateGeneralView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.SubTabExtendedTemplateNetworkInterfacesView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.SubTabExtendedTemplatePermissionsView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.SubTabExtendedTemplateVirtualDisksView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.ExtendedVmSubTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedPoolGeneralView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedPoolNetworkInterfaceView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedPoolVirtualDiskView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmApplicationView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmEventView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmGeneralView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmGuestContainerView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmGuestInfoView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmMonitorView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmNetworkInterfaceView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmPermissionView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmSnapshotView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmVirtualDiskView;

/**
 * GIN module containing UserPortal GWTP presenter bindings.
 */
public class PresenterModule extends BasePresenterModule {

    @Override
    protected void configure() {
        // Common stuff
        bindCommonPresenters();

        // Main section: common stuff
        bindPresenter(MainSectionPresenter.class,
                MainSectionPresenter.ViewDef.class,
                MainSectionView.class,
                MainSectionPresenter.ProxyDef.class);
        bindSingletonPresenterWidget(HeaderPresenterWidget.class,
                HeaderPresenterWidget.ViewDef.class,
                HeaderView.class);
        bindPresenterWidget(AboutPopupPresenterWidget.class,
                AboutPopupPresenterWidget.ViewDef.class,
                AboutPopupView.class);

        // Main section: main tabs
        bindPresenter(MainTabPanelPresenter.class,
                MainTabPanelPresenter.ViewDef.class,
                MainTabPanelView.class,
                MainTabPanelPresenter.ProxyDef.class);
        bindPresenter(MainTabBasicPresenter.class,
                MainTabBasicPresenter.ViewDef.class,
                MainTabBasicView.class,
                MainTabBasicPresenter.ProxyDef.class);
        bindPresenter(MainTabExtendedPresenter.class,
                MainTabExtendedPresenter.ViewDef.class,
                MainTabExtendedView.class,
                MainTabExtendedPresenter.ProxyDef.class);

        // Main section: side tabs
        bindPresenter(SideTabExtendedVirtualMachinePresenter.class,
                SideTabExtendedVirtualMachinePresenter.ViewDef.class,
                SideTabExtendedVirtualMachineView.class,
                SideTabExtendedVirtualMachinePresenter.ProxyDef.class);
        bindPresenter(SideTabExtendedTemplatePresenter.class,
                SideTabExtendedTemplatePresenter.ViewDef.class,
                SideTabExtendedTemplateView.class,
                SideTabExtendedTemplatePresenter.ProxyDef.class);
        bindPresenter(SideTabExtendedResourcePresenter.class,
                SideTabExtendedResourcePresenter.ViewDef.class,
                SideTabExtendedResourceView.class,
                SideTabExtendedResourcePresenter.ProxyDef.class);

        // Main section: sub tabs

        // Virtual Machine
        bindPresenter(ExtendedVmSubTabPanelPresenter.class,
                ExtendedVmSubTabPanelPresenter.ViewDef.class,
                ExtendedVmSubTabPanelView.class,
                ExtendedVmSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmGeneralPresenter.class,
                SubTabExtendedVmGeneralPresenter.ViewDef.class,
                SubTabExtendedVmGeneralView.class,
                SubTabExtendedVmGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedPoolGeneralPresenter.class,
                SubTabExtendedPoolGeneralPresenter.ViewDef.class,
                SubTabExtendedPoolGeneralView.class,
                SubTabExtendedPoolGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmNetworkInterfacePresenter.class,
                SubTabExtendedVmNetworkInterfacePresenter.ViewDef.class,
                SubTabExtendedVmNetworkInterfaceView.class,
                SubTabExtendedVmNetworkInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedPoolNetworkInterfacePresenter.class,
                SubTabExtendedPoolNetworkInterfacePresenter.ViewDef.class,
                SubTabExtendedPoolNetworkInterfaceView.class,
                SubTabExtendedPoolNetworkInterfacePresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmVirtualDiskPresenter.class,
                SubTabExtendedVmVirtualDiskPresenter.ViewDef.class,
                SubTabExtendedVmVirtualDiskView.class,
                SubTabExtendedVmVirtualDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedPoolVirtualDiskPresenter.class,
                SubTabExtendedPoolVirtualDiskPresenter.ViewDef.class,
                SubTabExtendedPoolVirtualDiskView.class,
                SubTabExtendedPoolVirtualDiskPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmSnapshotPresenter.class,
                SubTabExtendedVmSnapshotPresenter.ViewDef.class,
                SubTabExtendedVmSnapshotView.class,
                SubTabExtendedVmSnapshotPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmPermissionPresenter.class,
                SubTabExtendedVmPermissionPresenter.ViewDef.class,
                SubTabExtendedVmPermissionView.class,
                SubTabExtendedVmPermissionPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmEventPresenter.class,
                SubTabExtendedVmEventPresenter.ViewDef.class,
                SubTabExtendedVmEventView.class,
                SubTabExtendedVmEventPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmApplicationPresenter.class,
                SubTabExtendedVmApplicationPresenter.ViewDef.class,
                SubTabExtendedVmApplicationView.class,
                SubTabExtendedVmApplicationPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmGuestContainerPresenter.class,
                SubTabExtendedVmGuestContainerPresenter.ViewDef.class,
                SubTabExtendedVmGuestContainerView.class,
                SubTabExtendedVmGuestContainerPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmMonitorPresenter.class,
                SubTabExtendedVmMonitorPresenter.ViewDef.class,
                SubTabExtendedVmMonitorView.class,
                SubTabExtendedVmMonitorPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedVmGuestInfoPresenter.class,
                SubTabExtendedVmGuestInfoPresenter.ViewDef.class,
                SubTabExtendedVmGuestInfoView.class,
                SubTabExtendedVmGuestInfoPresenter.ProxyDef.class);

        // Template
        bindPresenter(ExtendedTemplateSubTabPanelPresenter.class,
                ExtendedTemplateSubTabPanelPresenter.ViewDef.class,
                ExtendedTemplateSubTabPanelView.class,
                ExtendedTemplateSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedTemplateGeneralPresenter.class,
                SubTabExtendedTemplateGeneralPresenter.ViewDef.class,
                SubTabExtendedTemplateGeneralView.class,
                SubTabExtendedTemplateGeneralPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedTemplateNetworkInterfacesPresenter.class,
                SubTabExtendedTemplateNetworkInterfacesPresenter.ViewDef.class,
                SubTabExtendedTemplateNetworkInterfacesView.class,
                SubTabExtendedTemplateNetworkInterfacesPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedTemplateVirtualDisksPresenter.class,
                SubTabExtendedTemplateVirtualDisksPresenter.ViewDef.class,
                SubTabExtendedTemplateVirtualDisksView.class,
                SubTabExtendedTemplateVirtualDisksPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedTemplateEventsPresenter.class,
                SubTabExtendedTemplateEventsPresenter.ViewDef.class,
                SubTabExtendedTemplateEventsView.class,
                SubTabExtendedTemplateEventsPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedTemplatePermissionsPresenter.class,
                SubTabExtendedTemplatePermissionsPresenter.ViewDef.class,
                SubTabExtendedTemplatePermissionsView.class,
                SubTabExtendedTemplatePermissionsPresenter.ProxyDef.class);

        // Main section: basic view
        bindSingletonPresenterWidget(MainTabBasicDetailsPresenterWidget.class,
                MainTabBasicDetailsPresenterWidget.ViewDef.class,
                MainTabBasicDetailsView.class);
        bindSingletonPresenterWidget(MainTabBasicListPresenterWidget.class,
                MainTabBasicListPresenterWidget.ViewDef.class,
                MainTabBasicListView.class);
        bindPresenterWidget(MainTabBasicListItemPresenterWidget.class,
                MainTabBasicListItemPresenterWidget.ViewDef.class,
                MainTabBasicListItemView.class);

        // Main section: popups

        // Permissions
        bindPresenterWidget(PermissionsPopupPresenterWidget.class,
                PermissionsPopupPresenterWidget.ViewDef.class,
                PermissionsPopupView.class);

        // VM popups
        bindPresenterWidget(VmPopupPresenterWidget.class,
                VmPopupPresenterWidget.ViewDef.class,
                VmPopupView.class);
        bindPresenterWidget(VmRunOncePopupPresenterWidget.class,
                VmRunOncePopupPresenterWidget.ViewDef.class,
                VmRunOncePopupView.class);
        bindPresenterWidget(VmChangeCDPopupPresenterWidget.class,
                VmChangeCDPopupPresenterWidget.ViewDef.class,
                VmChangeCDPopupView.class);
        bindPresenterWidget(CloneVmPopupPresenterWidget.class,
                CloneVmPopupPresenterWidget.ViewDef.class,
                CloneVmPopupView.class);
        bindPresenterWidget(VmMakeTemplatePopupPresenterWidget.class,
                VmMakeTemplatePopupPresenterWidget.ViewDef.class,
                VmMakeTemplatePopupView.class);
        bindPresenterWidget(VmDiskPopupPresenterWidget.class,
                VmDiskPopupPresenterWidget.ViewDef.class,
                VmDiskPopupView.class);
        bindPresenterWidget(VmDiskAttachPopupPresenterWidget.class,
                VmDiskAttachPopupPresenterWidget.ViewDef.class,
                VmDiskAttachPopupView.class);
        bindPresenterWidget(SingleSelectionVmDiskAttachPopupPresenterWidget.class,
                SingleSelectionVmDiskAttachPopupPresenterWidget.ViewDef.class,
                SingleSelectionVmDiskAttachPopupView.class);
        bindPresenterWidget(VmDiskRemovePopupPresenterWidget.class,
                VmDiskRemovePopupPresenterWidget.ViewDef.class,
                VmDiskRemovePopupView.class);
        bindPresenterWidget(VmSnapshotCreatePopupPresenterWidget.class,
                VmSnapshotCreatePopupPresenterWidget.ViewDef.class,
                VmSnapshotCreatePopupView.class);
        bindPresenterWidget(VmSnapshotPreviewPopupPresenterWidget.class,
                VmSnapshotPreviewPopupPresenterWidget.ViewDef.class,
                VmSnapshotPreviewPopupView.class);
        bindPresenterWidget(VmSnapshotCustomPreviewPopupPresenterWidget.class,
                VmSnapshotCustomPreviewPopupPresenterWidget.ViewDef.class,
                VmSnapshotCustomPreviewPopupView.class);
        bindPresenterWidget(VmClonePopupPresenterWidget.class,
                VmClonePopupPresenterWidget.ViewDef.class,
                VmClonePopupView.class);
        bindPresenterWidget(VncInfoPopupPresenterWidget.class,
                VncInfoPopupPresenterWidget.ViewDef.class,
                VncInfoPopupView.class);
        bindPresenterWidget(VmNextRunConfigurationPresenterWidget.class,
                VmNextRunConfigurationPresenterWidget.ViewDef.class,
                VmNextRunConfigurationPopupView.class);

        // Template popups
        bindPresenterWidget(TemplateEditPopupPresenterWidget.class,
                TemplateEditPopupPresenterWidget.ViewDef.class,
                TemplateEditPopupView.class);

        bindPresenterWidget(VmInterfacePopupPresenterWidget.class,
                VmInterfacePopupPresenterWidget.ViewDef.class,
                VmInterfacePopupView.class);

        bindPresenterWidget(TemplateInterfacePopupPresenterWidget.class,
                TemplateInterfacePopupPresenterWidget.ViewDef.class,
                TemplateInterfacePopupView.class);
    }

}
