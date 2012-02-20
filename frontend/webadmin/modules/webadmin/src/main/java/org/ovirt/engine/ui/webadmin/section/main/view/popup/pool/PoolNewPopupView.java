package org.ovirt.engine.ui.webadmin.section.main.view.popup.pool;

import org.ovirt.engine.ui.common.widget.uicommon.popup.pool.PoolNewPopupWidget;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool.PoolNewPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractVmPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PoolNewPopupView extends AbstractVmPopupView implements PoolNewPopupPresenterWidget.ViewDef {

    @Inject
    public PoolNewPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources, new PoolNewPopupWidget(constants));
    }

}
