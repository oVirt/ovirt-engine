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
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabBasicPresenter;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.MainTabExtendedPresenter;
import org.ovirt.engine.ui.userportal.section.main.view.HeaderView;
import org.ovirt.engine.ui.userportal.section.main.view.MainSectionView;
import org.ovirt.engine.ui.userportal.section.main.view.MainTabPanelView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.MainTabBasicView;
import org.ovirt.engine.ui.userportal.section.main.view.tab.MainTabExtendedView;
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
    }

}
