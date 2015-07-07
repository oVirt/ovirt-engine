package org.ovirt.engine.ui.userportal.widget.basic;

import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.ui.uicommonweb.models.vms.IconCache;
import com.google.gwt.editor.client.IsEditor;
import com.google.gwt.editor.client.adapters.TakesValueEditor;
import com.google.gwt.safehtml.shared.SafeUri;
import com.google.gwt.safehtml.shared.UriUtils;
import com.google.gwt.user.client.TakesValue;
import com.google.gwt.user.client.ui.Image;

public class IconImage extends Image implements IsEditor<TakesValueEditor<Guid>>, TakesValue<Guid> {

    private Guid value;

    @Override public TakesValueEditor<Guid> asEditor() {
        return TakesValueEditor.of(this);
    }

    @Override public void setValue(Guid value) {
        setUrl(toIconUrl(value));
        this.value = value;
    }

    private SafeUri toIconUrl(Guid iconId) {
        return UriUtils.fromTrustedString(IconCache.getInstance().getIcon(iconId));
    }

    @Override public Guid getValue() {
        return value;
    }
}
