package org.ovirt.engine.ui.webadmin.section.main.presenter.tab;

import java.util.LinkedList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.ActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class DiskActionPanelPresenterWidget extends ActionPanelPresenterWidget<Void, Disk, DiskListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Void, Disk> newButtonDefinition;

    @Inject
    public DiskActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Void, Disk> view,
            MainModelProvider<Disk, DiskListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Void, Disk>(constants.newDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);

        addActionButton(new WebAdminButtonDefinition<Void, Disk>(constants.editDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getEditCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Disk>(constants.moveDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getMoveCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Void, Disk>(constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCopyCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, Disk>(constants.exportDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, Disk>(constants.assignQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeQuotaCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Void, Disk>(constants.refreshLUN()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRefreshLUNCommand();
            }
        });

        // Upload operations drop down
        List<ActionButtonDefinition<Void, Disk>> uploadActions = new LinkedList<>();
        uploadActions.add(new WebAdminButtonDefinition<Void, Disk>(constants.uploadImageStart()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Void, Disk>(constants.uploadImageCancel()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCancelUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Void, Disk>(constants.uploadImagePause()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPauseUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Void, Disk>(constants.uploadImageResume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getResumeUploadCommand();
            }
        });
        addDropdownActionButton(new WebAdminMenuBarButtonDefinition<Void, Disk>(constants.uploadImage(),
                uploadActions));

        addActionButton(new WebAdminButtonDefinition<Void, Disk>(constants.downloadImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getDownloadCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Void, Disk> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
