package org.ovirt.engine.ui.common.widget.editor;

/**
 * Composite Editor that uses {@link EntityModelPasswordBox}.
 */
public class EntityModelPasswordBoxEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelPasswordBox> {

    public EntityModelPasswordBoxEditor() {
        super(new EntityModelPasswordBox());
    }

}
