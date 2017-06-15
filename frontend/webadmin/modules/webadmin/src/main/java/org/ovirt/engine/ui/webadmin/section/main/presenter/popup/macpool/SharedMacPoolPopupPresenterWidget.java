package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.macpool;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.macpool.SharedMacPoolModel;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool.SharedMacPoolPopupView;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class SharedMacPoolPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<SharedMacPoolModel, SharedMacPoolPopupView> {

    @Inject
    public SharedMacPoolPopupPresenterWidget(EventBus eventBus, SharedMacPoolPopupView view) {
        super(eventBus, view);
    }

}
