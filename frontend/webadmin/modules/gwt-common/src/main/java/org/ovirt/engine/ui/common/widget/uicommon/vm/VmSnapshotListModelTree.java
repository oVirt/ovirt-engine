package org.ovirt.engine.ui.common.widget.uicommon.vm;

import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.core.common.queries.CommandVersionsInfo;
import org.ovirt.engine.core.compat.StringFormat;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.CommonApplicationResources;
import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.SubTabTreeActionPanel;
import org.ovirt.engine.ui.common.widget.action.UiCommandButtonDefinition;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCellTable;
import org.ovirt.engine.ui.common.widget.table.column.EmptyColumn;
import org.ovirt.engine.ui.common.widget.tree.AbstractSubTabTree;
import org.ovirt.engine.ui.common.widget.uicommon.AbstractModelBoundTreeWidget;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
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
            CommonApplicationConstants constants,
            CommonApplicationTemplates templates) {
        super(modelProvider, eventBus, resources, constants, templates);

        this.modelProvider = modelProvider;
    }

    @Override
    protected AbstractSubTabTree<VmSnapshotListModel, Snapshot, SnapshotDetailModel> createTree() {
        return new SnapshotsTree(modelProvider, getEventBus(), resources, constants, templates);
    }

    @Override
    public void initTree(SubTabTreeActionPanel actionPanel, EntityModelCellTable<ListModel> table) {
        this.actionPanel = actionPanel;
        this.table = table;

        initHeader();
        initActionButtons();
    }

    protected void initHeader() {
        table.addColumn(new EmptyColumn(), constants.empty(), "55px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.dateSnapshot(), "150px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.statusSnapshot(), "150px"); //$NON-NLS-1$
        table.addColumn(new EmptyColumn(), constants.descriptionSnapshot());
    }

    private void initActionButtons() {
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.createSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getNewCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.previewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getPreviewCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.commitSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getCommitCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.undoSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getUndoCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.deleteSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getRemoveCommand();
            }
        });
        actionPanel.addActionButton(new UiCommandButtonDefinition<Snapshot>(getEventBus(), constants.cloneSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return modelProvider.getModel().getCloneVmCommand();
            }

            @Override
            public String getCustomToolTip() {
                if (!modelProvider.getModel().getIsCloneVmSupported() && modelProvider.getModel().getEntity() != null) {
                    CommandVersionsInfo commandVersionsInfo =
                            AsyncDataProvider.GetCommandVersionsInfo(VdcActionType.AddVmFromSnapshot);
                    String minimalClusterVersion = commandVersionsInfo != null ?
                            commandVersionsInfo.getClusterVersion().toString(2) : ""; //$NON-NLS-1$
                    return StringFormat.format(constants.cloneVmNotSupported(), minimalClusterVersion);
                }
                else {
                    return this.getTitle();
                }
            }
        });
    }

}
