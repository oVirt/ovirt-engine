package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.UIObject;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

public class NfsStorageView extends AbstractStorageView<NfsStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<NfsStorageModel, NfsStorageView> {

        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, NfsStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "path.entity")
    EntityModelTextBoxOnlyEditor pathEditor;

    @UiField
    @Ignore
    Label pathLabel;

    @UiField
    @Ignore
    Label pathHintLabel;

    @UiField(provided = true)
    @Path(value = "version.selectedItem")
    ListModelListBoxOnlyEditor<Object> versionEditor;

    @UiField
    @Path(value = "version.entity")
    EntityModelTextBoxOnlyEditor versionEditorLabel;

    @UiField
    @Ignore
    Label versionLabel;

    @UiField
    @Path(value = "retransmissions.entity")
    EntityModelTextBoxOnlyEditor retransmissionsEditor;

    @UiField
    @Ignore
    Label retransmissionsLabel;

    @UiField
    @Path(value = "timeout.entity")
    EntityModelTextBoxOnlyEditor timeoutEditor;

    @UiField
    @Ignore
    Label timeoutLabel;

    @UiField
    @Path(value = "mountOptions.entity")
    EntityModelTextBoxOnlyEditor mountOptionsEditor;

    @UiField
    @Ignore
    Label mountOptionsLabel;

    @UiField
    Label message;

    @Inject
    public NfsStorageView() {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
        pathEditor.addContentWidgetStyleName(style.pathEditorContent());
    }

    void initEditors() {

        versionEditor = new ListModelListBoxOnlyEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {

                EntityModel model = (EntityModel) object;
                return model.getTitle();
            }
        });
    }

    void localize(ApplicationConstants constants) {

        pathLabel.setText(constants.storagePopupNfsPathLabel());
        pathHintLabel.setText(constants.storagePopupNfsPathHintLabel());
        versionLabel.setText(constants.storagePopupNfsVersionLabel());
        retransmissionsLabel.setText(constants.storagePopupNfsRetransmissionsLabel());
        timeoutLabel.setText(constants.storagePopupNfsTimeoutLabel());
        mountOptionsLabel.setText(constants.storagePopupNfsMountOptionsLabel());
    }

    @Override
    public void edit(NfsStorageModel object) {
        Driver.driver.edit(object);

        pathHintLabel.setVisible(object.getPath().getIsAvailable() && object.getPath().getIsChangable());

        styleTextBoxEditor(pathEditor, object.getPath());
        styleTextBoxEditor(timeoutEditor, object.getTimeout());
        styleTextBoxEditor(mountOptionsEditor, object.getMountOptions());
        styleTextBoxEditor(retransmissionsEditor, object.getRetransmissions());
        styleTextBoxEditor(versionEditorLabel, object.getVersion());

        versionEditorLabel.setVisible(!object.getVersion().getIsChangable() && object.getVersion().getIsAvailable());
        setElementVisibility(versionEditor, object.getVersion().getIsChangable() && object.getVersion().getIsAvailable());
        setElementVisibility(versionLabel, object.getVersion().getIsAvailable());
        setElementVisibility(retransmissionsLabel, object.getRetransmissions().getIsAvailable());
        setElementVisibility(timeoutLabel, object.getTimeout().getIsAvailable());
        setElementVisibility(mountOptionsLabel, object.getMountOptions().getIsAvailable());
    }

    @Override
    public NfsStorageModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {

        String pathEditorContent();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }

    /*
     * Makes a provided editor look like label (enabled, read-only textbox).
     */
    private void styleTextBoxEditor(EntityModelTextBoxOnlyEditor editor, EntityModel model) {

        if (!model.getIsChangable()) {

            editor.setEnabled(true);

            ValueBox<Object> valueBox = editor.asValueBox();
            valueBox.setReadOnly(true);
            valueBox.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        }
    }

    private void setElementVisibility(UIObject object, boolean value) {

        object.getElement().getStyle().setVisibility(value ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
    }
}
