package org.ovirt.engine.ui.webadmin.section.main.presenter.popup;

import org.ovirt.engine.ui.common.presenter.popup.permissions.AbstractPermissionsPopupPresenterWidget;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;

import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PermissionsPopupPresenterWidget extends AbstractPermissionsPopupPresenterWidget<PermissionsPopupPresenterWidget.ViewDef, AdElementListModel> {

    public interface ViewDef extends AbstractPermissionsPopupPresenterWidget.ViewDef<AdElementListModel> {
    }

    @Inject
    public PermissionsPopupPresenterWidget(EventBus eventBus, ViewDef view) {
        super(eventBus, view);
    }

}
