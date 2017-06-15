package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.FindMultiDcPopupPresenterWidget;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class FindMultiDcPopupView extends AbstractFindDcPopupView implements FindMultiDcPopupPresenterWidget.ViewDef {

    @Inject
    public FindMultiDcPopupView(EventBus eventBus) {
        super(eventBus, true);
    }

}
