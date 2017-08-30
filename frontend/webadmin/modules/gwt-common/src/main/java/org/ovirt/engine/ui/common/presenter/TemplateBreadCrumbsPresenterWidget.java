package org.ovirt.engine.ui.common.presenter;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.uicommon.model.MainModelProvider;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateListModel;

import com.google.web.bindery.event.shared.EventBus;

public class TemplateBreadCrumbsPresenterWidget extends OvirtBreadCrumbsPresenterWidget<VmTemplate, TemplateListModel> {

    public interface TemplateBreadCrumbsViewDef extends OvirtBreadCrumbsPresenterWidget.ViewDef<VmTemplate> {
    }

    @Inject
    public TemplateBreadCrumbsPresenterWidget(EventBus eventBus,
            TemplateBreadCrumbsViewDef view,
            MainModelProvider<VmTemplate, TemplateListModel> listModelProvider) {
        super(eventBus, view, listModelProvider);
    }

}
