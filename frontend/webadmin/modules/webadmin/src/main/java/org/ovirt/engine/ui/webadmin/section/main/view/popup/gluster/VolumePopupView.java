package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.storage_pool;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
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

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<Object> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    ListModelListBoxEditor<Object> clusterEditor;

    @UiField
    @Path(value = "typeList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> typeListEditor;

    @UiField
    @Path(value = "replicaCount.entity")
    @WithElementId
    EntityModelTextBoxEditor replicaCountEditor;

    @UiField
    @Path(value = "stripeCount.entity")
    @WithElementId
    EntityModelTextBoxEditor stripeCountEditor;

    @UiField(provided = true)
    @Path(value = "tcpTransportType.entity")
    @WithElementId
    EntityModelCheckBoxEditor tcpTransportTypeEditor;

    @UiField(provided = true)
    @Path(value = "rdmaTransportType.entity")
    @WithElementId
    EntityModelCheckBoxEditor rdmaTransportTypeEditor;

    @UiField
    @WithElementId
    UiCommandButton addBricksButton;

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
    @Path(value = "allowAccess.entity")
    @WithElementId
    EntityModelTextBoxEditor allowAccessEditor;

    @Inject
    public VolumePopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        initRadioButtonEditors();
        initCheckboxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        initAddBricksButton();
        Driver.driver.initialize(this);
    }

    private void initCheckboxEditors() {
        tcpTransportTypeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        rdmaTransportTypeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initRadioButtonEditors() {

    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((storage_pool) object).getname();
            }
        });

        clusterEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });
    }

    private void initAddBricksButton() {
        addBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addBricksButton.getCommand().Execute();
            }
        });
    }

    private void localize(ApplicationConstants constants) {
        dataCenterEditor.setLabel("Data Center");
        clusterEditor.setLabel("Volume Cluster");
        nameEditor.setLabel(constants.clusterPopupNameLabel());
        typeListEditor.setLabel("Type");
        replicaCountEditor.setLabel("Replica Count");
        stripeCountEditor.setLabel("Stripe Count");
        tcpTransportTypeEditor.setLabel("TCP");
        rdmaTransportTypeEditor.setLabel("RDMA");
        addBricksButton.setLabel("Add Bricks");
        gluster_accecssProtocolEditor.setLabel("Gluster");
        nfs_accecssProtocolEditor.setLabel("NFS");
        cifs_accecssProtocolEditor.setLabel("CIFS");
        allowAccessEditor.setLabel("Allow Access From");

    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VolumeModel object) {
        Driver.driver.edit(object);
        addBricksButton.setCommand(object.getAddBricksCommand());
    }

    @Override
    public VolumeModel flush() {
        return Driver.driver.flush();
    }
}
