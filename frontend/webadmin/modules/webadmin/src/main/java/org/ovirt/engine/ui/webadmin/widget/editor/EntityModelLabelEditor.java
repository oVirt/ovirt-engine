package org.ovirt.engine.ui.webadmin.widget.editor;

public class EntityModelLabelEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelLabel> {

    /**
     * A ValueBoxWithLabelEditor that has a Label as the widget
     */
    public EntityModelLabelEditor() {
        super(new EntityModelLabel());
    }

}
