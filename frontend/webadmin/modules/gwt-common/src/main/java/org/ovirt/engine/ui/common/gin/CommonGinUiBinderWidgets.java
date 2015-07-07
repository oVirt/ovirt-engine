package org.ovirt.engine.ui.common.gin;

import org.ovirt.engine.ui.common.widget.editor.IconEditorWidget;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.SerialNumberPolicyWidget;
import com.google.gwt.inject.client.Ginjector;

/**
 * Ginjector extension containing views that make use of the GWTP GinUiBinder.
 *
 * Every view that makes use of GIN dependency injection and is to be embedded in a .ui.xml file --ex:
 * <pre>
 * <code>
 *     &lt;w:MyWidget />
 *
 *     public class MyWidget {
 *         {@literal @}Inject
 *         public MyWidget(MyOtherDependency dep) {
 *             // ...
 *         }
 *     }
 * </code>
 * </pre>
 *
 * Must be registered in this interface.
 */
public interface CommonGinUiBinderWidgets extends Ginjector {
    SerialNumberPolicyWidget getSerialNumberPolicyWidget();
    IconEditorWidget getIconEditorWidget();
}
