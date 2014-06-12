package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.storage.RegisterVmPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.widget.table.column.CustomSelectionCell;

import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public abstract class RegisterEntityPopupView extends AbstractModelBoundPopupView<RegisterEntityModel>
        implements RegisterVmPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<RegisterEntityModel, RegisterEntityPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, RegisterEntityPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<RegisterEntityPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    protected final ApplicationConstants constants;

    protected RegisterEntityInfoPanel registerEntityInfoPanel;

    @UiField
    WidgetStyle style;

    @UiField
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    SimplePanel entityInfoContainer;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel> entityTable;

    @Inject
    public RegisterEntityPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        this.constants = constants;

        initTables();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        localize(constants);
        addStyles();
        driver.initialize(this);

        createEntityTable();
        createInfoPanel();
        asWidget().enableResizeSupport(true);
    }

    abstract void createEntityTable();
    abstract void createInfoPanel();

    private void initTables() {
        // Create the entities main table
        entityTable = new EntityModelCellTable<ListModel>(false, true);
        entityTable.enableColumnResizing();

        // Create split layout panel
        splitLayoutPanel = new SplitLayoutPanel(4);
    }

    private void addStyles() {
    }

    private void localize(ApplicationConstants constants) {
    }

    @Override
    public void edit(RegisterEntityModel model) {
        driver.edit(model);

        entityTable.asEditor().edit(model.getEntities());

        model.getEntities().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                ListModel<ImportEntityData> entities = (ListModel<ImportEntityData>) sender;
                ImportEntityData importEntityData = entities.getSelectedItem();
                registerEntityInfoPanel.updateTabsData(importEntityData);
            }
        });

        model.getCluster().getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                refreshTable();
            }
        });
    }

    private void refreshTable() {
        entityTable.asEditor().edit(entityTable.asEditor().flush());
        entityTable.redraw();
    }

    @Override
    public RegisterEntityModel flush() {
        return driver.flush();
    }

    protected Column getClusterColumn() {
        CustomSelectionCell customSelectionCell = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCell.setStyle(style.cellSelectBox());

        Column column = new Column<ImportEntityData, String>(customSelectionCell) {
            @Override
            public String getValue(ImportEntityData object) {
                ((CustomSelectionCell) getCell()).setOptions(object.getClusterNames());

                return object.getCluster().getSelectedItem() != null ?
                        object.getCluster().getSelectedItem().getName() : constants.empty();
            }
        };
        column.setFieldUpdater(new FieldUpdater<ImportEntityData, String>() {
            @Override
            public void update(int index, ImportEntityData object, String value) {
                object.selectClusterByName(value);

            }
        });

        return column;
    }

    interface WidgetStyle extends CssResource {
        String cellSelectBox();
    }
}
