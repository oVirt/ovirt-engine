package org.ovirt.engine.ui.userportal.gin;

import org.ovirt.engine.ui.common.gin.BasePresenterModule;
import org.ovirt.engine.ui.userportal.main.view.popup.DefaultConfirmationPopupView;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginPopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.login.presenter.LoginSectionPresenter;
import org.ovirt.engine.ui.userportal.section.login.view.LoginPopupView;
import org.ovirt.engine.ui.userportal.section.login.view.LoginSectionView;
import org.ovirt.engine.ui.userportal.section.main.presenter.HeaderPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainSectionPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.MainTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.popup.console.ConsolePopupPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicDetailsPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListItemPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.basic.MainTabBasicListPresenterWidget;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedResourcePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedTemplatePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.SideTabExtendedVirtualMachinePresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.ExtendedTemplateSubTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateGeneralPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.ExtendedVmSubTabPanelPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.vm.SubTabExtendedVmGeneralPresenter;
import org.ovirt.engine.ui.userportal.section.main.view.HeaderView;
import org.ovirt.engine.ui.userportal.section.main.view.MainSectionView;
import org.ovirt.engine.ui.userportal.section.main.view.MainTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.popup.console.ConsolePopupView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.MainTabBasicView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.MainTabExtendedView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicDetailsView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListItemView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.basic.MainTabBasicListView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.SideTabExtendedResourceView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.SideTabExtendedTemplateView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.SideTabExtendedVirtualMachineView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.ExtendedTemplateSubTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template.SubTabExtendedTemplateGeneralView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.ExtendedVmSubTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.extended.vm.SubTabExtendedVmGeneralView;
import org.ovirt.engine.ui.userportal.view.ErrorPopupView;

/**
 * GIN module containing UserPortal GWTP presenter bindings.
 */
public class PresenterModule extends BasePresenterModule {

    @Override
    protected void configure() {
        // Common stuff
        bindCommonPresenters(ErrorPopupView.class, DefaultConfirmationPopupView.class);

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
        bindSingletonPresenterWidget(HeaderPresenterWidget.class,
                HeaderPresenterWidget.ViewDef.class,
                HeaderView.class);

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

        // Template
        bindPresenter(ExtendedTemplateSubTabPanelPresenter.class,
                ExtendedTemplateSubTabPanelPresenter.ViewDef.class,
                ExtendedTemplateSubTabPanelView.class,
                ExtendedTemplateSubTabPanelPresenter.ProxyDef.class);
        bindPresenter(SubTabExtendedTemplateGeneralPresenter.class,
                SubTabExtendedTemplateGeneralPresenter.ViewDef.class,
                SubTabExtendedTemplateGeneralView.class,
                SubTabExtendedTemplateGeneralPresenter.ProxyDef.class);

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

        // Console popup
        bindPresenterWidget(ConsolePopupPresenterWidget.class,
                ConsolePopupPresenterWidget.ViewDef.class,
                ConsolePopupView.class);
    }

}
