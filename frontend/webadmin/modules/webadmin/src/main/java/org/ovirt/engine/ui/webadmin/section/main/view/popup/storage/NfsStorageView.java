package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.NfsStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.dom.client.TableElement;
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

public class NfsStorageView extends AbstractStorageView<NfsStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<NfsStorageModel, NfsStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, NfsStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<NfsStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @WithElementId
    @Path(value = "path.entity")
    EntityModelTextBoxOnlyEditor pathEditor;

    @UiField
    @Ignore
    Label pathLabel;

    @UiField
    @Ignore
    Label pathHintLabel;

    @UiField
    @Ignore
    AdvancedParametersExpander expander;

    @UiField
    @Ignore
    Label warningLabel;

    @UiField(provided = true)
    @Path(value = "override.entity")
    @WithElementId("overrideEditor")
    EntityModelCheckBoxEditor overrideEditor;

    @UiField
    @Ignore
    TableElement expanderContent;

    @UiField(provided = true)
    @WithElementId
    @Path(value = "version.selectedItem")
    ListModelListBoxOnlyEditor<Object> versionEditor;

    @UiField
    @Ignore
    EntityModelTextBoxOnlyEditor versionReadOnlyEditor;

    @UiField
    @Ignore
    Label versionLabel;

    @UiField
    @WithElementId
    @Path(value = "retransmissions.entity")
    EntityModelTextBoxOnlyEditor retransmissionsEditor;

    @UiField
    @Ignore
    Label retransmissionsLabel;

    @UiField
    @WithElementId
    @Path(value = "timeout.entity")
    EntityModelTextBoxOnlyEditor timeoutEditor;

    @UiField
    @Ignore
    Label timeoutLabel;

    @UiField
    Label message;

    private final Driver driver = GWT.create(Driver.class);

    protected static final CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);
    protected static final CommonApplicationResources resources = GWT.create(CommonApplicationResources.class);
    protected static final CommonApplicationTemplates templates = GWT.create(CommonApplicationTemplates.class);

    @Inject
    public NfsStorageView() {
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.instance().getApplicationConstants());
        initExpander();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    private void initExpander() {
        expander.initWithContent(expanderContent);
    }

    void addStyles() {
        pathEditor.addContentWidgetStyleName(style.pathEditorContent());

        expanderContent.setClassName(style.expanderContent());

    }

    void initEditors() {

        versionEditor = new ListModelListBoxOnlyEditor<Object>(new AbstractRenderer<Object>() {
            @Override
            public String render(Object object) {

                EntityModel model = (EntityModel) object;
                return model.getTitle();
            }
        });

        overrideEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    void localize(ApplicationConstants constants) {

        pathLabel.setText(constants.storagePopupNfsPathLabel());
        pathHintLabel.setText(constants.storagePopupNfsPathHintLabel());
        warningLabel.setText(constants.advancedOptionsLabel());
        overrideEditor.setLabel(constants.storagePopupNfsOverrideLabel());
        versionLabel.setText(constants.storagePopupNfsVersionLabel());
        retransmissionsLabel.setText(constants.storagePopupNfsRetransmissionsLabel());
        timeoutLabel.setText(constants.storagePopupNfsTimeoutLabel());
    }

    @Override
    public void edit(NfsStorageModel object) {
        driver.edit(object);

        EntityModel version = (EntityModel) object.getVersion().getSelectedItem();
        versionReadOnlyEditor.asValueBox().setValue(version != null ? version.getTitle() : null);

        pathHintLabel.setVisible(object.getPath().getIsAvailable() && !object.getIsEditMode());

        styleTextBoxEditor(pathEditor, !object.getIsEditMode());
        styleTextBoxEditor(timeoutEditor, !object.getIsEditMode());
        styleTextBoxEditor(retransmissionsEditor, !object.getIsEditMode());
        styleTextBoxEditor(versionReadOnlyEditor, !object.getIsEditMode());

        setElementVisibility(versionEditor, !object.getIsEditMode() && object.getVersion().getIsAvailable());
        setElementVisibility(versionReadOnlyEditor, object.getIsEditMode() || !object.getVersion().getIsAvailable());
        setElementVisibility(versionLabel, object.getVersion().getIsAvailable());
        setElementVisibility(retransmissionsLabel, object.getRetransmissions().getIsAvailable());
        setElementVisibility(timeoutLabel, object.getTimeout().getIsAvailable());

        // When all advanced fields are unavailable - hide the expander.
        boolean anyField = object.getVersion().getIsAvailable()
            || object.getRetransmissions().getIsAvailable()
            || object.getTimeout().getIsAvailable();

        expander.getElement().getStyle().setVisibility(anyField ? Style.Visibility.VISIBLE : Style.Visibility.HIDDEN);
    }

    @Override
    public NfsStorageModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {

        String pathEditorContent();

        String expanderContent();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }

    /*
     * Makes a provided editor look like label (enabled, read-only textbox).
     */
    private void styleTextBoxEditor(EntityModelTextBoxOnlyEditor editor, boolean enabled) {

        if (!enabled) {

            editor.setEnabled(true);

            ValueBox<Object> valueBox = editor.asValueBox();
            valueBox.setReadOnly(true);
            valueBox.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        }
    }

    private void setElementVisibility(UIObject object, boolean value) {

        object.getElement().getStyle().setDisplay(value ? Style.Display.BLOCK : Style.Display.NONE);
    }

}
