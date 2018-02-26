package org.ovirt.engine.ui.common.view;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.storage.Disk;
import org.ovirt.engine.ui.common.presenter.DisksBreadCrumbsPresenterWidget.DiskBreadCrumbsViewDef;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.uicommonweb.models.disks.DiskListModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class DisksBreadCrumbsView extends OvirtBreadCrumbsView<Disk, DiskListModel> implements DiskBreadCrumbsViewDef {

    @Inject
    public DisksBreadCrumbsView(MainModelProvider<Disk, DiskListModel> listModelProvider,
            MenuDetailsProvider menuDetailsProvider) {
        super(listModelProvider, menuDetailsProvider);
    }

    @Override
    public SafeHtml getName(Disk item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(super.getName(item));
        builder.appendEscaped(" ("); // $NON-NLS-1$
        builder.appendEscaped(item.getId().toString());
        builder.appendEscaped(")"); // $NON-NLS-1$
        return builder.toSafeHtml();
    }
}
