package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.Cluster;
import org.ovirt.engine.core.common.businessentities.StoragePool;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
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
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.VolumePopupPresenterWidget;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class VolumePopupView extends AbstractModelBoundPopupView<VolumeModel> implements VolumePopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VolumeModel, VolumePopupView> {
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

    @UiField
    @Ignore
    Label transportTypesLabel;

    @UiField(provided = true)
    @Path(value = "tcpTransportType.entity")
    @WithElementId
    EntityModelCheckBoxEditor tcpTransportTypeEditor;

    @UiField(provided = true)
    @Path(value = "rdmaTransportType.entity")
    @WithElementId
    EntityModelCheckBoxEditor rdmaTransportTypeEditor;

    @UiField
    @Ignore
    Label bricksLabel;

    @UiField
    @WithElementId
    UiCommandButton addBricksButton;

    @UiField
    @Ignore
    @WithElementId
    Label bricksCountEditor;

    @UiField
    @Ignore
    Label accessProtocolsLabel;

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
    StringEntityModelTextBoxEditor allowAccessEditor;

    @UiField
    @Ignore
    Label allowAccessLabel;

    @UiField
    @Ignore
    Label messageLabel;

    @UiField
    @Ignore
    Label virtStoreOptimiseWarningLabel;

    @UiField
    @Path(value = "optimizeForVirtStore.entity")
    @WithElementId
    EntityModelCheckBoxEditor optimizeForVirtStoreEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public VolumePopupView(EventBus eventBus) {
        super(eventBus);
        initListBoxEditors();
        initCheckboxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize();
        setVisibilities();
        initAddBricksButton();
        initBricksCountLabele();
        driver.initialize(this);
    }

    private void setVisibilities() {
        virtStoreOptimiseWarningLabel.setVisible(false);
    }

    private void initCheckboxEditors() {
        tcpTransportTypeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        rdmaTransportTypeEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
    }

    private void initListBoxEditors() {
        dataCenterEditor = new ListModelListBoxEditor<>(new NameRenderer<StoragePool>());

        clusterEditor = new ListModelListBoxEditor<>(new NameRenderer<Cluster>());

        typeListEditor = new ListModelListBoxEditor<>(new EnumRenderer<GlusterVolumeType>());
    }

    private void initAddBricksButton() {
        addBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addBricksButton.getCommand().execute();
            }
        });
    }

    private void initBricksCountLabele() {
        bricksCountEditor.setText(ConstantsManager.getInstance().getMessages().noOfBricksSelected(0));
    }

    private void localize() {
        dataCenterEditor.setLabel(constants.dataCenterVolume());
        clusterEditor.setLabel(constants.volumeClusterVolume());
        nameEditor.setLabel(constants.clusterPopupNameLabel());
        typeListEditor.setLabel(constants.typeVolume());
        replicaCountEditor.setLabel(constants.replicaCountVolume());
        stripeCountEditor.setLabel(constants.stripeCountVolume());
        transportTypesLabel.setText(constants.transportTypeVolume());
        tcpTransportTypeEditor.setLabel(constants.tcpVolume());
        rdmaTransportTypeEditor.setLabel(constants.rdmaVolume());
        bricksLabel.setText(constants.bricksVolume());
        addBricksButton.setLabel(constants.addBricksVolume());
        accessProtocolsLabel.setText(constants.accessProtocolsVolume());
        gluster_accecssProtocolEditor.setLabel(constants.glusterVolume());
        nfs_accecssProtocolEditor.setLabel(constants.nfsVolume());
        cifs_accecssProtocolEditor.setLabel(constants.cifsVolume());
        allowAccessEditor.setLabel(constants.allowAccessFromVolume());
        allowAccessLabel.setText(constants.allowAccessFromLabelVolume());
        optimizeForVirtStoreEditor.setLabel(constants.optimizeForVirtStoreVolume());
        virtStoreOptimiseWarningLabel.setText(constants.newVolumeOptimiseForVirtStoreWarning());
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

    @Override
    public void edit(final VolumeModel object) {
        driver.edit(object);
        addBricksButton.setCommand(object.getAddBricksCommand());

        object.getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                VolumeModel model = (VolumeModel) sender;
                if ("Bricks".equals(args.propertyName)) { //$NON-NLS-1$
                    bricksCountEditor.setText(ConstantsManager.getInstance()
                            .getMessages()
                            .noOfBricksSelected(model.getBricks().getSelectedItems() == null ? 0 : model.getBricks()
                                    .getSelectedItems()
                                    .size()));
                }
            }
        });
        object.getOptimizeForVirtStore().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                virtStoreOptimiseWarningLabel.setVisible(object.getOptimizeForVirtStore().getEntity() && object.getReplicaCount().getEntity() != 3);
            }
        });
        object.getReplicaCount().getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {
            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                virtStoreOptimiseWarningLabel.setVisible(object.getOptimizeForVirtStore().getEntity() && object.getReplicaCount().getEntity() != 3);
            }
        });
    }

    @Override
    public VolumeModel flush() {
        return driver.flush();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

}
