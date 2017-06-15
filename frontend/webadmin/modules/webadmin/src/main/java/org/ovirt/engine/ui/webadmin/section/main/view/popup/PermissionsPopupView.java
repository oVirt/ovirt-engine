package org.ovirt.engine.ui.webadmin.section.main.view.popup;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.permissions.AbstractPermissionsPopupView;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.PermissionsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.inject.Inject;

public class PermissionsPopupView extends AbstractPermissionsPopupView<AdElementListModel> implements PermissionsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<AdElementListModel, PermissionsPopupView> {
    }

    interface ViewIdHandler extends ElementIdHandler<PermissionsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public PermissionsPopupView(EventBus eventBus) {
        super(eventBus);
        driver.initialize(this);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void edit(AdElementListModel object) {
        super.edit(object);
        driver.edit(object);
    }

    @Override
    protected AdElementListModel doFlush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

}
