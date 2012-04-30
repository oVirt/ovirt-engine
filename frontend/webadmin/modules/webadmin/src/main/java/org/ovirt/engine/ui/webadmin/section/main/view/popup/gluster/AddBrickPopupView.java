package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.UiCommandButton;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.EntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.EntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
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
    @Path(value = "replicaCount.entity")
    @WithElementId
    EntityModelTextBoxEditor replicaCountEditor;

    @UiField
    @Path(value = "stripeCount.entity")
    @WithElementId
    EntityModelTextBoxEditor stripeCountEditor;

    @UiField
    @Ignore
    @WithElementId
    Label availableBricksHeader;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> availableBricksTable;

    @UiField
    @Ignore
    @WithElementId
    Label selectedBricksHeader;

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> selectedBricksTable;

    @UiField
    @WithElementId
    UiCommandButton addBricksButton;

    @UiField
    @WithElementId
    UiCommandButton removeBricksButton;

    @UiField
    @WithElementId
    UiCommandButton addAllBricksButton;

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
    Label messageLabel;

    @Inject
    public AddBrickPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        availableBricksTable = new EntityModelCellTable<ListModel>(true);
        selectedBricksTable = new EntityModelCellTable<ListModel>(true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        initTableColumns(constants);
        initButtons();
        Driver.driver.initialize(this);
    }

    protected void initTableColumns(ApplicationConstants constants){
        // Table Entity Columns
        availableBricksTable.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel entityModel) {
                return ((GlusterBrickEntity) (entityModel.getEntity())).getServerName();
            }
        }, constants.serverBricks());

        availableBricksTable.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {

            @Override
            public String getValue(EntityModel entityModel) {
                return ((GlusterBrickEntity) (entityModel.getEntity())).getBrickDirectory();
            }
        }, constants.brickDirectoryBricks());

        selectedBricksTable.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel entityModel) {
                return ((GlusterBrickEntity) (entityModel.getEntity())).getServerName();
            }
        }, constants.serverBricks());

        selectedBricksTable.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {

            @Override
            public String getValue(EntityModel entityModel) {
                return ((GlusterBrickEntity) (entityModel.getEntity())).getBrickDirectory();
            }
        }, constants.brickDirectoryBricks());

    }

    private void initButtons()
    {
        addBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addBricksButton.getCommand().Execute();
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

        addAllBricksButton.addClickHandler(new ClickHandler() {

            @Override
            public void onClick(ClickEvent event) {
                addAllBricksButton.getCommand().Execute();
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
        if (availableBricksTable.getSelectionModel() instanceof MultiSelectionModel)
        {
            ((MultiSelectionModel) availableBricksTable.getSelectionModel()).clear();
        }

        if (selectedBricksTable.getSelectionModel() instanceof MultiSelectionModel)
        {
            ((MultiSelectionModel) selectedBricksTable.getSelectionModel()).clear();
        }
    }

    private void localize(ApplicationConstants constants) {
        replicaCountEditor.setLabel(constants.replicaCountVolume());
        stripeCountEditor.setLabel(constants.stripeCountVolume());
        availableBricksHeader.setText(constants.availableBricksHeaderLabel());
        selectedBricksHeader.setText(constants.selectedBricksHeaderLabel());
        addBricksButton.setLabel(constants.addBricksButtonLabel());
        removeBricksButton.setLabel(constants.removeBricksButtonLabel());
        addAllBricksButton.setLabel(constants.addAllBricksButtonLabel());
        removeAllBricksButton.setLabel(constants.removeAllBricksButtonLabel());
        moveBricksUpButton.setLabel(constants.moveBricksUpButtonLabel());
        moveBricksDownButton.setLabel(constants.moveBricksDownButtonLabel());
    }

    @Override
    public void edit(VolumeBrickModel object) {
        availableBricksTable.edit(object.getAvailableBricks());
        selectedBricksTable.edit(object.getSelectedBricks());
        Driver.driver.edit(object);

        addBricksButton.setCommand(object.getAddBricksCommand());
        removeBricksButton.setCommand(object.getRemoveBricksCommand());
        addAllBricksButton.setCommand(object.getAddAllBricksCommand());
        removeAllBricksButton.setCommand(object.getRemoveAllBricksCommand());

        moveBricksUpButton.setCommand(object.getMoveBricksUpCommand());
        moveBricksDownButton.setCommand(object.getMoveBricksDownCommand());
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    @Override
    public VolumeBrickModel flush() {
        selectedBricksTable.flush();
        return Driver.driver.flush();
    }

}
