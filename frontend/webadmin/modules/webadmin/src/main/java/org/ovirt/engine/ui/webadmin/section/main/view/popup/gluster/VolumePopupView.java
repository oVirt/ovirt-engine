package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VolumePopupView extends AbstractModelBoundPopupView<VolumeModel> implements VolumePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VolumeModel, VolumePopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VolumePopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VolumePopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Path(value = "name.entity")
    @WithElementId
    EntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "typeList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> typeListEditor;

    @UiField
    @Path(value = "bricks.entity")
    @WithElementId
    EntityModelTextBoxEditor bricksEditor;

    @UiField
    @Path(value = "gluster_accecssProtocol.entity")
    @WithElementId
    EntityModelCheckBoxEditor gluster_accecssProtocolEditor;

    @UiField
    @Path(value = "nfs_accecssProtocol.entity")
    @WithElementId
    EntityModelCheckBoxEditor nfs_accecssProtocolEditor;

    @UiField
    @Path(value = "cifs_accecssProtocol.entity")
    @WithElementId
    EntityModelCheckBoxEditor cifs_accecssProtocolEditor;

    @UiField
    @Path(value = "users.entity")
    @WithElementId
    EntityModelTextBoxEditor usersEditor;

    @UiField
    @Path(value = "allowAccess.entity")
    @WithElementId
    EntityModelTextBoxEditor allowAccessEditor;

    @Inject
    public VolumePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initRadioButtonEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        Driver.driver.initialize(this);
    }

    private void initRadioButtonEditors() {

    }

    private void initListBoxEditors() {
        // typeListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
        // @Override
        // public String renderNullSafe(Object object) {
        // return ((VOLUME_TYPE) object).getname();
        // }
        // });
    }

    private void localize(ApplicationConstants constants) {
        nameEditor.setLabel(constants.clusterPopupNameLabel());
        typeListEditor.setLabel("Type");
        bricksEditor.setLabel("Bricks");
        gluster_accecssProtocolEditor.setLabel("Gluster");
        nfs_accecssProtocolEditor.setLabel("NFS");
        cifs_accecssProtocolEditor.setLabel("CIFS");
        usersEditor.setLabel("CIFS Users");
        allowAccessEditor.setLabel("Allow Access From");

    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VolumeModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public VolumeModel flush() {
        return Driver.driver.flush();
    }
}
