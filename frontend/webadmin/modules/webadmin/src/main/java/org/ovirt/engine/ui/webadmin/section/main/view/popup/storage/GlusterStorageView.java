package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.storage.GlusterStorageModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class GlusterStorageView extends AbstractStorageView<GlusterStorageModel> {

    interface Driver extends SimpleBeanEditorDriver<GlusterStorageModel, GlusterStorageView> {

        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, GlusterStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "path.entity")
    @WithElementId("path")
    StringEntityModelTextBoxEditor pathEditor;

    @UiField
    @Ignore
    Label pathExampleLabel;

    @UiField
    @Path(value = "vfsType.entity")
    @WithElementId("vfsType")
    StringEntityModelTextBoxEditor vfsTypeEditor;

    @UiField
    @Path(value = "mountOptions.entity")
    @WithElementId("mountOptions")
    StringEntityModelTextBoxEditor mountOptionsEditor;

    @UiField
    @Path(value = "configurationMessage")
    Label message;


    public GlusterStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
        pathEditor.addContentWidgetContainerStyleName(style.pathEditorContent());
        vfsTypeEditor.addContentWidgetContainerStyleName(style.vfsTypeTextBoxEditor());
        mountOptionsEditor.addContentWidgetContainerStyleName(style.mountOptionsTextBoxEditor());
    }

    void localize() {
        pathEditor.setLabel(constants.storagePopupPosixPathLabel());
        pathExampleLabel.setText(constants.storagePopupGlusterPathExampleLabel());
        vfsTypeEditor.setLabel(constants.storagePopupVfsTypeLabel());
        mountOptionsEditor.setLabel(constants.storagePopupMountOptionsLabel());
    }

    @Override
    public void edit(GlusterStorageModel object) {
        Driver.driver.edit(object);

        pathExampleLabel.setVisible(object.getPath().getIsAvailable() && object.getPath().getIsChangable());

    }

    @Override
    public GlusterStorageModel flush() {
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {

        String pathEditorContent();

        String vfsTypeTextBoxEditor();

        String mountOptionsTextBoxEditor();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }
}
