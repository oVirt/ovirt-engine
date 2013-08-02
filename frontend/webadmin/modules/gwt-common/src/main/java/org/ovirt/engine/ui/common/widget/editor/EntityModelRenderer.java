package org.ovirt.engine.ui.common.widget.editor;

import com.google.gwt.text.shared.AbstractRenderer;

/**
 * @deprecated use the org.ovirt.engine.ui.common.widget.editor.generic.ToStringEntityModelRenderer instead
 */
@Deprecated
public class EntityModelRenderer extends AbstractRenderer<Object> {

    @Override
    public String render(Object entity) {
        return entity == null ? null : entity.toString();
    }

}
