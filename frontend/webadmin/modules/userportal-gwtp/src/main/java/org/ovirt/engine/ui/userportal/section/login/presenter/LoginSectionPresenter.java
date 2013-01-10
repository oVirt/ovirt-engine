package org.ovirt.engine.ui.userportal.section.login.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.userportal.place.ApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
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

    private final PlaceManager placeManager;
    private final CurrentUser user;
    private final LoginPopupPresenterWidget loginPopup;

    @Inject
    public LoginSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, CurrentUser user, LoginPopupPresenterWidget loginPopup) {
        super(eventBus, view, proxy);
        this.placeManager = placeManager;
        this.user = user;
        this.loginPopup = loginPopup;
    }

    @Override
    protected void revealInParent() {
        RevealRootLayoutContentEvent.fire(this, this);
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
