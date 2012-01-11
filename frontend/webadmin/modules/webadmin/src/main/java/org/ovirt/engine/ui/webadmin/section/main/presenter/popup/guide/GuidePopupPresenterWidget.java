package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide;

import org.ovirt.engine.ui.uicommonweb.models.GuideModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class GuidePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<GuideModel, GuidePopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<GuideModel> {
    }

    @Inject
    public GuidePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
