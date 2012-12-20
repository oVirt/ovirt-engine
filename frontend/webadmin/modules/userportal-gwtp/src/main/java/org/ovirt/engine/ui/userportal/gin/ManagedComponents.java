package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.userportal.ApplicationConstants;
import org.ovirt.engine.ui.userportal.ApplicationMessages;
import org.ovirt.engine.ui.userportal.ApplicationResources;
import org.ovirt.engine.ui.userportal.ApplicationResourcesWithLookup;
import org.ovirt.engine.ui.userportal.ApplicationTemplates;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginSectionPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
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
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmMonitorPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmNetworkInterfacePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmPermissionPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmSessionsPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmSnapshotPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmVirtualDiskPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.PoolDiskListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.PoolGeneralModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.PoolInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmDiskListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmEventListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmGeneralModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmInterfaceListModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmMonitorModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSessionsModelProvider;
import org.ovirt.engine.ui.userportal.uicommon.model.vm.VmSnapshotListModelProvider;

import com.google.gwt.inject.client.AsyncProvider;
import com.google.inject.Provider;

/**
 * Contains methods for accessing managed components that participate in dependency injection.
 * <p>
 * There should be a method for each {@link com.gwtplatform.mvp.client.Presenter Presenter} (excluding
 * {@link com.gwtplatform.mvp.clientcom.gwtplatform.mvp.client.PresenterWidget PresenterWidget} classes, unless they are
 * referenced through {@link ClientGinjector} directly). This is necessary due to the current limitation of GWTP-GIN
 * integration.
 */
public interface ManagedComponents {

    ApplicationConstants getApplicationConstants();

    ApplicationResources getApplicationResources();

    ApplicationResourcesWithLookup getApplicationResourcesWithLookup();

    ApplicationTemplates getApplicationTemplates();

    ApplicationMessages getApplicationMessages();

    ClientStorage getClientStorage();

    // Presenters: Login section

    Provider<LoginSectionPresenter> getLoginSectionPresenter();

    // Presenters: Main section: common stuff

    AsyncProvider<MainSectionPresenter> getMainSectionPresenter();

    // Presenters: Main section: main tabs

    AsyncProvider<MainTabPanelPresenter> getMainTabPanelPresenter();

    AsyncProvider<MainTabBasicPresenter> getMainTabBasicPresenter();

    AsyncProvider<MainTabExtendedPresenter> getMainTabExtendedPresenter();

    // Presenters: Main section: side tabs

    AsyncProvider<SideTabExtendedVirtualMachinePresenter> getSideTabExtendedVirtualMachinePresenter();

    AsyncProvider<SideTabExtendedTemplatePresenter> getSideTabExtendedTemplatePresenter();

    AsyncProvider<SideTabExtendedResourcePresenter> getSideTabExtendedResourcePresenter();

    // Presenters: Main section: sub tabs

    // Virtual Machine

    AsyncProvider<ExtendedVmSubTabPanelPresenter> getExtendedVmSubTabPanelPresenter();

    AsyncProvider<SubTabExtendedVmGeneralPresenter> getSubTabExtendedVmGeneralPresenter();

    AsyncProvider<SubTabExtendedVmNetworkInterfacePresenter> getSubTabExtendedVmNetworkInterfacePresenter();

    AsyncProvider<SubTabExtendedPoolNetworkInterfacePresenter> getSubTabExtendedPoolNetworkInterfacePresenter();

    AsyncProvider<SubTabExtendedVmVirtualDiskPresenter> getSubTabExtendedVmVirtualDiskPresenter();

    AsyncProvider<SubTabExtendedPoolVirtualDiskPresenter> getSubTabExtendedPoolVirtualDiskPresenter();

    AsyncProvider<SubTabExtendedVmSnapshotPresenter> getSubTabExtendedVmSnapshotPresenter();

    AsyncProvider<SubTabExtendedVmPermissionPresenter> getSubTabExtendedVmPermissionPresenter();

    AsyncProvider<SubTabExtendedVmEventPresenter> getSubTabExtendedVmEventPresenter();

    AsyncProvider<SubTabExtendedVmApplicationPresenter> getSubTabExtendedVmApplicationPresenter();

    AsyncProvider<SubTabExtendedVmMonitorPresenter> getSubTabExtendedVmMonitorPresenter();

    AsyncProvider<SubTabExtendedVmSessionsPresenter> getSubTabExtendedVmSessionsPresenter();

    VmSnapshotListModelProvider getVmSnapshotListModelProvider();

    VmEventListModelProvider getVmEventListModelProvider();

    VmMonitorModelProvider getVmMonitorModelProvider();

    VmGeneralModelProvider getVmGeneralModelProvider();

    PoolGeneralModelProvider getPoolGeneralModelProvider();

    VmInterfaceListModelProvider getVmInterfaceListModelProvider();

    PoolInterfaceListModelProvider getPoolInterfaceListModelProvider();

    VmDiskListModelProvider getVmDiskListModelProvider();

    PoolDiskListModelProvider getPoolDiskListModelProvider();

    AsyncProvider<SubTabExtendedPoolGeneralPresenter> getSubTabExtendedPoolGeneralPresenter();

    VmSessionsModelProvider getVmSessionsModelProvider();

    // Template

    AsyncProvider<ExtendedTemplateSubTabPanelPresenter> getExtendedTemplateSubTabPanelPresenter();

    AsyncProvider<SubTabExtendedTemplateGeneralPresenter> getSubTabExtendedTemplateGeneralPresenter();

    AsyncProvider<SubTabExtendedTemplateNetworkInterfacesPresenter> getSubTabExtendedTemplateNetworkInterfacesPresenter();

    AsyncProvider<SubTabExtendedTemplateVirtualDisksPresenter> getSubTabExtendedTemplateVirtualDisksPresenter();

    AsyncProvider<SubTabExtendedTemplateEventsPresenter> getSubTabExtendedTemplateEventsPresenter();

    AsyncProvider<SubTabExtendedTemplatePermissionsPresenter> getSubTabExtendedTemplatePermissionsPresenter();

}
