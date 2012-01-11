package org.ovirt.engine.ui.webadmin.section.main.view.popup.guide;

import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.core.common.businessentities.VDSGroup;
import org.ovirt.engine.core.common.businessentities.VDSStatus;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.hosts.MoveHost;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.webadmin.idhandler.WithElementId;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.guide.MoveHostPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.webadmin.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.webadmin.widget.table.column.EnumColumn;
import org.ovirt.engine.ui.webadmin.widget.table.column.TextColumnWithTooltip;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class MoveHostPopupView extends AbstractModelBoundPopupView<MoveHost> implements MoveHostPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<MoveHost, MoveHostPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, MoveHostPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<MoveHostPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField(provided = true)
    @Ignore
    @WithElementId
    EntityModelCellTable<MoveHost> table;

    @UiField(provided = true)
    @Path(value = "cluster.selectedItem")
    @WithElementId
    ListModelListBoxEditor<Object> clusterListEditor;

    @Inject
    public MoveHostPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initListBoxEditors();
        localize(constants);
        initTable();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);
    }

    private void localize(ApplicationConstants constants) {
        clusterListEditor.setLabel(constants.moveHostPopupClusterLabel());
    }

    private void initListBoxEditors() {
        clusterListEditor = new ListModelListBoxEditor<Object>(new NullSafeRenderer<Object>() {
            @Override
            public String renderNullSafe(Object object) {
                return ((VDSGroup) object).getname();
            }
        });
    }

    private void initTable() {
        table = new EntityModelCellTable<MoveHost>(true);
        table.setWidth("100%", true);

        TextColumnWithTooltip<EntityModel> nameColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return ((VDS) object.getEntity()).getvds_name();
            }
        };
        table.addColumn(nameColumn, "Name");

        TextColumnWithTooltip<EntityModel> hostColumn = new TextColumnWithTooltip<EntityModel>() {
            @Override
            public String getValue(EntityModel object) {
                return ((VDS) object.getEntity()).gethost_name();
            }
        };
        table.addColumn(hostColumn, "Host/IP");

        TextColumnWithTooltip<EntityModel> statusColumn = new EnumColumn<EntityModel, VDSStatus>() {
            @Override
            public VDSStatus getRawValue(EntityModel object) {
                return ((VDS) object.getEntity()).getstatus();
            }
        };
        table.addColumn(statusColumn, "Status", "90px");
    }

    @Override
    public void edit(MoveHost object) {
        Driver.driver.edit(object);
        table.edit(object);
    }

    @Override
    public MoveHost flush() {
        return table.flush();
    }

}
