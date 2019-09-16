package org.ovirt.engine.ui.webadmin.section.main.view.popup.user;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Role;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.VisibilityRenderer;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelMultipleSelectListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.uicommonweb.models.users.AdElementListModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.user.UserRolesPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;

public class UserRolesPopupView extends AbstractModelBoundPopupView<AdElementListModel>
    implements UserRolesPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<AdElementListModel, UserRolesPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, UserRolesPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField(provided = true)
    @Path("role.selectedItems")
    public ListModelMultipleSelectListBoxEditor<Role> roleSelection;

    @UiField
    @Ignore
    Label errorMessage;

    @Inject
    public UserRolesPopupView(EventBus eventBus) {
        super(eventBus);
        roleSelection = new ListModelMultipleSelectListBoxEditor<>(new NameRenderer<Role>(),
                new VisibilityRenderer.SimpleVisibilityRenderer());
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);
    }

    @Override
    public void edit(AdElementListModel object) {
        driver.edit(object);
    }

    @Override
    public AdElementListModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        errorMessage.setText(message);
    }
}
