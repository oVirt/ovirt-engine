package org.ovirt.engine.ui.webadmin.section.main.view.popup.host;

import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.widget.dialog.AdvancedParametersExpander;
import org.ovirt.engine.ui.common.widget.editor.generic.IntegerEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.hosts.VfsConfigModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationMessages;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.text.shared.AbstractRenderer;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.ValueLabel;
import com.google.inject.Inject;

public class VfsConfigWidget extends AbstractModelBoundPopupWidget<VfsConfigModel> {

    interface Driver extends SimpleBeanEditorDriver<VfsConfigModel, VfsConfigWidget> {
    }

    interface WidgetUiBinder extends UiBinder<FlowPanel, VfsConfigWidget> {
        WidgetUiBinder uiBinder = GWT.create(WidgetUiBinder.class);
    }

    interface WidgetIdHandler extends ElementIdHandler<VfsConfigWidget> {
        WidgetIdHandler idHandler = GWT.create(WidgetIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    interface WidgetStyle extends CssResource {
        String valueWidth();
    }

    @UiField
    WidgetStyle style;

    @UiField
    @Ignore
    AdvancedParametersExpander numVfsExpander;

    @UiField
    @Ignore
    FlowPanel numVfsExpanderContent;

    @UiField
    @Path(value = "numOfVfs.entity")
    IntegerEntityModelTextBoxEditor numOfVfs;

    @UiField(provided = true)
    @Path(value = "maxNumOfVfs.entity")
    ValueLabel<Integer> maxVfsLabel;

    @Inject
    public VfsConfigWidget(ApplicationConstants constants, final ApplicationMessages messages) {
        maxVfsLabel = new ValueLabel<>(new AbstractRenderer<Integer>() {

            @Override
            public String render(Integer object) {
                return messages.maxVfs(object);
            }
        });
        initWidget(WidgetUiBinder.uiBinder.createAndBindUi(this));
        WidgetIdHandler.idHandler.generateAndSetIds(this);

        initExpander(constants);
        localize(constants);
        driver.initialize(this);
        addStyles();
    }

    private void initExpander(ApplicationConstants constants) {
        numVfsExpander.initWithContent(numVfsExpanderContent.getElement());
        numVfsExpander.setTitleWhenExpanded(constants.numOfVfsSetting());
        numVfsExpander.setTitleWhenCollapsed(constants.numOfVfsSetting());
    }

    private void localize(ApplicationConstants constants) {
        numOfVfs.setLabel(constants.numOfVfs());
    }

    protected void addStyles() {
        numOfVfs.addContentWidgetContainerStyleName(style.valueWidth());
    }

    interface Style extends CssResource {
        String valueBox();
    }

    @Override
    public void edit(VfsConfigModel model) {
        driver.edit(model);
    }

    @Override
    public VfsConfigModel flush() {
        return driver.flush();
    }
}
