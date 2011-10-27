package org.ovirt.engine.ui.uicommon.validation;
import java.util.Collections;
import org.ovirt.engine.core.compat.*;
import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.core.common.businessentities.*;
import org.ovirt.engine.core.common.vdscommands.*;
import org.ovirt.engine.core.common.queries.*;
import org.ovirt.engine.core.common.action.*;
import org.ovirt.engine.ui.frontend.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;
import org.ovirt.engine.core.common.*;

import org.ovirt.engine.ui.uicompat.*;
import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public class HostAddressValidation implements IValidation
{
	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();

		String val = (String)value;
		if (StringHelper.isNullOrEmpty(val) || !UriValidator.IsValid(val))
		{
			result.setSuccess(false);
			result.getReasons().add("Address is not a valid host name or IP address.");
		}
		return result;
	}
}