package org.ovirt.engine.ui.common.widget.uicommon.popup;

import org.ovirt.engine.ui.common.widget.HasEditorDriver;
import org.ovirt.engine.ui.uicommonweb.models.Model;

import com.google.gwt.user.client.ui.Composite;

/**
 * Base class for widgets that represent the content of popup views bound to a UiCommon Window model.
 *
 * @param <T>
 *            Window model type.
 */
public abstract class AbstractModelBoundPopupWidget<T extends Model> extends Composite implements HasEditorDriver<T> {

}
