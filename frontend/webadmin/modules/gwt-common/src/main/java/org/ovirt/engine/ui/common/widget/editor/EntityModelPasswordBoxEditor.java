package org.ovirt.engine.ui.common.widget.editor;

/**
 * Composite Editor that uses {@link EntityModelPasswordBox}.
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.EntityModelPasswordBox instead
 */
@Deprecated
public class EntityModelPasswordBoxEditor extends AbstractValueBoxWithLabelEditor<Object, EntityModelPasswordBox> {

    public EntityModelPasswordBoxEditor() {
        super(new EntityModelPasswordBox());
    }

}
