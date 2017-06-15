package org.ovirt.engine.ui.webadmin.section.main.presenter.popup.datacenter;

import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class FindSingleStoragePopupPresenterWidget extends AbstractModelBoundPopupPresenterWidget<ListModel, FindSingleStoragePopupPresenterWidget.ViewDef> {

    public interface ViewDef extends AbstractModelBoundPopupPresenterWidget.ViewDef<ListModel> {
    }

    @Inject
    public FindSingleStoragePopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
