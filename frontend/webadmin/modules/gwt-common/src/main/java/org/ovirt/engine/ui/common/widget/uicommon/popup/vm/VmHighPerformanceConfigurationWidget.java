package org.ovirt.engine.ui.common.widget.uicommon.popup.vm;


import org.ovirt.engine.ui.common.CommonApplicationTemplates;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.gin.AssetProvider;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.VmHighPerformanceConfigurationModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.safehtml.shared.SafeHtml;
import com.google.gwt.safehtml.shared.SafeHtmlBuilder;
import com.google.gwt.safehtml.shared.SafeHtmlUtils;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;

public class VmHighPerformanceConfigurationWidget extends AbstractModelBoundPopupWidget<VmHighPerformanceConfigurationModel> {

    interface Driver extends UiCommonEditorDriver<VmHighPerformanceConfigurationModel, VmHighPerformanceConfigurationWidget> {
    }

    interface ViewUiBinder extends UiBinder<FlowPanel, VmHighPerformanceConfigurationWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<VmHighPerformanceConfigurationWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    @UiField
    @Ignore
    HTML recommendationsList;

    @UiField
    FlowPanel recommendationsListPanel;

    @UiField
    @Ignore
    Label recommendationsListPanelTitle;

    @UiField
    @Ignore
    FlowPanel recommendationsListContent;

    private final Driver driver = GWT.create(Driver.class);

    private static final CommonApplicationTemplates templates = AssetProvider.getTemplates();

    public VmHighPerformanceConfigurationWidget() {
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        setVisibilityToRecommendationsListExpander(false);
    }

    private SafeHtml bulletedItem(String msg) {
        return templates.unorderedList(templates.listItem(SafeHtmlUtils.fromSafeConstant(msg)));
    }

    @Override
    public void edit(VmHighPerformanceConfigurationModel object) {
        driver.edit(object);

        if (object.getRecommendationsList().size() > 0) {
            setVisibilityToRecommendationsListExpander(true);
            SafeHtmlBuilder recommendationsListBuilder = new SafeHtmlBuilder();
            for (String field: object.getRecommendationsList()) {
                String escapedField = SafeHtmlUtils.htmlEscape(field).replaceAll("\n", "<br>"); //$NON-NLS-1$ //$NON-NLS-2$
               recommendationsListBuilder.append(bulletedItem(escapedField));
            }
            recommendationsList.setHTML(recommendationsListBuilder.toSafeHtml());
        }
    }

    @Override
    public VmHighPerformanceConfigurationModel flush() {
        return driver.flush();
    }

    private void setVisibilityToRecommendationsListExpander(boolean flag) {
        recommendationsListPanel.setVisible(flag);
        recommendationsListPanelTitle.setVisible(flag);
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
