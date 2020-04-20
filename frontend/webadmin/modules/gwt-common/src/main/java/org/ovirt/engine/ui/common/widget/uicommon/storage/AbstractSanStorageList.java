package org.ovirt.engine.ui.common.widget.uicommon.storage;

import java.util.ArrayList;
import java.util.List;

import org.gwtbootstrap3.client.ui.constants.Placement;
import org.ovirt.engine.core.common.businessentities.storage.LUNs;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationMessages;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.utils.ElementTooltipUtils;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.LunModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.SanStoragePartialModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.core.client.Scheduler;
import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.Style.Unit;
import com.google.gwt.dom.client.Style.Visibility;
import com.google.gwt.dom.client.TableRowElement;
import com.google.gwt.event.shared.HandlerRegistration;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.ScrollPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.Tree;
import com.google.gwt.user.client.ui.TreeItem;
import com.google.gwt.user.client.ui.Widget;

public abstract class AbstractSanStorageList<M extends EntityModel, L extends ListModel> extends Composite {

    protected static final int ROW_HEIGHT = 25;

    @SuppressWarnings("rawtypes")
    interface WidgetUiBinder extends UiBinder<Widget, AbstractSanStorageList> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    @UiField
    SimplePanel treeHeader;

    @UiField
    ScrollPanel treeContainer;

    SanStoragePartialModel model;

    Tree tree;

    boolean hideLeaf;
    boolean multiSelection;

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();
    private static final CommonApplicationMessages messages = AssetProvider.getMessages();

    public AbstractSanStorageList(SanStoragePartialModel model) {
        this(model, false, false);
    }

    public AbstractSanStorageList(SanStoragePartialModel model, boolean hideLeaf, boolean multiSelection) {
        this.model = model;
        this.hideLeaf = hideLeaf;
        this.multiSelection = multiSelection;
        model.setMultiSelection(multiSelection);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        createHeaderWidget();
        createSanStorageListWidget();
    }

    public void activateItemsUpdate() {
        disableItemsUpdate();

        model.getItemsChangedEvent().addListener((ev, sender, args) -> updateItems());
        updateItems();
    }

    public void disableItemsUpdate() {
        model.getItemsChangedEvent().getListeners().clear();
    }

    @SuppressWarnings("unchecked")
    protected void updateItems() {
        List<M> items = (List<M>) model.getItems();
        tree.clear();

        if (items != null) {
            for (M rootModel : items) {
                addRootNode(createRootNode(rootModel), createLeafNode(getLeafModel(rootModel)));
            }
        }
    }

    protected void addRootNode(final TreeItem rootItem, final TreeItem leafItem) {
        rootItem.getElement().getStyle().setBackgroundColor("#eff3ff"); //$NON-NLS-1$
        rootItem.getElement().getStyle().setMarginBottom(1, Unit.PX);
        rootItem.getElement().getStyle().setPadding(0, Unit.PX);

        if (leafItem != null) {
            rootItem.addItem(leafItem);

            // Defer styling in order to override padding done in:
            // com.google.gwt.user.client.ui.Tree -> showLeafImage
            Scheduler.get().scheduleDeferred(() -> {
                leafItem.getElement().getStyle().setBackgroundColor("#ffffff"); //$NON-NLS-1$
                leafItem.getElement().getStyle().setMarginLeft(20, Unit.PX);
                leafItem.getElement().getStyle().setPadding(0, Unit.PX);

                Boolean isLeafEmpty = (Boolean) leafItem.getUserObject();
                if (isLeafEmpty != null && isLeafEmpty.equals(Boolean.TRUE)) {
                    rootItem.getElement().getElementsByTagName("td").getItem(0).getStyle().setVisibility(Visibility.HIDDEN); //$NON-NLS-1$
                }
                rootItem.getElement().getElementsByTagName("td").getItem(1).getStyle().setWidth(100, Unit.PCT); //$NON-NLS-1$
            });
        }

        tree.addItem(rootItem);
    }

    protected void grayOutItem(ArrayList<String> grayOutReasons,
            LunModel model,
            EntityModelCellTable<ListModel<LunModel>> table) {
        for (int row = 0; row < table.getRowCount(); row++) {
            TableRowElement tableRowElement = table.getRowElement(row);
            if (table.getVisibleItem(row).equals(model)) {
                tableRowElement.setPropertyBoolean("disabled", true); //$NON-NLS-1$
                updateInputTitle(grayOutReasons, tableRowElement);
            } else {
                ElementTooltipUtils.destroyTooltip(tableRowElement);
            }
        }
    }

    protected void updateInputTitle(ArrayList<String> grayOutReasons, Element input) {
        StringBuilder title = new StringBuilder(constants.empty());
        for (String reason : grayOutReasons) {
            title.append(reason).append(constants.space());
        }
        ElementTooltipUtils.setTooltipOnElement(input, SafeHtmlUtils.fromString(title.toString()), Placement.LEFT);
    }

    protected void updateSelectedLunWarning(LunModel lunModel) {
        LUNs lun = lunModel.getEntity();
        String warning = constants.empty();

        // Adding 'GrayedOutReasons'
        if (lun.getStorageDomainId() != null) {
            warning = messages.lunAlreadyPartOfStorageDomainWarning(lun.getStorageDomainName());
        } else if (lun.getDiskId() != null) {
            warning = messages.lunUsedByDiskWarning(lun.getDiskAlias());
        }

        model.setSelectedLunWarning(warning);
    }

    protected void createSanStorageListWidget() {
        tree = new Tree();
        treeContainer.add(tree);
    }

    public void setTreeContainerStyleName(String styleName) {
        treeContainer.setStyleName(styleName);
    }

    public void setTreeContainerHeight(double height) {
        treeContainer.getElement().getStyle().setHeight(height, Unit.PX);
    }

    public ScrollPanel getTreeContainer() {
        return treeContainer;
    }

    protected HandlerRegistration addOpenHandlerToTree(Tree tree, TreeItem item, EntityModelCellTable<ListModel<LunModel>> table) {
        return tree.addOpenHandler(e -> {
            TreeItem target = e.getTarget();
            if (target != null && target.getChildCount() == 1 && target.getChild(0).equals(item)) {
                table.updateGridSize(table.getVisibleItemCount() * ROW_HEIGHT + 1);
                table.redraw();
            }
        });
    }

    protected abstract void createHeaderWidget();

    protected abstract L getLeafModel(M rootModel);

    protected abstract TreeItem createRootNode(M rootModel);

    protected abstract TreeItem createLeafNode(L leafModel);

    public interface SanStorageListTargetRootResources extends DataGrid.Resources {
        interface Style extends DataGrid.Style {
        }

        @Override
        @Source({ DataGrid.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/SanStorageListTargetRoot.css" })
        Style dataGridStyle();
    }

    public interface SanStorageListLunTableResources extends DataGrid.Resources {
        interface Style extends DataGrid.Style {
        }

        @Override
        @Source({ DataGrid.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/SanStorageListLunTable.css" })
        Style dataGridStyle();
    }

    public interface SanStorageListLunRootResources extends DataGrid.Resources {
        interface Style extends DataGrid.Style {
        }

        @Override
        @Source({ DataGrid.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/SanStorageListLunRoot.css" })
        Style dataGridStyle();
    }

    public interface SanStorageListTargetTableResources extends DataGrid.Resources {
        interface Style extends DataGrid.Style {
        }

        @Override
        @Source({ DataGrid.Style.DEFAULT_CSS, "org/ovirt/engine/ui/common/css/SanStorageListTargetTable.css" })
        Style dataGridStyle();
    }

}
