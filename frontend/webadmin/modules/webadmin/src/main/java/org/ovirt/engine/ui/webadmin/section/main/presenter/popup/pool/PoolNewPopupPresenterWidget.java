package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool;

import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PoolNewPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<PoolNewPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {

    }

    @Inject
    public PoolNewPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
