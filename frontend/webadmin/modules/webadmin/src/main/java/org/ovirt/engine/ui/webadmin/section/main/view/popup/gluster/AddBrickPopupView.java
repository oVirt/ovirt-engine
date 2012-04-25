package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.ovirt.engine.core.common.businessentities.gluster.GlusterBrickEntity;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
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
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
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

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel> table;

    @UiField
    @Ignore
    Label messageLabel;

    @Inject
    public AddBrickPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        table = new EntityModelCellTable<ListModel>(true);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        initTableColumns(constants);
        Driver.driver.initialize(this);
    }

    protected void initTableColumns(ApplicationConstants constants){
        // Table Entity Columns
        table.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {
            @Override
            public String getValue(EntityModel entityModel) {
                return ((GlusterBrickEntity) (entityModel.getEntity())).getServerName();
            }
        }, constants.serverBricks());

        table.addEntityModelColumn(new EntityModelTextColumn<EntityModel>() {

            @Override
            public String getValue(EntityModel entityModel) {
                return ((GlusterBrickEntity) (entityModel.getEntity())).getBrickDirectory();
            }
        }, constants.brickDirectoryBricks());

    }

    private void localize(ApplicationConstants constants) {
        replicaCountEditor.setLabel(constants.replicaCountVolume());
        stripeCountEditor.setLabel(constants.stripeCountVolume());
    }

    @Override
    public void edit(VolumeBrickModel object) {
        table.edit(object.getBricks());
        Driver.driver.edit(object);
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        // Hide table in case of message
        if (message != null && message.length() > 0) {
            table.setVisible(false);
        }
        messageLabel.setText(message);
    }

    @Override
    public VolumeBrickModel flush() {
        table.flush();
        return Driver.driver.flush();
    }

}
