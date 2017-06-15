package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.editor.UiCommonEditorDriver;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.editor.generic.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EntityModelCheckBoxWidget extends Composite implements HasEditorDriver<EntityModel<Boolean>>, HasElementId, FocusableComponentsContainer {

    interface Driver extends UiCommonEditorDriver<EntityModel<Boolean>, EntityModelCheckBoxWidget> {
    }

    interface ViewUiBinder extends UiBinder<Widget, EntityModelCheckBoxWidget> {
        ViewUiBinder uiBinder = GWT.create(ViewUiBinder.class);
    }

    @Ignore
    @UiField
    Label checkBoxTitle;

    @Path(value = "entity")
    @UiField(provided = true)
    EntityModelCheckBoxEditor checkBoxEditor;

    private final Driver driver = GWT.create(Driver.class);

    public EntityModelCheckBoxWidget(Align align, String title, String label) {
        checkBoxEditor = new EntityModelCheckBoxEditor(align);
        checkBoxEditor.setLabel(label);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        driver.initialize(this);

        checkBoxTitle.setText(title);
    }

    @Override
    public int setTabIndexes(int nextTabIndex) {
        checkBoxEditor.setTabIndex(nextTabIndex++);
        return nextTabIndex;
    }

    @Override
    public void setElementId(String elementId) {
        checkBoxEditor.setElementId(ElementIdUtils.createElementId(elementId, "checkBox")); //$NON-NLS-1$
    }

    @Override
    public void edit(EntityModel<Boolean> object) {
        driver.edit(object);
    }

    @Override
    public EntityModel<Boolean> flush() {
        return driver.flush();
    }

    @Override
    public void cleanup() {
        driver.cleanup();
    }
}
