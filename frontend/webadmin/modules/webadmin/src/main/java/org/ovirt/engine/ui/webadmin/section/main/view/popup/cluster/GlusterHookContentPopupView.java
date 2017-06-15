package org.ovirt.engine.ui.webadmin.section.main.view.popup.cluster;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextAreaLabelEditor;
import org.ovirt.engine.ui.uicommonweb.models.clusters.GlusterHookContentModel;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.cluster.GlusterHookContentPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Label;
import com.google.inject.Inject;

public class GlusterHookContentPopupView extends AbstractModelBoundPopupView<GlusterHookContentModel> implements GlusterHookContentPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<GlusterHookContentModel, GlusterHookContentPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, GlusterHookContentPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<GlusterHookContentPopupView> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    WidgetStyle style;

    @UiField
    @Path(value = "content.entity")
    StringEntityModelTextAreaLabelEditor contentEditor;

    @UiField
    @Ignore
    Label messageLabel;

    @Inject
    public GlusterHookContentPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        applyStyles();
        driver.initialize(this);
    }

    private void applyStyles() {
        contentEditor.setContentWidgetContainerStyleName(style.content());
    }

    @Override
    public void edit(GlusterHookContentModel object) {
        driver.edit(object);
    }

    @Override
    public GlusterHookContentModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void setMessage(String message) {
        super.setMessage(message);
        messageLabel.setText(message);
    }

    interface WidgetStyle extends CssResource {
        String labelStyle();

        String content();
    }
}
