package org.ovirt.engine.ui.webadmin.section.main.view.tab.provider;

import javax.inject.Inject;

import org.ovirt.engine.core.common.businessentities.Provider;
import org.ovirt.engine.core.common.businessentities.ProviderType;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.uicommon.model.DetailModelProvider;
import org.ovirt.engine.ui.common.view.AbstractSubTabFormView;
import org.ovirt.engine.ui.common.widget.form.FormBuilder;
import org.ovirt.engine.ui.common.widget.form.FormItem;
import org.ovirt.engine.ui.common.widget.form.GeneralFormPanel;
import org.ovirt.engine.ui.common.widget.label.StringValueLabel;
import org.ovirt.engine.ui.common.widget.renderer.EnumRenderer;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderGeneralModel;
import org.ovirt.engine.ui.uicommonweb.models.providers.ProviderListModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.tab.provider.SubTabProviderGeneralPresenter;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.Editor;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.gwt.user.client.ui.Widget;

public class SubTabProviderGeneralView extends AbstractSubTabFormView<Provider, ProviderListModel, ProviderGeneralModel> implements SubTabProviderGeneralPresenter.ViewDef, Editor<ProviderGeneralModel> {

    interface ViewUiBinder extends UiBinder<Widget, SubTabProviderGeneralView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<SubTabProviderGeneralView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    interface Driver extends UiCommonEditorDriver<ProviderGeneralModel, SubTabProviderGeneralView> { }

    private final Driver driver = GWT.create(Driver.class);

    StringValueLabel name = new StringValueLabel();
    ValueLabel<ProviderType> type = new ValueLabel<>(new EnumRenderer<ProviderType>());
    StringValueLabel description = new StringValueLabel();
    StringValueLabel url = new StringValueLabel();

    @UiField(provided = true)
    @WithElementId
    GeneralFormPanel formPanel;

    FormBuilder formBuilder;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public SubTabProviderGeneralView(DetailModelProvider<ProviderListModel, ProviderGeneralModel> modelProvider) {
        super(modelProvider);

        // Init formPanel
        formPanel = new GeneralFormPanel();

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        generateIds();

        // Build a form using the FormBuilder
        formBuilder = new FormBuilder(formPanel, 1, 4);

        formBuilder.addFormItem(new FormItem(constants.nameProvider(), name, 0, 0),  2, 10);
        formBuilder.addFormItem(new FormItem(constants.typeProvider(), type, 1, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.descriptionProvider(), description, 2, 0), 2, 10);
        formBuilder.addFormItem(new FormItem(constants.urlProvider(), url, 3, 0), 2, 10);
    }

    @Override
    protected void generateIds() {
        ViewIdHandler.idHandler.generateAndSetIds(this);
    }

    @Override
    public void setMainSelectedItem(Provider selectedItem) {
        driver.edit(getDetailModel());
        formBuilder.update(getDetailModel());
    }

}
