package org.ovirt.engine.ui.webadmin.widget.provider;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.dialog.InfoIcon;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.providers.KubevirtPropertiesModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationTemplates;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class KubevirtPropertiesWidget extends AbstractModelBoundPopupWidget<KubevirtPropertiesModel> {

    interface Driver extends UiCommonEditorDriver<KubevirtPropertiesModel, KubevirtPropertiesWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface ViewUiBinder extends UiBinder<FlowPanel, KubevirtPropertiesWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<KubevirtPropertiesWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private static final ApplicationConstants constants = AssetProvider.getConstants();
    private static final ApplicationTemplates templates = AssetProvider.getTemplates();

    @UiField
    @Ignore
    AdvancedParametersExpander expander;

    @UiField
    @Ignore
    FlowPanel expanderContent;

    @UiField(provided = true)
    @Ignore
    InfoIcon certificateAuthorityInfoIcon;

    @UiField
    @Path("certificateAuthority.entity")
    @WithElementId("certificateAuthority")
    StringEntityModelTextAreaEditor certificateAuthority;

    @UiField(provided = true)
    @Ignore
    InfoIcon prometheusUrlInfoIcon;

    @UiField
    @Path("prometheusUrl.entity")
    @WithElementId("prometheusUrl")
    StringEntityModelTextBoxEditor prometheusUrl;

    @UiField(provided = true)
    @Ignore
    InfoIcon prometheusCertificateAuthorityInfoIcon;

    @UiField
    @Path("prometheusCertificateAuthority.entity")
    @WithElementId("prometheusCertificateAuthority")
    StringEntityModelTextAreaEditor prometheusCertificateAuthority;

    public KubevirtPropertiesWidget() {
        initInfoIcon();
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        initExpander();
        ViewIdHandler.idHandler.generateAndSetIds(this);

        driver.initialize(this);
    }

    private void initExpander() {
        expander.initWithContent(expanderContent.getElement());
    }

    private void initInfoIcon() {
        certificateAuthorityInfoIcon =
                new InfoIcon(templates.italicText(constants.kubevirtCertificateAuthorityHelpMessage()));
        prometheusUrlInfoIcon =
                new InfoIcon(templates.italicText(constants.prometheusUrlHelpMessage()));
        prometheusCertificateAuthorityInfoIcon =
                new InfoIcon(templates.italicText(constants.prometheusCertificateAuthorityHelpMessage()));
    }

    @Override
    public void edit(KubevirtPropertiesModel object) {
        driver.edit(object);
    }

    @Override
    public KubevirtPropertiesModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        certificateAuthority.setTabIndex(nextTabIndex++);
        prometheusUrl.setTabIndex(nextTabIndex++);
        prometheusCertificateAuthority.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}
