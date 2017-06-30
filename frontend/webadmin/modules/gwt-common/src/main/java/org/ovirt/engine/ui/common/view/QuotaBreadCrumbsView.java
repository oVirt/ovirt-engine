package org.ovirt.engine.ui.common.view;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Quota;
import org.ovirt.engine.ui.common.presenter.QuotaBreadCrumbsPresenterWidget.QuotaBreadCrumbsViewDef;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.tab.MenuLayout;
import org.ovirt.engine.ui.uicommonweb.models.quota.QuotaListModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class QuotaBreadCrumbsView extends OvirtBreadCrumbsView<Quota, QuotaListModel> implements QuotaBreadCrumbsViewDef {

    @Inject
    public QuotaBreadCrumbsView(MenuLayout menuLayout, MainModelProvider<Quota, QuotaListModel> listModelProvider) {
        super(menuLayout, listModelProvider);
    }

    @Override
    public SafeHtml getName(Quota item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(super.getName(item));
        builder.appendEscaped(" ("); // $NON-NLS-1$
        builder.appendEscaped(item.getStoragePoolName());
        builder.appendEscaped(")"); // $NON-NLS-1$
        return builder.toSafeHtml();
    }

}
