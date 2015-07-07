package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.PublicKeyPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.PublicKeyModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.PublicKeyPopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PublicKeyPopupView extends AbstractModelBoundWidgetPopupView<PublicKeyModel> implements PublicKeyPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<PublicKeyPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public PublicKeyPopupView(EventBus eventBus) {
        super(eventBus, new PublicKeyPopupWidget(), "600px", "160px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
