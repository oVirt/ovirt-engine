package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.uicommonweb.models.PublicKeyModel;

public class PublicKeyPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<PublicKeyModel, PublicKeyPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<PublicKeyModel> {
    }

    @Inject
    public PublicKeyPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
