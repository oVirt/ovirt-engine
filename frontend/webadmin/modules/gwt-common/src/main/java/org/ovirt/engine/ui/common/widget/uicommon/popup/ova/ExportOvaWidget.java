package org.ovirt.engine.ui.common.widget.uicommon.popup.ova;

import org.gwtbootstrap3.client.ui.Container;
import org.ovirt.engine.core.common.businessentities.VDS;
import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.ElementIdHandler;
import org.ovirt.engine.ui.common.idhandler.WithElementId;
import org.ovirt.engine.ui.common.widget.editor.ListModelListBoxEditor;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.common.widget.renderer.NullSafeRenderer;
import org.ovirt.engine.ui.common.widget.uicommon.popup.AbstractModelBoundPopupWidget;
import org.ovirt.engine.ui.uicommonweb.models.vms.ExportOvaModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.Label;

public class ExportOvaWidget extends AbstractModelBoundPopupWidget<ExportOvaModel> {

    interface Driver extends UiCommonEditorDriver<ExportOvaModel, ExportOvaWidget> {
    }

    interface ViewUiBinder extends UiBinder<Container, ExportOvaWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    interface ViewIdHandler extends ElementIdHandler<ExportOvaWidget> {
        ViewIdHandler idHandler = GWT.create(ViewIdHandler.class);
    }

    private final Driver driver = GWT.create(Driver.class);

    @UiField
    @Path(value = "path.entity")
    @WithElementId("path")
    StringEntityModelTextBoxEditor pathEditor;

    @UiField
    @Path(value = "name.entity")
    @WithElementId("name")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField(provided = true)
    @Path(value = "proxy.selectedItem")
    @WithElementId("proxy")
    ListModelListBoxEditor<VDS> proxyEditor;

    @UiField
    @WithElementId("Message")
    FlowPanel messagePanel;

    public ExportOvaWidget() {
        proxyEditor = new ListModelListBoxEditor<>(new NullSafeRenderer<VDS>() {
            @Override
            protected String renderNullSafe(VDS object) {
                return object.getName();
            }
        });
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        ViewIdHandler.idHandler.generateAndSetIds(this);
        driver.initialize(this);
    }

    @Override
    public void edit(ExportOvaModel object) {
        driver.edit(object);

        object.getPropertyChangedEvent().addListener((ev, sender, args) -> {
            String propName = args.propertyName;
            if ("Message".equals(propName)) { //$NON-NLS-1$
                appendMessage(object.getMessage());
            }
        });
    }

    public void appendMessage(String message) {
        if (message == null) {
            return;
        }

        messagePanel.add(new Label(message));
    }

    @Override
    public ExportOvaModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }


    @Override
    public void focusInput() {
        proxyEditor.setFocus(true);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        proxyEditor.setTabIndex(nextTabIndex++);
        pathEditor.setTabIndex(nextTabIndex++);
        nameEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }
}
