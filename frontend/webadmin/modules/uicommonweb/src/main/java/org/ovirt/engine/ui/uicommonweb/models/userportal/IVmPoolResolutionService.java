package org.ovirt.engine.ui.uicommonweb.models.userportal;
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

import org.ovirt.engine.ui.uicommonweb.dataprovider.*;
import org.ovirt.engine.ui.uicommonweb.models.configure.*;
import org.ovirt.engine.ui.uicommonweb.models.pools.*;
import org.ovirt.engine.ui.uicommonweb.models.vms.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.*;
import org.ovirt.engine.core.common.businessentities.*;

import org.ovirt.engine.core.common.interfaces.*;
import org.ovirt.engine.ui.uicommonweb.*;
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public interface IVmPoolResolutionService
{
	vm_pools ResolveVmPoolById(Guid id);
}