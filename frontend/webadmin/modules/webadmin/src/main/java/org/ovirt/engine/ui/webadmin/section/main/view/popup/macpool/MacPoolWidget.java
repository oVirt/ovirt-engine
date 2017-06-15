package org.ovirt.engine.ui.webadmin.section.main.view.popup.macpool;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.Align;
import org.ovirt.engine.ui.common.widget.PatternFlyCompatible;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.macpool.MacPoolModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;

public class MacPoolWidget extends AbstractModelBoundPopupWidget<MacPoolModel> implements PatternFlyCompatible {

    interface Driver extends UiCommonEditorDriver<MacPoolModel, MacPoolWidget> {
    }

    private final Driver driver = GWT.create(Driver.class);

    interface WidgetUiBinder extends UiBinder<FlowPanel, MacPoolWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<MacPoolWidget> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    @UiField(provided = true)
    @Path(value = "allowDuplicates.entity")
    @WithElementId
    public EntityModelCheckBoxEditor allowDuplicates;

    @UiField
    @Ignore
    @WithElementId
    public MacRangeWidget macRanges;

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    public MacPoolWidget() {
        allowDuplicates = new EntityModelCheckBoxEditor(Align.RIGHT);
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);

        allowDuplicates.setLabel(constants.macPoolWidgetAllowDuplicates());
    }

    @Override
    public void edit(MacPoolModel model) {
        driver.edit(model);
        macRanges.edit(model.getMacRanges());
    }

    @Override
    public MacPoolModel flush() {
        macRanges.flush();
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setUsePatternFly(boolean use) {
        macRanges.setUsePatternFly(use);
    }

}
