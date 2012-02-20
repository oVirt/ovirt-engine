package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.Event;
import org.ovirt.engine.core.compat.EventArgs;
import org.ovirt.engine.core.compat.IEventListener;
import org.ovirt.engine.ui.common.PopupTableResources;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.table.ElementIdCellTable;
import org.ovirt.engine.ui.common.widget.table.column.RadioboxCell;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A CellTable of a {@link ListModel} of {@link EntityModel}s
 */
public class EntityModelCellTable<M extends ListModel> extends ElementIdCellTable<EntityModel> implements HasEditorDriver<M> {

    /**
     * The ListModel
     */
    private M listModel;

    /**
     * Whether to allow multi/single selection
     */
    private final boolean multiSelection;

    private static final int DEFAULT_PAGESIZE = 1000;
    private static Resources DEFAULT_RESOURCES = GWT.create(CellTable.Resources.class);
    private static final int CHECK_COLUMN_WIDTH = 27;

    /**
     * Create a new {@link EntityModelCellTable} with Single Selection
     */
    public EntityModelCellTable() {
        this(false, (Resources) GWT.create(PopupTableResources.class));
    }

    /**
     * Create a new {@link EntityModelCellTable} with Single Selection
     *
     * @param resources
     *            table's resources
     */
    public EntityModelCellTable(Resources resources) {
        this(false, resources);
    }

    /**
     * Create a new {@link EntityModelCellTable}
     *
     * @param multiSelection
     *            Whether to allow multi/single selection
     */
    public EntityModelCellTable(boolean multiSelection) {
        this(multiSelection, (Resources) GWT.create(PopupTableResources.class));
    }

    public EntityModelCellTable(boolean multiSelection, boolean hideCheckbox) {
        this(multiSelection, (Resources) GWT.create(PopupTableResources.class), hideCheckbox);
    }

    /**
     * Create a new {@link EntityModelCellTable}
     *
     * @param multiSelection
     *            Whether to allow multi/single selection
     *
     * @param resources
     *            table's resources
     */
    public EntityModelCellTable(boolean multiSelection, Resources resources) {
        this(multiSelection, resources, false);
    }

    public EntityModelCellTable(boolean multiSelection, Resources resources, boolean hideCheckbox) {
        super(DEFAULT_PAGESIZE, resources);

        this.multiSelection = multiSelection;

        if (!multiSelection) {
            setSelectionModel(new SingleSelectionModel<EntityModel>());
        } else {
            setSelectionModel(new MultiSelectionModel<EntityModel>(),
                    DefaultSelectionEventManager.<EntityModel> createCheckboxManager());
        }

        // Handle Selection
        getSelectionModel().addSelectionChangeHandler(new Handler() {
            @SuppressWarnings("unchecked")
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if ((EntityModelCellTable.this.listModel == null)
                        || (EntityModelCellTable.this.listModel.getItems() == null)) {
                    return;
                }

                // Clear "IsSelected"
                for (EntityModel entity : (List<EntityModel>) EntityModelCellTable.this.listModel.getItems()) {
                    entity.setIsSelected(false);
                }
                EntityModelCellTable.this.listModel.setSelectedItems(null);

                // Set "IsSelected"
                SelectionModel<? super EntityModel> selectionModel = EntityModelCellTable.this.getSelectionModel();
                if (selectionModel instanceof SingleSelectionModel) {
                    ((SingleSelectionModel<EntityModel>) selectionModel).getSelectedObject().setIsSelected(true);
                    EntityModelCellTable.this.listModel.setSelectedItem(((SingleSelectionModel<EntityModel>) selectionModel).getSelectedObject());
                } else if (selectionModel instanceof MultiSelectionModel) {
                    List<EntityModel> selectedItems = new ArrayList<EntityModel>();
                    for (EntityModel entity : ((MultiSelectionModel<EntityModel>) selectionModel).getSelectedSet()) {
                        entity.setIsSelected(true);
                        selectedItems.add(entity);
                    }

                    EntityModelCellTable.this.listModel.setSelectedItems(selectedItems);
                }
            }
        });

        if (!hideCheckbox) {
            // add selection columns
            Column<EntityModel, Boolean> checkColumn;
            if (multiSelection) {
                checkColumn = new Column<EntityModel, Boolean>(
                        new CheckboxCell(true, false)) {
                    @Override
                    public Boolean getValue(EntityModel object) {
                        return getSelectionModel().isSelected(object);
                    }
                };
            } else {
                checkColumn = new Column<EntityModel, Boolean>(
                        new RadioboxCell(true, false)) {
                    @Override
                    public Boolean getValue(EntityModel object) {
                        return getSelectionModel().isSelected(object);
                    }
                };
            }
            addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
            setColumnWidth(checkColumn, CHECK_COLUMN_WIDTH, Unit.PX);
        }
    }

    /**
     * Ad an EntityModelColumn to the Grid
     *
     * @param column
     * @param headerString
     */
    public void addEntityModelColumn(Column<EntityModel, ?> column, String headerString) {
        super.addColumn(column, headerString);
    }

    public void setCustomSelectionColumn(Column customSelectionColumn, String width) {
        removeColumn(0);
        insertColumn(0, customSelectionColumn, SafeHtmlUtils.fromSafeConstant("<br/>"));
        setColumnWidth(customSelectionColumn, width);
    }

    @Override
    public void addColumn(Column column, String headerString) {
        super.addColumn(column, headerString);
    }

    @Override
    public void addColumn(Column column, String headerString, String width) {
        super.addColumn(column, headerString);
        super.setColumnWidth(column, width);
    }

    public void addColumn(Column column, Header<?> header, String width) {
        super.addColumn(column, header);
        super.setColumnWidth(column, width);
    }

    @Override
    public void insertColumn(int beforeIndex, Column column, String headerString, String width) {
        super.insertColumn(beforeIndex, column, headerString);
        super.setColumnWidth(column, width);
    }

    public void setLoadingState(LoadingState state) {
        super.onLoadingStateChanged(state);
    }

    @Override
    public void insertColumn(int beforeIndex, Column col, Header header, Header footer) {
        super.insertColumn(beforeIndex, col, header, footer);
        configureElementId(col);
    }

    @SuppressWarnings("unchecked")
    @Override
    public void edit(M object) {
        this.listModel = object;

        // Add ItemsChangedEvent Listener
        object.getItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                List<EntityModel> items = (List<EntityModel>) list.getItems();
                setRowData(items == null ? new ArrayList<EntityModel>() : items);
            }
        });

        object.getSelectedItemChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                getSelectionModel().setSelected((EntityModel) list.getSelectedItem(), true);
            }
        });

        object.getSelectedItemsChangedEvent().addListener(new IEventListener() {
            @Override
            public void eventRaised(Event ev, Object sender, EventArgs args) {
                M list = (M) sender;
                if (list.getSelectedItems() != null) {
                    for (Object item : list.getSelectedItems()) {
                        EntityModel entityModel = (EntityModel) item;
                        getSelectionModel().setSelected(entityModel, true);
                    }
                }
            }
        });
    }

    @Override
    public M flush() {
        return listModel;
    }

}
