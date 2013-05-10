package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool;

import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PoolEditPopupPresenterWidget extends AbstractVmBasedPopupPresenterWidget<PoolEditPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {

    }

    @Inject
    public PoolEditPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }
}
