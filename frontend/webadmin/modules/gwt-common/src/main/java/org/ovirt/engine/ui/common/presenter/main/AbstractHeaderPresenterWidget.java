package org.ovirt.engine.ui.common.presenter.main;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.utils.WebUtils;
import org.ovirt.engine.ui.uicommonweb.Configurator;
import org.ovirt.engine.ui.uicommonweb.TypeResolver;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.dom.client.HasMouseOverHandlers;
import com.google.gwt.event.dom.client.MouseOverEvent;
import com.google.gwt.event.dom.client.MouseOverHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.event.shared.HasHandlers;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public abstract class AbstractHeaderPresenterWidget<V extends AbstractHeaderPresenterWidget.ViewDef> extends PresenterWidget<V> {

    public interface ViewDef extends View {

        void setUserName(String userName);

        HasClickHandlers getLogoutLink();

        HasHandlers getGuideLink();

        void setGuideLinkEnabled(boolean enabled);

    }

    private final CurrentUser user;
    private final String documentationGuidePath;
    private final String windowName;

    public AbstractHeaderPresenterWidget(EventBus eventBus, V view,
            CurrentUser user, String documentationGuidePath, String windowName) {
        super(eventBus, view);
        this.user = user;
        this.documentationGuidePath = documentationGuidePath;
        this.windowName = windowName;
    }

    @Override
    protected void onBind() {
        super.onBind();

        getView().setGuideLinkEnabled(false);

        registerHandler(getView().getLogoutLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                user.logout();
            }
        }));

        if (getView().getGuideLink() instanceof HasClickHandlers) {
            registerGuideLinkClickHandler((HasClickHandlers) getView().getGuideLink());
        }

        if (getView().getGuideLink() instanceof HasMouseOverHandlers) {
            registerGuideLinkMouseOverHandler((HasMouseOverHandlers) getView().getGuideLink());
        }
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().setUserName(user.getUserName());
    }

    void registerGuideLinkClickHandler(HasClickHandlers guideLink) {
        registerHandler(guideLink.addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                Configurator configurator = (Configurator) TypeResolver.getInstance().resolve(Configurator.class);
                if (configurator.isDocumentationAvailable()) {
                    String url = configurator.getDocumentationLibURL() + documentationGuidePath;
                    WebUtils.openUrlInNewWindow(windowName, url, WebUtils.OPTION_SCROLLBARS);
                }
            }
        }));
    }

    void registerGuideLinkMouseOverHandler(HasMouseOverHandlers guideLink) {
        registerHandler(guideLink.addMouseOverHandler(new MouseOverHandler() {
            @Override
            public void onMouseOver(MouseOverEvent event) {
                Configurator configurator = (Configurator) TypeResolver.getInstance().resolve(Configurator.class);
                getView().setGuideLinkEnabled(configurator.isDocumentationAvailable());
            }
        }));
    }

}
