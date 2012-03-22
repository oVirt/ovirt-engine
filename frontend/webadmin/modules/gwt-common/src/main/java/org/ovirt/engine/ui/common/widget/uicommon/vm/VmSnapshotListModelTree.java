package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTreeWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicommonweb.models.ListWithDetailsModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.SnapshotDetailModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;

import com.google.gwt.event.shared.EventBus;

public class VmSnapshotListModelTree<L extends ListWithDetailsModel> extends AbstractModelBoundTreeWidget<VmSnapshotListModel, Snapshot, SnapshotDetailModel, VmListModel> {

    private SubTabTreeActionPanel actionPanel;
    private EntityModelCellTable<ListModel> table;
    private SearchableDetailModelProvider<Snapshot, L, VmSnapshotListModel> modelProvider;

    public VmSnapshotListModelTree(SearchableDetailModelProvider modelProvider,
            EventBus eventBus,
            CommonApplicationResources resources,
            CommonApplicationConstants constants) {
        super(modelProvider, eventBus, resources, constants);

        this.modelProvider = modelProvider;
    }

    @Override
    protected AbstractSubTabTree<VmSnapshotListModel, Snapshot, SnapshotDetailModel> createTree() {
        return new SnapshotsTree(modelProvider, getEventBus(), resources, constants);
    }

    @Override
    public void initTree(SubTabTreeActionPanel actionPanel, EntityModelCellTable<ListModel> table) {
        this.actionPanel = actionPanel;
        this.table = table;

        initHeader();
        initActionButtons();
    }

    protected void initHeader() {
        table.addColumn(new EmptyColumn(), "", "55px");
        table.addColumn(new EmptyColumn(), "Date", "150px");
        table.addColumn(new EmptyColumn(), "Status", "150px");
        table.addColumn(new EmptyColumn(), "Description");
    }

    private void initActionButtons() {
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), "Create") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getNewCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), "Preview") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getPreviewCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), "Commit") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getCommitCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), "Undo") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getUndoCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), "Delete") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), "Clone") {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getCloneVmCommand();
            }

            @Override
            public String getCustomToolTip() {
                if (!modelProvider.getModel().getIsCloneVmSupported()) {
                    return constants.cloneVmNotSupported();
                }
                else {
                    return this.getTitle();
                }
            }
        });
    }

}
