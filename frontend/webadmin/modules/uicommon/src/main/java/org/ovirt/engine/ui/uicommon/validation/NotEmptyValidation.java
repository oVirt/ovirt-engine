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

import org.ovirt.engine.ui.uicommon.*;

@SuppressWarnings("unused")
public class NotEmptyValidation implements IValidation
{
	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();

		if (value == null || (value instanceof String && StringHelper.isNullOrEmpty((String)value)))
		{
			result.setSuccess(false);
			result.getReasons().add("This field can't be empty.");
		}

		return result;
	}
}