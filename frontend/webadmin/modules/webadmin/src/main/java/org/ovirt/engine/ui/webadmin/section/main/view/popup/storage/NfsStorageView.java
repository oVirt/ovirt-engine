package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelTextBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class NfsStorageView extends AbstractStorageView<NfsStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<NfsStorageModel, NfsStorageView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, NfsStorageView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField(provided = true)
    @Path(value = "path.entity")
    EntityModelTextBoxEditor nfsPathEditor;

    @UiField
    @Ignore
    Label nfsMessageLabel;

    @UiField
    Label message;

    @Inject
    public NfsStorageView() {
        createPathEditor();
        nfsPathEditor = pathEditor;
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
        nfsPathEditor.addContentWidgetStyleName(style.nfsPathContentWidget());
    }

    void localize(ApplicationConstants constants) {
        nfsPathEditor.setLabel(constants.storagePopupNfsPathLabel());
        nfsMessageLabel.setText(constants.storagePopupNfsMessageLabel());
    }

    @Override
    public void edit(NfsStorageModel object) {
        Driver.driver.edit(object);

        nfsMessageLabel.setVisible(object.getPath().getIsAvailable());
    }

    @Override
    public NfsStorageModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String nfsPathContentWidget();
    }

    @Override
    public void focus() {
        nfsPathEditor.setFocus(true);
    }
}
