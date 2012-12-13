package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.core.common.businessentities.gluster.GlusterVolumeType;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelLabelEditor;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.parser.EntityModelParser;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.VolumeBrickModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.AddBrickPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.inject.Inject;

public class AddBrickPopupView extends AbstractModelBoundPopupView<VolumeBrickModel> implements AddBrickPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<VolumeBrickModel, AddBrickPopupView> {
        Driver driver = GWT.create(Driver.class);
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
    EntityModelLabelEditor volumeTypeEditor;

    @UiField
    @Path(value = "replicaCount.entity")
    @WithElementId
    EntityModelTextBoxEditor replicaCountEditor;

    @UiField
    @Path(value = "stripeCount.entity")
    @WithElementId
    EntityModelTextBoxEditor stripeCountEditor;

    @UiField(provided = true)
    @Path(value = "servers.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> serverEditor;

    @UiField
    @Path(value = "brickDirectory.entity")
    @WithElementId
    EntityModelTextBoxEditor exportDirEditor;

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
    Label messageLabel;

    ApplicationConstants constants;

    @Inject
    public AddBrickPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;
        bricksTable = new EntityModelCellTable<ListModel>(true);
        initListBoxEditors();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addStyles();
        initTableColumns(constants);
        initButtons();
        Driver.driver.initialize(this);
    }

    private void initListBoxEditors() {
        volumeTypeEditor = new EntityModelLabelEditor(new EnumRenderer(), new EntityModelParser());
        serverEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDS) object).gethost_name();
            }
        });

    }

    protected void addStyles()
    {
        volumeTypeEditor.addContentWidgetStyleName(style.editorContentWidget());
        replicaCountEditor.addContentWidgetStyleName(style.editorContentWidget());
        stripeCountEditor.addContentWidgetStyleName(style.editorContentWidget());
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

    private void initButtons()
    {
        addBrickButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addBrickButton.getCommand().Execute();
                clearSelections();
            }
        });


        removeBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                removeBricksButton.getCommand().Execute();
                clearSelections();
            }
        });


        removeAllBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                removeAllBricksButton.getCommand().Execute();
                clearSelections();
            }
        });

        moveBricksUpButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                moveBricksUpButton.getCommand().Execute();
            }
        });

        moveBricksDownButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                moveBricksDownButton.getCommand().Execute();
            }
        });
    }

    private void clearSelections()
    {
        if (bricksTable.getSelectionModel() instanceof MultiSelectionModel)
        {
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
    }

    @Override
    public void edit(VolumeBrickModel object) {
        bricksTable.edit(object.getBricks());
        Driver.driver.edit(object);

        addBrickButton.setCommand(object.getAddBrickCommand());
        removeBricksButton.setCommand(object.getRemoveBricksCommand());
        removeAllBricksButton.setCommand(object.getRemoveAllBricksCommand());

        moveBricksUpButton.setCommand(object.getMoveBricksUpCommand());
        moveBricksDownButton.setCommand(object.getMoveBricksDownCommand());

        GlusterVolumeType volumeType = (GlusterVolumeType) object.getVolumeType().getEntity();
        if (volumeType == GlusterVolumeType.DISTRIBUTED_REPLICATE) {
            infoLabel.setText(constants.distributedReplicateVolumeBrickInfoLabel());
        }
        else if (volumeType == GlusterVolumeType.DISTRIBUTED_STRIPE) {
            infoLabel.setText(constants.distributedStripeVolumeBrickInfoLabel());
        }
        else {
            infoLabel.setText(null);
        }
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    @Override
    public VolumeBrickModel flush() {
        bricksTable.flush();
        return Driver.driver.flush();
    }

    interface WidgetStyle extends CssResource {
        String editorContentWidget();
    }

}
