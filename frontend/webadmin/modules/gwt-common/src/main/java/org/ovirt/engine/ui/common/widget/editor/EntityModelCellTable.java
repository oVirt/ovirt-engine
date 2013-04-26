package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.PopupTableResources;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.table.ElementIdCellTable;
import org.ovirt.engine.ui.common.widget.table.column.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.header.SelectAllCheckBoxHeader;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;

import com.google.gwt.cell.client.CheckboxCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.CellPreviewEvent;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A CellTable of a {@link ListModel} of {@link EntityModel}s.
 */
public class EntityModelCellTable<M extends ListModel> extends ElementIdCellTable<EntityModel> implements HasEditorDriver<M> {

    public static enum SelectionMode {
        NONE,
        SINGLE,
        MULTIPLE
    }

    private M listModel;

    private static final int DEFAULT_PAGESIZE = 1000;
    private static final int CHECK_COLUMN_WIDTH = 27;

    private static CommonApplicationConstants constants = GWT.create(CommonApplicationConstants.class);

    /**
     * Create a new {@link EntityModelCellTable} with single selection mode.
     */
    public EntityModelCellTable() {
        this(SelectionMode.SINGLE, (Resources) GWT.create(PopupTableResources.class));
    }

    /**
     * Create a new {@link EntityModelCellTable} with single selection mode.
     *
     * @param resources
     *            Table resources.
     */
    public EntityModelCellTable(Resources resources) {
        this(SelectionMode.SINGLE, resources);
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param selectionMode
     *            Table selection mode.
     */
    public EntityModelCellTable(SelectionMode selectionMode) {
        this(selectionMode, (Resources) GWT.create(PopupTableResources.class));
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param isMultiple
     *            Whether to allow multiple ({@code true}) or single ({@code false}) selection mode.
     */
    public EntityModelCellTable(boolean isMultiple) {
        this(isMultiple, (Resources) GWT.create(PopupTableResources.class));
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param selectionMode
     *            Table selection mode.
     * @param hideCheckbox
     *            Whether to hide selection column or not.
     */
    public EntityModelCellTable(SelectionMode selectionMode, boolean hideCheckbox) {
        this(selectionMode, (Resources) GWT.create(PopupTableResources.class), hideCheckbox, false);
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param isMultiple
     *            Whether to allow multiple ({@code true}) or single ({@code false}) selection mode.
     * @param hideCheckbox
     *            Whether to hide selection column or not.
     */
    public EntityModelCellTable(boolean isMultiple, boolean hideCheckbox) {
        this(isMultiple, (Resources) GWT.create(PopupTableResources.class), hideCheckbox);
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param isMultiple
     *            Whether to allow multiple ({@code true}) or single ({@code false}) selection mode.
     * @param hideCheckbox
     *            Whether to hide selection column or not.
     * @param showSelectAllCheckbox
     *            Whether to show the SelectAll Checkbox in the header or not.
     */
    public EntityModelCellTable(boolean isMultiple, boolean hideCheckbox, boolean showSelectAllCheckbox) {
        this(isMultiple, (Resources) GWT.create(PopupTableResources.class), hideCheckbox, showSelectAllCheckbox);
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param selectionMode
     *            Table selection mode.
     * @param resources
     *            Table resources.
     */
    public EntityModelCellTable(SelectionMode selectionMode, Resources resources) {
        this(selectionMode, resources, false, false);
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param isMultiple
     *            Whether to allow multiple ({@code true}) or single ({@code false}) selection mode.
     * @param resources
     *            Table resources.
     */
    public EntityModelCellTable(boolean isMultiple, Resources resources) {
        this(isMultiple, resources, false, false);
    }

    public EntityModelCellTable(boolean isMultiple, Resources resources, boolean hideCheckbox) {
        this(isMultiple ? SelectionMode.MULTIPLE : SelectionMode.SINGLE, resources, hideCheckbox, false);
    }

    public EntityModelCellTable(boolean isMultiple,
            Resources resources,
            boolean hideCheckbox,
            boolean showSelectAllCheckbox) {
        this(isMultiple ? SelectionMode.MULTIPLE : SelectionMode.SINGLE, resources, hideCheckbox, showSelectAllCheckbox);
    }

    @SuppressWarnings("unchecked")
    public EntityModelCellTable(SelectionMode selectionMode,
            Resources resources,
            boolean hideCheckbox,
            boolean showSelectAllCheckbox) {
        super(DEFAULT_PAGESIZE, resources);

        // Configure table selection model
        switch (selectionMode) {
        case MULTIPLE:
            setSelectionModel(new MultiSelectionModel<EntityModel>(),
                    DefaultSelectionEventManager.<EntityModel> createCheckboxManager());
            break;
        case NONE:
            setSelectionModel(new NoSelectionModel<EntityModel>());
            break;
        case SINGLE:
        default:
            setSelectionModel(new SingleSelectionModel<EntityModel>());
            break;
        }

        // Handle selection
        getSelectionModel().addSelectionChangeHandler(new Handler() {
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
                    EntityModel selectedObject =
                            ((SingleSelectionModel<EntityModel>) selectionModel).getSelectedObject();
                    if (selectedObject != null) {
                        selectedObject.setIsSelected(true);
                        EntityModelCellTable.this.listModel.setSelectedItem(selectedObject);
                    }
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
            // Add selection column
            Column<EntityModel, Boolean> checkColumn = null;
            if (getSelectionModel() instanceof SingleSelectionModel) {
                checkColumn = new Column<EntityModel, Boolean>(
                        new RadioboxCell(true, false)) {
                    @Override
                    public Boolean getValue(EntityModel object) {
                        return getSelectionModel().isSelected(object);
                    }
                };
                addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
            } else if (getSelectionModel() instanceof MultiSelectionModel) {
                checkColumn = new Column<EntityModel, Boolean>(
                        new CheckboxCell(true, false)) {
                    @Override
                    public Boolean getValue(EntityModel object) {
                        return getSelectionModel().isSelected(object);
                    }
                };
                if (showSelectAllCheckbox) {
                    SelectAllCheckBoxHeader<EntityModel> selectAllHeader = new SelectAllCheckBoxHeader<EntityModel>() {
                        @Override
                        protected void selectionChanged(Boolean value) {
                            if (listModel == null || listModel.getItems() == null) {
                                return;
                            }
                            handleSelection(value, listModel, getSelectionModel());
                        }

                        @Override
                        public Boolean getValue() {
                            if (listModel == null || listModel.getItems() == null) {
                                return false;
                            }
                            return getCheckValue(listModel.getItems(), getSelectionModel());
                        }
                    };
                    addColumn(checkColumn, selectAllHeader);
                } else {
                    addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
                }
            }

            if (checkColumn != null) {
                setColumnWidth(checkColumn, CHECK_COLUMN_WIDTH, Unit.PX);
            }

            addCellPreviewHandler(new CellPreviewEvent.Handler<EntityModel>() {
                @Override
                public void onCellPreview(CellPreviewEvent<EntityModel> event) {
                    if ("click".equals(event.getNativeEvent().getType()) //$NON-NLS-1$
                            && !(getSelectionModel() instanceof NoSelectionModel)) {
                        // Let the selection column deal with this
                        if (event.getColumn() == 0) {
                            return;
                        }
                        getSelectionModel().setSelected(event.getValue(),
                                !getSelectionModel().isSelected(event.getValue()));
                    }
                }
            });
        }
    }

    public void addEntityModelColumn(Column<EntityModel, ?> column, String headerString) {
        super.addColumn(column, headerString);
    }

    public void setCustomSelectionColumn(Column customSelectionColumn, String width) {
        removeColumn(0);
        insertColumn(0, customSelectionColumn, SafeHtmlUtils.fromSafeConstant("<br/>")); //$NON-NLS-1$
        setColumnWidth(customSelectionColumn, width);
    }

    @Override
    public void addColumn(Column column, String headerString) {
        super.addColumn(column, headerString);
    }

    @Override
    public void addColumn(Column column, SafeHtml headerHtml) {
        super.addColumn(column, headerHtml);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method shadows original signature semantics, replacing {@code footerString} with {@code width}.
     */
    @Override
    public void addColumn(Column column, String headerString, String width) {
        addColumnAndSetWidth(column, headerString, width);
    }

    public void addColumn(Column column, Header<?> header, String width) {
        super.addColumn(column, header);
        super.setColumnWidth(column, width);
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note that this method shadows original signature semantics, replacing {@code footerString} with {@code width}.
     */
    @Override
    public void insertColumn(int beforeIndex, Column column, String headerString, String width) {
        super.insertColumn(beforeIndex, column, headerString);
        super.setColumnWidth(column, width);
    }

    public void setLoadingState(LoadingState state) {
        super.onLoadingStateChanged(state);
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

        // Get items from ListModel and update table data
        List<EntityModel> items = (List<EntityModel>) listModel.getItems();
        setRowData(items == null ? new ArrayList<EntityModel>() : items);
    }

    @Override
    public M flush() {
        return listModel;
    }

}
