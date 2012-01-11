package org.ovirt.engine.ui.webadmin.section.login.presenter;

import java.util.logging.Logger;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.system.ErrorPopupManager;
import org.ovirt.engine.ui.webadmin.auth.SilentLoginData;
import org.ovirt.engine.ui.webadmin.place.ApplicationPlaces;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.PlaceRequest;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealRootLayoutContentEvent;
import com.gwtplatform.mvp.client.proxy.RevealRootPopupContentEvent;

public class LoginSectionPresenter extends Presenter<LoginSectionPresenter.ViewDef, LoginSectionPresenter.ProxyDef> {

    @ProxyStandard
    @NameToken(ApplicationPlaces.loginPlace)
    @NoGatekeeper
    public interface ProxyDef extends ProxyPlace<LoginSectionPresenter> {
    }

    public interface ViewDef extends View {
    }

    private static final Logger logger = Logger.getLogger(LoginSectionPresenter.class.getName());

    private final PlaceManager placeManager;
    private final CurrentUser user;
    private final LoginPopupPresenterWidget loginPopup;
    private final ErrorPopupManager errorPopupManager;

    @Inject
    public LoginSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, CurrentUser user, LoginPopupPresenterWidget loginPopup,
            ErrorPopupManager errorPopupManager) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.user = user;
        this.loginPopup = loginPopup;
        this.errorPopupManager = errorPopupManager;
    }

    @Override
    protected void revealInParent() {
        RevealRootLayoutContentEvent.fire(this, this);
    }

    @Override
    public void prepareFromRequest(PlaceRequest request) {
        super.prepareFromRequest(request);

        SilentLoginData silentLoginData = null;

        // Activate silent login when running in development mode
        if (!GWT.isProdMode() && "true".equalsIgnoreCase(request.getParameter("silentLogin", null))) {
            String adminUser = request.getParameter("user", null);
            String password = request.getParameter("password", null);
            String domain = request.getParameter("domain", null);

            if (adminUser != null && !adminUser.isEmpty()
                    && password != null && !password.isEmpty()) {
                logger.info("Silent login is enabled");
                silentLoginData = new SilentLoginData(adminUser, password, domain);
            }
        }

        loginPopup.setSilentLoginData(silentLoginData);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        // Disable error popups when entering login section
        errorPopupManager.setShowPopups(false);
    }

    @Override
    protected void onHide() {
        super.onHide();

        // Enable error popups when leaving login section
        errorPopupManager.setShowPopups(true);
    }

    @Override
    protected void onReset() {
        super.onReset();

        if (user.isLoggedIn()) {
            placeManager.revealDefaultPlace();
        } else {
            RevealRootPopupContentEvent.fire(this, loginPopup);
        }
    }

}
