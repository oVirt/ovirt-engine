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

public class DiskActionPanelPresenterWidget extends ActionPanelPresenterWidget<Disk, DiskListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    private WebAdminButtonDefinition<Disk> newButtonDefinition;

    @Inject
    public DiskActionPanelPresenterWidget(EventBus eventBus,
            ActionPanelPresenterWidget.ViewDef<Disk> view,
            MainModelProvider<Disk, DiskListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        newButtonDefinition = new WebAdminButtonDefinition<Disk>(constants.newDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getNewCommand();
            }
        };
        addActionButton(newButtonDefinition);

        addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getRemoveCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Disk>(constants.moveDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getMoveCommand();
            }
        });

        addActionButton(new WebAdminButtonDefinition<Disk>(constants.copyDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCopyCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Disk>(constants.exportDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getExportCommand();
            }
        });

        addMenuListItem(new WebAdminButtonDefinition<Disk>(constants.assignQuota()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getChangeQuotaCommand();
            }
        });

        // Upload operations drop down
        List<ActionButtonDefinition<Disk>> uploadActions = new LinkedList<>();
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageStart()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageCancel()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getCancelUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImagePause()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getPauseUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageResume()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getResumeUploadCommand();
            }
        });
        addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.uploadImage(),
                uploadActions), uploadActions);

        addActionButton(new WebAdminButtonDefinition<Disk>(constants.downloadImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getModel().getDownloadCommand();
            }
        });
    }

    public WebAdminButtonDefinition<Disk> getNewButtonDefinition() {
        return newButtonDefinition;
    }
}
