package org.ovirt.engine.ui.common.widget;

import org.ovirt.engine.ui.common.CommonApplicationConstants;
import org.ovirt.engine.ui.common.idhandler.HasElementId;
import org.ovirt.engine.ui.common.utils.ElementIdUtils;
import org.ovirt.engine.ui.common.view.popup.FocusableComponentsContainer;
import org.ovirt.engine.ui.common.widget.editor.EntityModelCheckBoxEditor;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;

import com.google.gwt.core.client.GWT;
import com.google.gwt.editor.client.SimpleBeanEditorDriver;
import com.google.gwt.uibinder.client.UiBinder;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.client.ui.Composite;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.Widget;

public class EntityModelCheckBoxWidget extends Composite implements HasEditorDriver<EntityModel>, HasElementId, FocusableComponentsContainer {

    interface Driver extends SimpleBeanEditorDriver<EntityModel, EntityModelCheckBoxWidget> {
        Driver driver = GWT.create(Driver.class);
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

    CommonApplicationConstants constants;

    public EntityModelCheckBoxWidget(Align align, String title, String label) {
        checkBoxEditor = new EntityModelCheckBoxEditor(align);
        checkBoxEditor.setLabel(label);

        initWidget(ViewUiBinder.uiBinder.createAndBindUi(this));
        Driver.driver.initialize(this);

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
    public void edit(EntityModel object) {
        Driver.driver.edit(object);
    }

    @Override
    public EntityModel flush() {
        return Driver.driver.flush();
    }

}
