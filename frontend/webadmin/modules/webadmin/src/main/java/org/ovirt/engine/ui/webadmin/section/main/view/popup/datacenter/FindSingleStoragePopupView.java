package org.ovirt.engine.ui.webadmin.section.main.view.popup.datacenter;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter.FindSingleStoragePopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class FindSingleStoragePopupView extends AbstractFindStoragePopupView implements FindSingleStoragePopupPresenterWidget.ViewDef {

    @Inject
    public FindSingleStoragePopupView(EventBus eventBus) {
        super(eventBus, false);
    }

}
