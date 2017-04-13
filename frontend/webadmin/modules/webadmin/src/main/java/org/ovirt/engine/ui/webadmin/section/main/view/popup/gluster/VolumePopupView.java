package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NameRenderer;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VolumePopupView extends AbstractModelBoundPopupView<VolumeModel> implements VolumePopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<VolumeModel, VolumePopupView> {
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
    StringEntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "dataCenter.selectedItem")
    @WithElementId("dataCenter")
    ListModelListBoxEditor<StoragePool> dataCenterEditor;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId("cluster")
    ListModelListBoxEditor<Cluster> clusterEditor;

    @UiField(provided = true)
    @Path(value = "typeList.selectedItem")
    @WithElementId
    ListModelListBoxEditor<GlusterVolumeType> typeListEditor;

    @UiField
    @Path(value = "replicaCount.entity")
    @WithElementId
    IntegerEntityModelLabelEditor replicaCountEditor;

    @UiField
    @Path(value = "stripeCount.entity")
    @WithElementId
    IntegerEntityModelLabelEditor stripeCountEditor;

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
    @Ignore
    @WithElementId
    Label bricksCountEditor;

    @UiField (provided = true)
    @Path(value = "gluster_accecssProtocol.entity")
    @WithElementId
    EntityModelCheckBoxEditor glusterAccessProtocolEditor;

    @UiField (provided = true)
    @Path(value = "nfs_accecssProtocol.entity")
    @WithElementId
    EntityModelCheckBoxEditor nfsAccessProtocolEditor;

    @UiField (provided = true)
    @Path(value = "cifs_accecssProtocol.entity")
    @WithElementId
    EntityModelCheckBoxEditor cifsAccessProtocolEditor;

    @UiField
    @Path(value = "allowAccess.entity")
    @WithElementId
    StringEntityModelTextBoxEditor allowAccessEditor;

    @UiField
    @Ignore
    Alert message;

    @UiField
    @Ignore
    Alert virtStoreOptimiseWarning;

    @UiField (provided = true)
    @Path(value = "optimizeForVirtStore.entity")
    @WithElementId
    EntityModelCheckBoxEditor optimizeForVirtStoreEditor;

    @UiField (provided = true)
    @Path(value = "arbiterVolume.entity")
    @WithElementId
    EntityModelCheckBoxEditor arbiterVolumeEditor;

    private final Driver driver = GWT.create(Driver.class);

    @Inject
    public VolumePopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initCheckboxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initAddBricksButton();
        initBricksCountLabel();
        driver.initialize(this);
    }

    private void initCheckboxEditors() {
        tcpTransportTypeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        rdmaTransportTypeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        arbiterVolumeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        glusterAccessProtocolEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        nfsAccessProtocolEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        cifsAccessProtocolEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        optimizeForVirtStoreEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());

        clusterEditor = new ListModelListBoxEditor<>(new NameRenderer<Cluster>());

        typeListEditor = new ListModelListBoxEditor<>(new EnumRenderer<GlusterVolumeType>());
    }

    private void initAddBricksButton() {
        addBricksButton.addClickHandler(event -> addBricksButton.getCommand().execute());
    }

    private void initBricksCountLabel() {
        bricksCountEditor.setText(ConstantsManager.getInstance().getMessages().noOfBricksSelected(0));
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VolumeModel object) {
        driver.edit(object);
        addBricksButton.setCommand(object.getAddBricksCommand());

        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            VolumeModel model = (VolumeModel) sender;
            if ("Bricks".equals(args.propertyName)) { //$NON-NLS-1$
                bricksCountEditor.setText(ConstantsManager.getInstance()
                        .getMessages()
                        .noOfBricksSelected(model.getBricks().getSelectedItems() == null ? 0 : model.getBricks()
                                .getSelectedItems()
                                .size()));
            }
        });
        object.getOptimizeForVirtStore().getEntityChangedEvent().addListener((ev, sender, args) ->
                virtStoreOptimiseWarning.setVisible(object.getOptimizeForVirtStore().getEntity() && object.getReplicaCount().getEntity() != 3));
        object.getReplicaCount().getEntityChangedEvent().addListener((ev, sender, args) ->
                virtStoreOptimiseWarning.setVisible(object.getOptimizeForVirtStore().getEntity() && object.getReplicaCount().getEntity() != 3));
    }

    @Override
    public VolumeModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
        this.message.setVisible(message != null);
    }

}
