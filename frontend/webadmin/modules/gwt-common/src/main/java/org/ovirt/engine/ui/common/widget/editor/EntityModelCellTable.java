package org.ovirt.engine.ui.common.widget.editor;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.DataGridPopupTableResources;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.common.widget.IsEditorDriver;
import org.ovirt.engine.ui.common.widget.table.ElementIdCellTable;
import org.ovirt.engine.ui.common.widget.table.HasColumns;
import org.ovirt.engine.ui.common.widget.table.cell.CheckboxCell;
import org.ovirt.engine.ui.common.widget.table.cell.EventHandlingCell;
import org.ovirt.engine.ui.common.widget.table.cell.RadioboxCell;
import org.ovirt.engine.ui.common.widget.table.header.AbstractSelectAllCheckBoxHeader;
import org.ovirt.engine.ui.common.widget.table.header.SafeHtmlHeader;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;

import com.google.gwt.cell.client.Cell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.BrowserEvents;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.resources.client.ClientBundle;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.Header;
import com.google.gwt.user.cellview.client.LoadingStateChangeEvent.LoadingState;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.MultiSelectionModel;
import com.google.gwt.view.client.NoSelectionModel;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SelectionChangeEvent.Handler;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;

/**
 * A {@code CellTable} acting as Editor of {@link ListModel} objects containing {@link EntityModel} items.
 *
 * @param <M>
 *            List model type.
 */
public class EntityModelCellTable<M extends ListModel> extends ElementIdCellTable<EntityModel> implements
    IsEditorDriver<M>, HasColumns {

    public interface CellTableValidation extends CssResource {
        String invalidRow();
    }

    public interface CellTableResources extends ClientBundle {
        @Source("org/ovirt/engine/ui/common/css/CellTableValidation.css")
        CellTableValidation cellTableValidation();
    }

    public enum SelectionMode {
        NONE,
        SINGLE,
        MULTIPLE
    }

    private static final CellTableResources cellTableResources = GWT.create(CellTableResources.class);

    private static final int DEFAULT_PAGESIZE = 1000;
    private static final int CHECK_COLUMN_WIDTH = 27;

    /**
     * Index of selection (check box) column, if {@linkplain #isSelectionColumnPresent present}.
     */
    public static final int SELECTION_COLUMN_INDEX = 0;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    private final CellTableValidation style;
    private final HasDataListModelEditorAdapter<M, EntityModel> editorAdapter;

    private boolean selectionColumnPresent = false;

    /**
     * Create a new {@link EntityModelCellTable} with single selection mode.
     */
    public EntityModelCellTable() {
        this(SelectionMode.SINGLE, GWT.create(DataGridPopupTableResources.class));
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
        this(selectionMode, GWT.create(DataGridPopupTableResources.class));
    }

    /**
     * Create a new {@link EntityModelCellTable}.
     *
     * @param isMultiple
     *            Whether to allow multiple ({@code true}) or single ({@code false}) selection mode.
     */
    public EntityModelCellTable(boolean isMultiple) {
        this(isMultiple, GWT.create(DataGridPopupTableResources.class));
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
        this(selectionMode, GWT.create(DataGridPopupTableResources.class), hideCheckbox, false);
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
        this(isMultiple, GWT.create(DataGridPopupTableResources.class), hideCheckbox);
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
        this(isMultiple, GWT.create(DataGridPopupTableResources.class), hideCheckbox, showSelectAllCheckbox);
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

    public EntityModelCellTable(SelectionMode selectionMode,
            Resources resources,
            boolean hideCheckbox,
            boolean showSelectAllCheckbox) {
        super(DEFAULT_PAGESIZE, resources);

        dontApplyResizableHeaderStyle();
        style = cellTableResources.cellTableValidation();
        style.ensureInjected();

        this.editorAdapter = new HasDataListModelEditorAdapter<>(this);

        // Configure table selection model
        switch (selectionMode) {
        case MULTIPLE:
            setSelectionModel(new MultiSelectionModel<>(), DefaultSelectionEventManager.createCheckboxManager(0));
            break;
        case NONE:
            setSelectionModel(new NoSelectionModel<>());
            break;
        case SINGLE:
        default:
            setSelectionModel(new SingleSelectionModel<>());
            break;
        }

        addSelectionChangeHandler();
        addCheckBoxColumn(hideCheckbox, showSelectAllCheckbox);
        super.setHeight(LOADING_HEIGHT + Unit.PX.getType());

    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateGridSize();
    }

    @Override
    public void setHeight(String height) {
        super.setHeight(height);
        redraw();
        isHeightSet = true;
    }

    public void addSelectionChangeHandler() {
        // Handle selection
        getSelectionModel().addSelectionChangeHandler(new Handler() {
            @Override
            public void onSelectionChange(SelectionChangeEvent event) {
                if (getListModel() == null || getListModel().getItems() == null) {
                    return;
                }

                // Clear "IsSelected"
                for (EntityModel entity : (List<EntityModel>) getListModel().getItems()) {
                    entity.setIsSelected(false);
                }

                // Set "IsSelected"
                SelectionModel<? super EntityModel> selectionModel = EntityModelCellTable.this.getSelectionModel();
                if (selectionModel instanceof SingleSelectionModel) {
                    EntityModel selectedObject =
                            ((SingleSelectionModel<EntityModel>) selectionModel).getSelectedObject();
                    clearCurrentSelectedItems();
                    if (selectedObject != null) {
                        selectedObject.setIsSelected(true);
                        getListModel().setSelectedItem(selectedObject);
                    }
                } else if (selectionModel instanceof MultiSelectionModel) {
                    List<EntityModel> selectedItems = new ArrayList<>();
                    for (EntityModel entity : ((MultiSelectionModel<EntityModel>) selectionModel).getSelectedSet()) {
                        entity.setIsSelected(true);
                        selectedItems.add(entity);
                    }
                    clearCurrentSelectedItems();
                    getListModel().setSelectedItems(selectedItems);
                }
            }

            private void clearCurrentSelectedItems() {
                getListModel().setSelectedItems(null);
                getListModel().setSelectedItem(null);
            }
        });
    }

    private void addCheckBoxColumn(boolean hideCheckbox, boolean showSelectAllCheckbox) {
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
                    AbstractSelectAllCheckBoxHeader<EntityModel> selectAllHeader = new AbstractSelectAllCheckBoxHeader<EntityModel>() {
                        @Override
                        protected void selectionChanged(Boolean value) {
                            if (getListModel() == null || getListModel().getItems() == null) {
                                return;
                            }
                            handleSelection(value, getListModel(), getSelectionModel());
                        }

                        @Override
                        public Boolean getValue() {
                            if (getListModel() == null || getListModel().getItems() == null) {
                                return false;
                            }
                            return getCheckValue(getListModel().getItems(), getSelectionModel());
                        }
                    };
                    addColumn(checkColumn, selectAllHeader);
                } else {
                    addColumn(checkColumn, SafeHtmlUtils.fromSafeConstant(constants.htmlNonBreakingSpace()));
                }
            }

            if (checkColumn != null) {
                setColumnWidth(checkColumn, CHECK_COLUMN_WIDTH, Unit.PX);
                selectionColumnPresent = true;
            }

            addCellPreviewHandler(event -> {
                int columnIndex = event.getColumn();
                Cell<?> cell = getColumn(columnIndex).getCell();
                if (cell instanceof EventHandlingCell
                        && ((EventHandlingCell) cell).handlesEvent(event)) {
                    return;
                }

                if (BrowserEvents.CLICK.equals(event.getNativeEvent().getType())
                        && !(getSelectionModel() instanceof NoSelectionModel)) {
                    // Let the selection column deal with this
                    if (event.getColumn() == 0) {
                        return;
                    }
                    if (getSelectionModel() instanceof MultiSelectionModel) {
                        getSelectionModel().setSelected(event.getValue(),
                                !getSelectionModel().isSelected(event.getValue()));
                    } else if (getSelectionModel() instanceof SingleSelectionModel) {
                        // Setting the value only here is fine as we clear all the values in the selection change
                        // handler
                        getSelectionModel().setSelected(event.getValue(), true);
                    }
                }
            });
        }
    }

    public boolean isSelectionColumnPresent() {
        return selectionColumnPresent;
    }

    M getListModel() {
        return asEditor().flush();
    }

    public void validate(List<String> errors) {
        int rowCount = getRowCount();
        assert errors != null && errors.size() == rowCount : "errors must be the same size as the contents of the table!"; //$NON-NLS-1$

        for (int i = 0; i < rowCount; ++i) {
            Element element = getRowElement(i);
            assert element != null : "element shouldn't be null if errors is the same size as the contents of the table!"; //$NON-NLS-1$

            String error = errors.get(i);
            boolean valid = StringHelper.isNullOrEmpty(error);

            if (!valid) {
                ElementTooltipUtils.setTooltipOnElement(element, SafeHtmlUtils.fromString(error));
                element.addClassName(style.invalidRow());
            } else {
                ElementTooltipUtils.destroyTooltip(element);
                element.removeClassName(style.invalidRow());
            }
        }
    }

    @Override
    public HasEditorDriver<M> asEditor() {
        return editorAdapter;
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
        super.addColumnAndSetWidth(column, headerString, width);
    }

    public void addColumn(Column column, SafeHtml headerHtml, String width) {
        super.addColumnWithHtmlHeader(column, headerHtml, width);
    }

    public void addColumn(Column column, Header<?> header, String width) {
        super.addColumn(column, header);
        super.setColumnWidth(column, width);
    }

    @Override
    public void addColumnWithHtmlHeader(Column column, SafeHtml header) {
        super.addColumn(column, header);
    }

    @Override
    public void addColumnWithHtmlHeader(Column column, SafeHtml header, String width) {
        super.addColumn(column, header);
        super.setColumnWidth(column, width);
    }

    @Override
    public void addColumn(Column column, SafeHtmlHeader header) {
        super.addColumn(column, header);
    }

    @Override
    public void addColumn(Column column, SafeHtmlHeader header, String width) {
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

    @Override
    public void cleanup() {
        asEditor().cleanup();
        super.cleanup();
    }
}
