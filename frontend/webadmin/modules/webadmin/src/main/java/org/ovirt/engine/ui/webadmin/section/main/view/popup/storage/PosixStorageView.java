package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxOnlyEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.PosixStorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.ClientGinjectorProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ValueBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PosixStorageView extends AbstractStorageView<PosixStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<PosixStorageModel, PosixStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, PosixStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<PosixStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

    @UiField
    @WithElementId
    @Path(value = "path.entity")
    StringEntityModelTextBoxOnlyEditor pathEditor;

    @UiField
    @Ignore
    Label pathLabel;

    @UiField
    @Ignore
    Label pathHintLabel;

    @UiField
    @WithElementId
    @Path(value = "vfsType.entity")
    StringEntityModelTextBoxOnlyEditor vfsTypeEditor;

    @UiField
    @Ignore
    Label vfsTypeLabel;

    @UiField
    @WithElementId
    @Path(value = "mountOptions.entity")
    StringEntityModelTextBoxOnlyEditor mountOptionsEditor;

    @UiField
    @Ignore
    Label mountOptionsLabel;

    @UiField
    Label message;

    @UiField
    Image nfsPosixAlertIcon;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public PosixStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize(ClientGinjectorProvider.getApplicationConstants());
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    void addStyles() {
        pathEditor.addContentWidgetContainerStyleName(style.pathEditorContent());
    }

    void localize(ApplicationConstants constants) {

        pathLabel.setText(constants.storagePopupPosixPathLabel());
        pathHintLabel.setText(constants.storagePopupPosixPathHintLabel());
        vfsTypeLabel.setText(constants.storagePopupVfsTypeLabel());
        mountOptionsLabel.setText(constants.storagePopupMountOptionsLabel());
        nfsPosixAlertIcon.setTitle(constants.storagePopupPosixNfsWarningLabel());
    }

    private IEventListener vfsTypeListener = new IEventListener<EventArgs>() {
        @Override
        public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
            EntityModel<String> posixStorageModel = (EntityModel<String>) sender;
            boolean isNfs =
                    posixStorageModel.getEntity() != null ? posixStorageModel.getEntity().toLowerCase().equals("nfs") : false; //$NON-NLS-1$
            nfsPosixAlertIcon.setVisible(isNfs);
        }
    };

    @Override
    public void edit(PosixStorageModel object) {
        driver.edit(object);

        pathHintLabel.setVisible(object.getPath().getIsAvailable() && object.getPath().getIsChangable());

        StyleTextBoxEditor(pathEditor, object.getPath());
        StyleTextBoxEditor(vfsTypeEditor, object.getVfsType());
        StyleTextBoxEditor(mountOptionsEditor, object.getMountOptions());

        if (!object.getVfsType().getEntityChangedEvent().getListeners().contains(vfsTypeListener)) {
            object.getVfsType().getEntityChangedEvent().addListener(vfsTypeListener);
        }
    }

    /*
    Makes a provided editor look like label (enabled, read-only textbox).
     */
    private void StyleTextBoxEditor(StringEntityModelTextBoxOnlyEditor editor, EntityModel model) {

        if (!model.getIsChangable()) {

            editor.setEnabled(true);

            ValueBox<String> valueBox = editor.asValueBox();
            valueBox.setReadOnly(true);
            valueBox.getElement().getStyle().setBorderWidth(0, Style.Unit.PX);
        }

        editor.asValueBox().setTitle(model.getTitle());
    }

    @Override
    public PosixStorageModel flush() {
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {

        String pathEditorContent();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }

}
