package org.ovirt.engine.ui.uicommonweb;
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

import org.ovirt.engine.ui.uicommonweb.models.*;
import org.ovirt.engine.ui.uicommonweb.models.vms.*;

@SuppressWarnings("unused")
public enum ValidateServerCertificateEnum
{
	// Validate server certificate.
	TRUE,
	// Don't validate server certificate.
	FALSE,
	// Validate server certificate only if we browse in ssl.
	AUTO;

	public int getValue()
	{
		return this.ordinal();
	}

	public static ValidateServerCertificateEnum forValue(int value)
	{
		return values()[value];
	}
}