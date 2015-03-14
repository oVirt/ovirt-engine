package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.pool.IscsiBondPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.IscsiBondModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.IscsiBondPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class IscsiBondPopupView extends AbstractModelBoundWidgetPopupView<IscsiBondModel> implements IscsiBondPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<IscsiBondPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public IscsiBondPopupView(EventBus eventBus) {
        super(eventBus, new IscsiBondPopupWidget(), "690px", "640px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
