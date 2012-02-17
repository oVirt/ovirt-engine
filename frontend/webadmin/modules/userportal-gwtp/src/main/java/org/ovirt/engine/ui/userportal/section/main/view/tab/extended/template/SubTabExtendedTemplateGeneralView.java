package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateGeneralPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.template.TemplateGeneralModelProvider;

import com.google.gwt.editor.client.Editor;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class SubTabExtendedTemplateGeneralView extends AbstractSubTabFormView<VmTemplate, UserPortalTemplateListModel, TemplateGeneralModel>
        implements SubTabExtendedTemplateGeneralPresenter.ViewDef, Editor<TemplateGeneralModel> {

    private Label helloLabel = new Label("");

    @Inject
    public SubTabExtendedTemplateGeneralView(TemplateGeneralModelProvider modelProvider) {
        super(modelProvider);
        initWidget(helloLabel);
    }

    @Override
    public void setMainTabSelectedItem(VmTemplate selectedItem) {
        // TODO
    }

    @Override
    public void editTemplate(VmTemplate entity) {
        helloLabel.setText("Template name: " + entity.getname());
    }

}
