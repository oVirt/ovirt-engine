package org.ovirt.engine.ui.uicommonweb.validation;
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

@SuppressWarnings("unused")
public final class ValidationResult
{
	private boolean privateSuccess;
	public boolean getSuccess()
	{
		return privateSuccess;
	}
	public void setSuccess(boolean value)
	{
		privateSuccess = value;
	}
	private java.util.List<String> privateReasons;
	public java.util.List<String> getReasons()
	{
		return privateReasons;
	}
	public void setReasons(java.util.List<String> value)
	{
		privateReasons = value;
	}

	public ValidationResult()
	{
		setSuccess(true);
		setReasons(new java.util.ArrayList<String>());
	}
}