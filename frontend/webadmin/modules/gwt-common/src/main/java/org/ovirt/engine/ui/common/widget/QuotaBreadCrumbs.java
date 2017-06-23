package org.ovirt.engine.ui.common.widget;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.MenuLayout;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.web.bindery.event.shared.EventBus;

public class QuotaBreadCrumbs extends OvirtBreadCrumbs<Quota, QuotaListModel> {

    @Inject
    public QuotaBreadCrumbs(EventBus eventBus,
            MainModelProvider<Quota, QuotaListModel> listModelProvider,
            MenuLayout menuLayout) {
        super(eventBus, listModelProvider, menuLayout);
    }

    @Override
    protected SafeHtml getName(Quota item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(super.getName(item));
        builder.appendEscaped(" ("); // $NON-NLS-1$
        builder.appendEscaped(item.getStoragePoolName());
        builder.appendEscaped(")"); // $NON-NLS-1$
        return builder.toSafeHtml();
    }
}
