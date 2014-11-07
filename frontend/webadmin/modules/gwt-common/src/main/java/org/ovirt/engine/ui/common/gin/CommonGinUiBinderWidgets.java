package org.ovirt.engine.ui.common.gin;

import com.google.gwt.inject.client.Ginjector;
import org.ovirt.engine.ui.common.widget.uicommon.popup.vm.SerialNumberPolicyWidget;

/**
 * Ginjector extension containing views that make use of the GWTP GinUiBinder.
 *
 * Every view that makes use of GIN dependency injection and is to be embedded in a .ui.xml file --ex:
 * <code>
 *     <w:MyWidget />
 *
 *     public class MyWidget {
 *         @Inject
 *         public MyWidget(ApplicationResources resources, MyOtherDependency dep) {
 *             // ...
 *         }
 *     }
 * </code>
 *
 * Must be registered in this interface.
 */
public interface CommonGinUiBinderWidgets extends Ginjector {
    SerialNumberPolicyWidget getSerialNumberPolicyWidget();
}
