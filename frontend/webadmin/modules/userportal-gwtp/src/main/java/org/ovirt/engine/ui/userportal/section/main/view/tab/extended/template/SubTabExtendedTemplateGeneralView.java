package org.ovirt.engine.ui.userportal.section.main.view.tab.extended.template;

import org.ovirt.engine.core.common.businessentities.VmTemplate;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.uicommon.template.TemplateGeneralModelForm;
import org.ovirt.engine.ui.uicommonweb.models.templates.TemplateGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.userportal.UserPortalTemplateListModel;
import org.ovirt.engine.ui.userportal.section.main.presenter.tab.extended.template.SubTabExtendedTemplateGeneralPresenter;
import org.ovirt.engine.ui.userportal.uicommon.model.UserPortalDetailModelProvider;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Widget;
import com.google.inject.Inject;

public class SubTabExtendedTemplateGeneralView extends AbstractSubTabFormView<VmTemplate, UserPortalTemplateListModel, TemplateGeneralModel>
        implements SubTabExtendedTemplateGeneralPresenter.ViewDef {

    interface ViewIdHandler extends ElementIdHandler<SubTabExtendedTemplateGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface ViewUiBinder extends UiBinder<Widget, SubTabExtendedTemplateGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField(provided = true)
    @WithElementId
    TemplateGeneralModelForm form;

    @Inject
    public SubTabExtendedTemplateGeneralView(
            UserPortalDetailModelProvider<UserPortalTemplateListModel, TemplateGeneralModel> modelProvider) {
        super(modelProvider);
        form = new TemplateGeneralModelForm(modelProvider);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));

        generateIds();

        form.initialize();
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainTabSelectedItem(VmTemplate selectedItem) {
        form.update();
    }

}
