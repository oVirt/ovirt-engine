package org.ovirt.engine.ui.webadmin.section.main.view.popup.storage;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.presenter.AbstractModelBoundPopupPresenterWidget;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.panel.AlertPanel;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.RegisterEntityModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.ImportEntityData;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.table.cell.CustomSelectionCell;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.client.ui.ResizeLayoutPanel;
import com.google.gwt.user.client.ui.SplitLayoutPanel;
import com.google.inject.Inject;

public abstract class RegisterEntityPopupView<E, D extends ImportEntityData<E>, M extends RegisterEntityModel<E, D>>
        extends AbstractModelBoundPopupView<M>
        implements AbstractModelBoundPopupPresenterWidget.ViewDef<M> {

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, RegisterEntityPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<RegisterEntityPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    protected final UiCommonEditorDriver<M, RegisterEntityPopupView<E, D, M>> driver;

    protected RegisterEntityInfoPanel<E, D, M> registerEntityInfoPanel;

    private M registerEntityModel;

    @UiField(provided = true)
    SplitLayoutPanel splitLayoutPanel;

    @UiField
    ResizeLayoutPanel entityInfoContainer;

    @UiField(provided = true)
    @Ignore
    EntityModelCellTable<ListModel<D>> entityTable;

    @UiField
    AlertPanel warningPanel;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public RegisterEntityPopupView(EventBus eventBus, UiCommonEditorDriver<M, RegisterEntityPopupView<E, D, M>> driver) {
        super(eventBus);
        this.driver = driver;

        initTables();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        this.driver.initialize(this);
    }

    protected abstract void createEntityTable(M model);
    protected abstract void createInfoPanel(M model);

    private void initTables() {
        // Create the entities main table
        entityTable = new EntityModelCellTable<>(false, true);
        entityTable.enableColumnResizing();

        // Create split layout panel
        splitLayoutPanel = new SplitLayoutPanel(4);
    }

    private void createTables(M model) {
        createEntityTable(model);
        createInfoPanel(model);
        entityTable.asEditor().edit(model.getEntities());
        model.getEntities().setSelectedItem(Linq.firstOrNull(model.getEntities().getItems()));
    }

    protected void refreshEntityTable() {
        entityTable.asEditor().edit(entityTable.asEditor().flush());
        entityTable.redraw();
    }

    @Override
    public void edit(final M model) {
        registerEntityModel = model;
        driver.edit(model);

        model.getEntities().getSelectedItemChangedEvent().addListener((ev, sender, args) -> {
            ListModel<D> entities = (ListModel<D>) sender;
            D importEntityData = entities.getSelectedItem();
            if (importEntityData != null) {
                registerEntityInfoPanel.updateTabsData(importEntityData);
            }
        });

        model.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            if (args.propertyName.equals("InvalidVm")) { //$NON-NLS-1$
                entityTable.redraw();
            }
        });

        model.getCluster().getItemsChangedEvent().addListener((ev, sender, args) -> createTables(model));

        model.getCluster().getSelectedItemChangedEvent().addListener((ev, sender, args) -> refreshEntityTable());

        model.getClusterQuotasMap().getEntityChangedEvent().addListener((ev, sender, args) -> refreshEntityTable());

        model.getStorageQuota().getItemsChangedEvent().addListener((ev, sender, args) -> refreshEntityTable());
    }

    @Override
    public M flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    protected Column<D, String> getClusterColumn() {
        CustomSelectionCell customSelectionCell = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCell.setStyle("input-group col-xs-11 gwt-ListBox"); //$NON-NLS-1$

        Column<D, String> column = new Column<D, String>(customSelectionCell) {
            @Override
            public String getValue(D object) {
                ((CustomSelectionCell) getCell()).setOptions(object.getClusterNames());

                return object.getCluster().getSelectedItem() != null ?
                        object.getCluster().getSelectedItem().getName() : constants.empty();
            }
        };
        column.setFieldUpdater((index, object, value) -> {
            object.selectClusterByName(value);
            refreshEntityTable();
        });

        return column;
    }

    protected Column<D, String> getClusterQuotaColumn() {
        CustomSelectionCell customSelectionCell = new CustomSelectionCell(new ArrayList<String>());
        customSelectionCell.setStyle("input-group col-xs-11 gwt-ListBox"); //$NON-NLS-1$

        Column<D, String> column = new Column<D, String>(customSelectionCell) {
            @Override
            public String getValue(D object) {
                Guid clusterId = object.getCluster().getSelectedItem() != null ?
                        object.getCluster().getSelectedItem().getId() : null;
                List<Quota> quotas = registerEntityModel.getClusterQuotasMap().getEntity().get(clusterId);

                object.getClusterQuota().setItems(quotas);
                ((CustomSelectionCell) getCell()).setOptions(registerEntityModel.getQuotaNames(quotas));

                return object.getClusterQuota().getSelectedItem() != null ?
                        object.getClusterQuota().getSelectedItem().getQuotaName() : constants.empty();
            }
        };
        column.setFieldUpdater((index, object, value) -> registerEntityModel.selectQuotaByName(value, object.getClusterQuota()));

        return column;
    }
}
