package org.ovirt.engine.ui.webadmin.section.main.view.popup.vm;

import org.ovirt.engine.ui.uicommonweb.models.userportal.AttachCdModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.ApplicationResources;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.vm.VmChangeCDPopupPresenterWidget;
import org.ovirt.engine.ui.webadmin.section.main.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.webadmin.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.webadmin.widget.editor.ListModelListBoxEditor;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.resources.client.CssResource;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class VmChangeCDPopupView extends AbstractModelBoundPopupView<AttachCdModel>
        implements VmChangeCDPopupPresenterWidget.ViewDef {

    interface Driver extends SimpleBeanEditorDriver<AttachCdModel, VmChangeCDPopupView> {
        Driver driver = GWT.create(Driver.class);
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, VmChangeCDPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    Style style;

    @UiField
    @Path(value = "isoImage.selectedItem")
    ListModelListBoxEditor<Object> isoImageEditor;

    @Inject
    public VmChangeCDPopupView(EventBus eventBus, ApplicationResources resources, ApplicationConstants constants) {
        super(eventBus, resources);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        addStyles();
        Driver.driver.initialize(this);
    }

    void addStyles() {
        isoImageEditor.addLabelStyleName(style.isoImageEditorLabel());
        isoImageEditor.addContentWidgetStyleName(style.isoImageEditorBox());
    }

    @Override
    public void edit(AttachCdModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public AttachCdModel flush() {
        return Driver.driver.flush();
    }

    @Override
    public void focusInput() {
        isoImageEditor.setFocus(true);
    }

    interface Style extends CssResource {
        String isoImageEditorLabel();

        String isoImageEditorBox();
    }

}
