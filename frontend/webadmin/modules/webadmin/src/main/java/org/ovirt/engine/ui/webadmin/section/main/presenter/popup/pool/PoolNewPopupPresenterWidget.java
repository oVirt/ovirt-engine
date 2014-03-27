package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.pool;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.system.ClientStorage;
import org.ovirt.engine.ui.common.widget.popup.AbstractVmBasedPopupPresenterWidget;

public class PoolNewPopupPresenterWidget extends BasePoolPopupPresenterWidget<PoolNewPopupPresenterWidget.ViewDef> {
    public interface ViewDef extends AbstractVmBasedPopupPresenterWidget.ViewDef {

    }

    @Inject
    public PoolNewPopupPresenterWidget(EventBus eventBus, ViewDef view, CommonApplicationMessages messages, ClientStorage clientStorage) {
        super(eventBus, view, messages, clientStorage);
    }

}
