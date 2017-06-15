package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import org.gwtbootstrap3.client.ui.Icon;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.storage.AbstractStorageView;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.PosixStorageModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class PosixStorageView extends AbstractStorageView<PosixStorageModel> {

    interface Driver extends UiCommonEditorDriver<PosixStorageModel, PosixStorageView> {
    }

    interface ViewUiBinder extends UiBinder<Widget, PosixStorageView> {

        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<PosixStorageView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

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
    Icon nfsPosixAlertIcon;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public PosixStorageView() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
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

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focus() {
        pathEditor.setFocus(true);
    }

}
