package org.ovirt.engine.ui.webadmin.section.main.view.popup.tag;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.view.popup.AbstractModelBoundPopupView;
import org.ovirt.engine.ui.common.widget.dialog.SimpleDialogPanel;
import org.ovirt.engine.ui.common.widget.editor.generic.StringEntityModelTextBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.tags.TagModel;
import org.ovirt.engine.ui.webadmin.ApplicationConstants;
import org.ovirt.engine.ui.webadmin.gin.AssetProvider;
import org.ovirt.engine.ui.webadmin.section.main.presenter.popup.tag.TagPopupPresenterWidget;

import com.google.gwt.core.client.GWT;
import com.google.gwt.event.shared.EventBus;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.inject.Inject;

public class TagPopupView extends AbstractModelBoundPopupView<TagModel> implements TagPopupPresenterWidget.ViewDef {

    interface Driver extends UiCommonEditorDriver<TagModel, TagPopupView> {
    }

    interface ViewUiBinder extends UiBinder<SimpleDialogPanel, TagPopupView> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @UiField
    @Path(value = "name.entity")
    StringEntityModelTextBoxEditor nameEditor;

    @UiField
    @Path(value = "description.entity")
    StringEntityModelTextBoxEditor descriptionEditor;

    private final Driver driver = GWT.create(Driver.class);

    private static final ApplicationConstants constants = AssetProvider.getConstants();

    @Inject
    public TagPopupView(EventBus eventBus) {
        super(eventBus);
        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        localize();
        driver.initialize(this);
    }

    void localize() {
        nameEditor.setLabel(constants.tagPopupNameLabel());
        descriptionEditor.setLabel(constants.tagPopupDescriptionLabel());
    }

    @Override
    public void edit(TagModel object) {
        driver.edit(object);
    }

    @Override
    public TagModel flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }

    @Override
    public void focusInput() {
        nameEditor.setFocus(true);
    }

}
