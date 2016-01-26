package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.PosixStorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
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
    Label message;

    @UiField
    Image nfsPosixAlertIcon;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public PosixStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        ViewIdHandler.idHandler.generateAndSetIds(this);
        addStyles();
        driver.initialize(this);
    }

    void addStyles() {
        pathEditor.addContentWidgetContainerStyleName(style.pathEditorContent());
        vfsTypeEditor.addContentWidgetContainerStyleName(style.vfsTypeTextBoxEditor());
        mountOptionsEditor.addContentWidgetContainerStyleName(style.mountOptionsTextBoxEditor());
    }

    void localize() {
        pathEditor.setLabel(constants.storagePopupPosixPathLabel());
        pathExampleLabel.setText(constants.storagePopupPosixPathExampleLabel());
        vfsTypeEditor.setLabel(constants.storagePopupVfsTypeLabel());
        mountOptionsEditor.setLabel(constants.storagePopupMountOptionsLabel());
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

        pathExampleLabel.setVisible(object.getPath().getIsAvailable() && object.getPath().getIsChangable());

        if (!object.getVfsType().getEntityChangedEvent().getListeners().contains(vfsTypeListener)) {
            object.getVfsType().getEntityChangedEvent().addListener(vfsTypeListener);
        }
    }

    @Override
    public PosixStorageModel flush() {
        return driver.flush();
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
