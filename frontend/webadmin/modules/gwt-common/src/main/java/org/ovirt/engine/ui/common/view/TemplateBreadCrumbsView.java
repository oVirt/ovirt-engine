package org.ovirt.engine.ui.common.view;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.presenter.TemplateBreadCrumbsPresenterWidget.TemplateBreadCrumbsViewDef;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.common.widget.MenuDetailsProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;

import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;

public class TemplateBreadCrumbsView extends OvirtBreadCrumbsView<VmTemplate, TemplateListModel>
    implements TemplateBreadCrumbsViewDef {

    @Inject
    public TemplateBreadCrumbsView(MainModelProvider<VmTemplate, TemplateListModel> listModelProvider,
            MenuDetailsProvider menuDetailsProvider) {
        super(listModelProvider, menuDetailsProvider);
    }

    @Override
    public SafeHtml getName(VmTemplate item) {
        SafeHtmlBuilder builder = new SafeHtmlBuilder();
        builder.append(super.getName(item));
        if (!item.getId().equals(item.getBaseTemplateId())) {
            if (item.getTemplateVersionName() != null) {
                builder.appendEscaped("/"); // $NON-NLS-1$
                builder.appendEscaped(item.getTemplateVersionName());
            }
            builder.appendEscaped(" ("); // $NON-NLS-1$
            builder.append(item.getTemplateVersionNumber());
            builder.appendEscaped(")"); // $NON-NLS-1$
        }
        return builder.toSafeHtml();
    }
}
