package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindMultiStoragePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class FindMultiStoragePopupView extends AbstractFindStoragePopupView implements FindMultiStoragePopupPresenterWidget.ViewDef {

    @Inject
    public FindMultiStoragePopupView(EventBus eventBus) {
        super(eventBus, true);
    }

}
