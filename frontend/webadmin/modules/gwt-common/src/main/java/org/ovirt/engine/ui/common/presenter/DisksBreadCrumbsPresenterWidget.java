package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;

import com.google.web.bindery.event.shared.EventBus;

public class DisksBreadCrumbsPresenterWidget extends OvirtBreadCrumbsPresenterWidget<Disk, DiskListModel> {

    public interface DiskBreadCrumbsViewDef extends OvirtBreadCrumbsPresenterWidget.ViewDef<Disk> {
    }

    @Inject
    public DisksBreadCrumbsPresenterWidget(EventBus eventBus, DiskBreadCrumbsViewDef view,
            MainModelProvider<Disk, DiskListModel> listModelProvider) {
        super(eventBus, view, listModelProvider);
    }

}
