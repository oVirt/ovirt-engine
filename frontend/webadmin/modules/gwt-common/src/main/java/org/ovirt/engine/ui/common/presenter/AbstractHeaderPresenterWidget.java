package org.ovirt.engine.ui.common.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.utils.WebUtils;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.dom.client.HasClickHandlers;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.gwtplatform.mvp.client.PresenterWidget;
import com.gwtplatform.mvp.client.View;

public abstract class AbstractHeaderPresenterWidget<V extends AbstractHeaderPresenterWidget.ViewDef> extends PresenterWidget<V> {

    public interface ViewDef extends View {

        void setUserName(String userName);

        HasClickHandlers getLogoutLink();

        HasClickHandlers getGuideLink();

    }

    private final CurrentUser user;
    private final String windowName;
    private String guideUrl;

    public AbstractHeaderPresenterWidget(EventBus eventBus, V view, CurrentUser user,
            String windowName, String guideUrl) {
        super(eventBus, view);
        this.user = user;
        this.windowName = windowName;
        setGuideUrl(guideUrl);
    }

    @Override
    protected void onBind() {
        super.onBind();

        registerHandler(getView().getLogoutLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                user.logout();
            }
        }));

        registerHandler(getView().getGuideLink().addClickHandler(new ClickHandler() {
            @Override
            public void onClick(ClickEvent event) {
                WebUtils.openUrlInNewWindow(windowName, guideUrl);
            }
        }));
    }

    /**
     * Set the URL of the guide link. This string is escaped.
     * @param guideUrlString The new URL as a string.
     */
    public void setGuideUrl(String guideUrlString) {
        this.guideUrl = SafeHtmlUtils.htmlEscapeAllowEntities(guideUrlString);
    }

    @Override
    protected void onReset() {
        super.onReset();

        getView().setUserName(user.getUserName());
    }

}
