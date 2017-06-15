package org.ovirt.engine.ui.webadmin.section.main.view.popup.pool;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractVmPopupView;
import org.ovirt.engine.ui.common.view.popup.VmPopupResources;
import org.ovirt.engine.ui.common.widget.uicommon.popup.pool.PoolNewPopupWidget;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool.PoolNewPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PoolNewPopupView extends AbstractVmPopupView implements PoolNewPopupPresenterWidget.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<PoolNewPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @Inject
    public PoolNewPopupView(EventBus eventBus, VmPopupResources resources) {
        super(eventBus, new PoolNewPopupWidget(), resources);
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

}
