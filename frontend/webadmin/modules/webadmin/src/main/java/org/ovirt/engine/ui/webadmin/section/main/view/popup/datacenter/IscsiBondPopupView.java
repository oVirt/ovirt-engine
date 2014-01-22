package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundWidgetPopupView;
import org.ovirt.engine.ui.common.widget.uicommon.popup.pool.IscsiBondPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.datacenters.IscsiBondModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.IscsiBondPopupPresenterWidget;

public class IscsiBondPopupView extends AbstractModelBoundWidgetPopupView<IscsiBondModel> implements IscsiBondPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<IscsiBondPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public IscsiBondPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, new IscsiBondPopupWidget(constants), "690px", "640px"); //$NON-NLS-1$ //$NON-NLS-2$
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }
}
