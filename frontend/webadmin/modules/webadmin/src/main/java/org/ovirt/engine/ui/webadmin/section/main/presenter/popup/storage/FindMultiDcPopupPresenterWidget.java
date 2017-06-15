package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class FindMultiDcPopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ListModel, FindMultiDcPopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ListModel> {
    }

    @Inject
    public FindMultiDcPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
