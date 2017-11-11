package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.auth.CurrentUser;
import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.EngineRpmVersionData;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * WebAdmin about dialog
 */
public class AboutPopupPresenterWidget extends AbstractPopupPresenterWidget<AboutPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {

        void setVersion(String version);

        void setUserName(String userName);

    }

    private final CurrentUser user;

    @Inject
    public AboutPopupPresenterWidget(EventBus eventBus, ViewDef view, CurrentUser user) {
        super(eventBus, view);
        this.user = user;
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        String version = EngineRpmVersionData.getVersion();
        getView().setVersion(version);
        getView().setUserName(user.getFullUserName());
    }

}
