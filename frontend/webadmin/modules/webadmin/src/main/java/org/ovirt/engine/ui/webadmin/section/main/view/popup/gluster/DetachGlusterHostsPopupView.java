package org.ovirt.engine.ui.webadmin.section.main.view.popup.gluster;

import org.gwtbootstrap3.client.ui.Alert;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.table.column.AbstractEntityModelTextColumn;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.gluster.DetachGlusterHostsModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.gluster.DetachGlusterHostsPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class DetachGlusterHostsPopupView extends AbstractModelBoundPopupView<DetachGlusterHostsModel> implements DetachGlusterHostsPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<DetachGlusterHostsModel, DetachGlusterHostsPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, DetachGlusterHostsPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<DetachGlusterHostsPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<ListModel<EntityModel<String>>> hostsTable;

    @UiField(provided = true)
    @Path(value = "force.entity")
    @WithElementId
    EntityModelCheckBoxEditor forceEditor;

    @UiField
    @Ignore
    Alert message;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public DetachGlusterHostsPopupView(EventBus eventBus) {
        super(eventBus);
        hostsTable = new EntityModelCellTable<>(true, false, true);
        forceEditor = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        initTableColumns();
        driver.initialize(this);
    }

    protected void initTableColumns(){
        // Table Entity Columns
        hostsTable.addColumn(new AbstractEntityModelTextColumn<String>() {
            @Override
            public String getText(String hostAddress) {
                return hostAddress;
            }
        }, constants.detachGlusterHostsHostAddress());
    }

    @Override
    public void edit(DetachGlusterHostsModel object) {
        hostsTable.asEditor().edit(object.getHosts());
        driver.edit(object);
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        this.message.setText(message);
    }

    @Override
    public DetachGlusterHostsModel flush() {
        hostsTable.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
