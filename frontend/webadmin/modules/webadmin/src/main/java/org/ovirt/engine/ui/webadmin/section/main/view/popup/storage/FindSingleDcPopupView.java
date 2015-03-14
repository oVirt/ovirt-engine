package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindSingleDcPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class FindSingleDcPopupView extends AbstractFindDcPopupView implements FindSingleDcPopupPresenterWidget.ViewDef {

    @Inject
    public FindSingleDcPopupView(EventBus eventBus) {
        super(eventBus, false);
    }

}
