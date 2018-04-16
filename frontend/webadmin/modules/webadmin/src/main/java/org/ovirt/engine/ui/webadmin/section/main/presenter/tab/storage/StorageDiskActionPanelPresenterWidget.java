package org.ovirt.engine.ui.webadmin.section.main.presenter.tab.storage;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.DetailActionPanelPresenterWidget;
import org.ovirt.engine.ui.common.uicommon.model.SearchableDetailModelProvider;
import org.ovirt.engine.ui.common.widget.action.ActionButtonDefinition;
import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageDiskListModel;
import org.ovirt.engine.ui.uicommonweb.models.storage.StorageListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminButtonDefinition;
import org.ovirt.engine.ui.webadmin.widget.action.WebAdminMenuBarButtonDefinition;

import com.google.web.bindery.event.shared.EventBus;

public class StorageDiskActionPanelPresenterWidget extends
    DetailActionPanelPresenterWidget<Disk, StorageListModel, StorageDiskListModel> {

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public StorageDiskActionPanelPresenterWidget(EventBus eventBus,
            DetailActionPanelPresenterWidget.ViewDef<Disk> view,
            SearchableDetailModelProvider<Disk, StorageListModel, StorageDiskListModel> dataProvider) {
        super(eventBus, view, dataProvider);
    }

    @Override
    protected void initializeButtons() {
        addActionButton(new WebAdminButtonDefinition<Disk>(constants.removeDisk()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getRemoveCommand();
            }
        });

        // Upload operations drop down
        List<ActionButtonDefinition<Disk>> uploadActions = new ArrayList<>();
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageStart()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageCancel()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getCancelUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImagePause()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getPauseUploadCommand();
            }
        });
        uploadActions.add(new WebAdminButtonDefinition<Disk>(constants.uploadImageResume()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getResumeUploadCommand();
            }
        });
        addActionButton(new WebAdminMenuBarButtonDefinition<>(constants.uploadImage(), uploadActions), uploadActions);

        addActionButton(new WebAdminButtonDefinition<Disk>(constants.downloadImage()) {
            @Override
            protected UICommand resolveCommand() {
                return getDetailModel().getDownloadCommand();
            }
        });
    }

}
