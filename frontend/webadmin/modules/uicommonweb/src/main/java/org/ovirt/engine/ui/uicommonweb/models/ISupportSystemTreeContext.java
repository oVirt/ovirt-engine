package org.ovirt.engine.ui.uicommonweb.models;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicommonweb.*;

/**
 Implements this interface if type should be aware of what is selected
 in system tree. Assignment controlled by a CommonModel.
*/
@SuppressWarnings("unused")
public interface ISupportSystemTreeContext
{
	SystemTreeItemModel getSystemTreeSelectedItem();
	void setSystemTreeSelectedItem(SystemTreeItemModel value);
}