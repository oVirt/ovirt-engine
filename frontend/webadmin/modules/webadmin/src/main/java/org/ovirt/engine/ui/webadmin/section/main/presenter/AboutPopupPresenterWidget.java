package org.ovirt.engine.ui.webadmin.section.main.presenter;

import org.ovirt.engine.ui.common.presenter.AbstractPopupPresenterWidget;
import org.ovirt.engine.ui.common.system.EngineRpmVersionData;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

/**
 * Implements the WebAdmin about dialog.
 */
public class AboutPopupPresenterWidget extends AbstractPopupPresenterWidget<AboutPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractPopupPresenterWidget.ViewDef {

        void setVersion(String version);

    }

    @Inject
    public AboutPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

    @Override
    protected void onReveal() {
        super.onReveal();

        String version = EngineRpmVersionData.getVersion();
        getView().setVersion(version);
    }

    @Override
    protected boolean shouldDestroyOnClose() {
        return true;
    }

}
