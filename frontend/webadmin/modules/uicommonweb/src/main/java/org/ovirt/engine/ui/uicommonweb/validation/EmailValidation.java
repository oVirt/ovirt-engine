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
public class EmailValidation implements IValidation
{
	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();

		try
		{
			new MailAddress((String)value);
		}
		catch (RuntimeException e)
		{
			result.setSuccess(false);
			result.getReasons().add("Invalid E-Mail address");
		}

		return result;
	}
}