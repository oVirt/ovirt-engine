package org.ovirt.engine.ui.common.widget.action;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Snapshot;
import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmListModel;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmSnapshotListModel;

import com.google.web.bindery.event.shared.EventBus;

public class SnapshotActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Snapshot, VmListModel<Void>, VmSnapshotListModel> {

    private static final CommonApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SnapshotActionPanelPresenterWidget(EventBus eventBus,
           DetailActionPanelPresenterWidget.ViewDef<Snapshot> view,
           SearchableDetailModelProvider<Snapshot, VmListModel<Void>, VmSnapshotListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.createSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getNewCommand();
            }
        });

        List<ActionButtonDefinition<Snapshot>> previewSubActions = new ArrayList<>();
        previewSubActions.add(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.customPreviewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCustomPreviewCommand();
            }
        });
        addComboActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.previewSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPreviewCommand();
            }
        }, previewSubActions);

        addActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.commitSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCommitCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.undoSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUndoCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.deleteSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.cloneSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCloneVmCommand();
            }
        });
        addActionButton(new UiCommandButtonDefinition<Snapshot>(getSharedEventBus(), constants.makeTemplateFromSnapshot()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCloneTemplateCommand();
            }
        });
    }

}
