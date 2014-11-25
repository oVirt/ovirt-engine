package org.ovirt.engine.ui.webadmin.section.login.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.uicommonweb.place.WebAdminApplicationPlaces;

import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.GwtEvent.Type;
import com.google.inject.Inject;
import com.gwtplatform.mvp.client.Presenter;
import com.gwtplatform.mvp.client.View;
import com.gwtplatform.mvp.client.annotations.ContentSlot;
import com.gwtplatform.mvp.client.annotations.NameToken;
import com.gwtplatform.mvp.client.annotations.NoGatekeeper;
import com.gwtplatform.mvp.client.annotations.ProxyStandard;
import com.gwtplatform.mvp.client.proxy.PlaceManager;
import com.gwtplatform.mvp.client.proxy.ProxyPlace;
import com.gwtplatform.mvp.client.proxy.RevealContentHandler;

public class LoginSectionPresenter extends Presenter<LoginSectionPresenter.ViewDef, LoginSectionPresenter.ProxyDef> {

    @ProxyStandard
    @NameToken(WebAdminApplicationPlaces.loginPlace)
    @NoGatekeeper
    public interface ProxyDef extends ProxyPlace<LoginSectionPresenter> {
    }

    public interface ViewDef extends View {
    }

    @ContentSlot
    public static final Type<RevealContentHandler<?>> TYPE_SetLoginForm = new Type<RevealContentHandler<?>>();

    private final PlaceManager placeManager;
    private final CurrentUser user;
    private final LoginFormPresenterWidget loginForm;

    @Inject
    public LoginSectionPresenter(EventBus eventBus, ViewDef view, ProxyDef proxy,
            PlaceManager placeManager, CurrentUser user, LoginFormPresenterWidget loginForm) {
        super(eventBus, view, proxy, RevealType.RootLayout);
        this.placeManager = placeManager;
        this.user = user;
        this.loginForm = loginForm;
    }

    @Override
    protected void onReset() {
        super.onReset();

        if (user.isLoggedIn()) {
            placeManager.revealDefaultPlace();
        }
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        setInSlot(TYPE_SetLoginForm, loginForm);
    }

}
