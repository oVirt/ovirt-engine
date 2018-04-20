package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.inject.Inject;

public class AddBrickPopupView extends AbstractModelBoundPopupView<VolumeBrickModel> implements AddBrickPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<VolumeBrickModel, AddBrickPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AddBrickPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AddBrickPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "volumeType.entity")
    @WithElementId
    EntityModelLabelEditor<GlusterVolumeType> volumeTypeEditor;

    @UiField
    @Path(value = "replicaCount.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor replicaCountEditor;

    @UiField
    @Path(value = "stripeCount.entity")
    @WithElementId
    IntegerEntityModelTextBoxEditor stripeCountEditor;

    @UiField(provided = true)
    @Path(value = "force.entity")
    @WithElementId
    EntityModelCheckBoxEditor forceEditor;

    @UiField(provided = true)
    @Path(value = "servers.selectedItem")
    @WithElementId
    ListModelListBoxEditor<VDS> serverEditor;

    @UiField(provided = true)
    @Path(value = "showBricksList.entity")
    @WithElementId
    EntityModelCheckBoxEditor showBricksListEditor;

    @UiField
    @Path(value = "brickDirectory.entity")
    @WithElementId
    StringEntityModelTextBoxEditor exportDirEditor;

    @UiField
    @Path(value = "bricksFromServer.selectedItem")
    @WithElementId
    ListModelListBoxEditor<String> bricksFromServerList;

    @UiField
    @WithElementId
    UiCommandButton addBrickButton;

    @UiField
    @Ignore
    @WithElementId
    Label bricksHeader;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> bricksTable;

    @UiField
    @WithElementId
    UiCommandButton removeBricksButton;

    @UiField
    @WithElementId
    UiCommandButton removeAllBricksButton;

    @UiField
    @WithElementId
    UiCommandButton moveBricksUpButton;

    @UiField
    @WithElementId
    UiCommandButton moveBricksDownButton;

    @UiField
    @Ignore
    Alert info;

    @UiField
    @Ignore
    Alert forceWarning;

    @UiField
    @Ignore
    Alert message;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public AddBrickPopupView(EventBus eventBus) {
        super(eventBus);
        bricksTable = new EntityModelCellTable<>(true);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTableColumns();
        initButtons();
        driver.initialize(this);
    }

    private void initEditors() {
        volumeTypeEditor = new EntityModelLabelEditor<>(new EnumRenderer<GlusterVolumeType>());
        forceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        showBricksListEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        serverEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<VDS>() {
            @Override
            public String renderNullSafe(VDS vds) {
                return vds.getHostName();
            }
        });

    }

    protected void initTableColumns(){
        // Table Entity Columns
        bricksTable.addColumn(new AbstractEntityModelTextColumn<GlusterBrickEntity>() {
            @Override
            public String getText(GlusterBrickEntity entity) {
                return entity.getServerName();
            }
        }, constants.serverBricks());

        bricksTable.addColumn(new AbstractEntityModelTextColumn<GlusterBrickEntity>() {
            @Override
            public String getText(GlusterBrickEntity entity) {
                return entity.getBrickDirectory();
            }
        }, constants.brickDirectoryBricks());
    }

    private void initButtons() {
        addBrickButton.addClickHandler(event -> {
            addBrickButton.getCommand().execute();
            clearSelections();
        });


        removeBricksButton.addClickHandler(event -> {
            removeBricksButton.getCommand().execute();
            clearSelections();
            bricksTable.flush();
            bricksTable.redraw();
        });


        removeAllBricksButton.addClickHandler(event -> {
            removeAllBricksButton.getCommand().execute();
            clearSelections();
            bricksTable.flush();
            bricksTable.redraw();
        });

        moveBricksUpButton.addClickHandler(event -> moveBricksUpButton.getCommand().execute());

        moveBricksDownButton.addClickHandler(event -> moveBricksDownButton.getCommand().execute());
    }

    private void clearSelections() {
        if (bricksTable.getSelectionModel() instanceof MultiSelectionModel) {
            ((MultiSelectionModel) bricksTable.getSelectionModel()).clear();
        }
    }

    @Override
    public void edit(final VolumeBrickModel object) {
        bricksTable.asEditor().edit(object.getBricks());
        driver.edit(object);

        addBrickButton.setCommand(object.getAddBrickCommand());
        removeBricksButton.setCommand(object.getRemoveBricksCommand());
        removeAllBricksButton.setCommand(object.getRemoveAllBricksCommand());

        moveBricksUpButton.setCommand(object.getMoveBricksUpCommand());
        moveBricksDownButton.setCommand(object.getMoveBricksDownCommand());

        GlusterVolumeType volumeType = object.getVolumeType().getEntity();
        if (volumeType == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
            info.setVisible(true);
            info.setText(constants.distributedReplicateVolumeBrickInfoLabel());
        } else if (volumeType == GlusterVolumeType.DISTRIBUTED_STRIPE) {
            info.setVisible(true);
            info.setText(constants.distributedStripeVolumeBrickInfoLabel());
        } else {
            info.setVisible(false);
            info.setText(null);
        }

        forceWarning.setVisible(object.getForce().getEntity());

        object.getForce().getEntityChangedEvent().addListener((ev, sender, args) -> forceWarning.setVisible(object.getForce().getEntity()));
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
        this.message.setVisible(false);
        if (StringHelper.isNotNullOrEmpty(message)) {
            this.message.setVisible(true);
        }
    }

    @Override
    public VolumeBrickModel flush() {
        bricksTable.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
