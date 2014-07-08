package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import java.text.ParseException;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
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
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.Parser;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.inject.Inject;

public class AddBrickPopupView extends AbstractModelBoundPopupView<VolumeBrickModel> implements AddBrickPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VolumeBrickModel, AddBrickPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, AddBrickPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<AddBrickPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    WidgetStyle style;

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

    @UiField
    @Path(value = "brickDirectory.entity")
    @WithElementId
    StringEntityModelTextBoxEditor exportDirEditor;

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
    Label infoLabel;

    @UiField
    @Ignore
    Label forceWarningLabel;

    @UiField
    @Ignore
    Label messageLabel;

    private final Driver driver = GWT.create(Driver.class);

    private final ApplicationConstants constants;

    @Inject
    public AddBrickPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;
        bricksTable = new EntityModelCellTable<ListModel>(true);
        initEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addStyles();
        initTableColumns(constants);
        initButtons();
        driver.initialize(this);
    }

    private void initEditors() {
        volumeTypeEditor = new EntityModelLabelEditor<GlusterVolumeType>(new EnumRenderer<GlusterVolumeType>(), new Parser<GlusterVolumeType>() {
            @Override
            public GlusterVolumeType parse(CharSequence text) throws ParseException {
                //Parser is not needed as its a read only field and value is nowhere used.
                return null;
            }
        });
        forceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        serverEditor = new ListModelListBoxEditor<VDS>(new NullSafeRenderer<VDS>() {
            @Override
            public String renderNullSafe(VDS vds) {
                return vds.getHostName();
            }
        });

    }

    protected void addStyles() {
        volumeTypeEditor.addContentWidgetStyleName(style.editorContentWidget());
        replicaCountEditor.addContentWidgetStyleName(style.editorContentWidget());
        stripeCountEditor.addContentWidgetStyleName(style.editorContentWidget());
        forceEditor.addContentWidgetStyleName(style.forceEditorWidget());
    }

    protected void initTableColumns(ApplicationConstants constants){
        // Table Entity Columns
        bricksTable.addEntityModelColumn(new EntityModelTextColumn<GlusterBrickEntity>() {
            @Override
            public String getText(GlusterBrickEntity entity) {
                return entity.getServerName();
            }
        }, constants.serverBricks());

        bricksTable.addEntityModelColumn(new EntityModelTextColumn<GlusterBrickEntity>() {
            @Override
            public String getText(GlusterBrickEntity entity) {
                return entity.getBrickDirectory();
            }
        }, constants.brickDirectoryBricks());
    }

    private void initButtons() {
        addBrickButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addBrickButton.getCommand().execute();
                clearSelections();
            }
        });


        removeBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                removeBricksButton.getCommand().execute();
                clearSelections();
                bricksTable.flush();
                bricksTable.redraw();
            }
        });


        removeAllBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                removeAllBricksButton.getCommand().execute();
                clearSelections();
                bricksTable.flush();
                bricksTable.redraw();
            }
        });

        moveBricksUpButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                moveBricksUpButton.getCommand().execute();
            }
        });

        moveBricksDownButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                moveBricksDownButton.getCommand().execute();
            }
        });
    }

    private void clearSelections() {
        if (bricksTable.getSelectionModel() instanceof MultiSelectionModel) {
            ((MultiSelectionModel) bricksTable.getSelectionModel()).clear();
        }
    }

    private void localize(ApplicationConstants constants) {
        volumeTypeEditor.setLabel(constants.volumeTypeVolume());
        replicaCountEditor.setLabel(constants.replicaCountVolume());
        stripeCountEditor.setLabel(constants.stripeCountVolume());
        bricksHeader.setText(constants.bricksHeaderLabel());
        serverEditor.setLabel(constants.serverBricks());
        exportDirEditor.setLabel(constants.brickDirectoryBricks());
        addBrickButton.setLabel(constants.addBricksButtonLabel());
        removeBricksButton.setLabel(constants.removeBricksButtonLabel());
        removeAllBricksButton.setLabel(constants.removeAllBricksButtonLabel());
        moveBricksUpButton.setLabel(constants.moveBricksUpButtonLabel());
        moveBricksDownButton.setLabel(constants.moveBricksDownButtonLabel());
        forceEditor.setLabel(constants.allowBricksInRootPartition());
        forceWarningLabel.setText(constants.allowBricksInRootPartitionWarning());
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
            infoLabel.setText(constants.distributedReplicateVolumeBrickInfoLabel());
        }
        else if (volumeType == GlusterVolumeType.DISTRIBUTED_STRIPE) {
            infoLabel.setText(constants.distributedStripeVolumeBrickInfoLabel());
        }
        else {
            infoLabel.setText(null);
        }

        forceWarningLabel.setVisible(object.getForce().getEntity());

        object.getForce().getEntityChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                forceWarningLabel.setVisible(object.getForce().getEntity());
            }
        });
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    @Override
    public VolumeBrickModel flush() {
        bricksTable.flush();
        return driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String editorContentWidget();

        String forceEditorWidget();
    }

}
