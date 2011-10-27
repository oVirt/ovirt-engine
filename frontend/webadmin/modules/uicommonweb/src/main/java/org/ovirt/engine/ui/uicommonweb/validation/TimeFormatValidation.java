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
public class TimeFormatValidation implements IValidation
{
	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();

		if (value != null && value instanceof String && !((String)value).equals(""))
		{
			CultureInfo ci = CultureInfo.CurrentCulture;
			java.util.Date dtValue = new java.util.Date(0);

			RefObject<java.util.Date> tempRef_dtValue = new RefObject<java.util.Date>(dtValue);
			boolean tempVar = !DateTime.TryParseExact((String)value, "t", ci.DateTimeFormat, DateTimeStyles.None, tempRef_dtValue);
				dtValue = tempRef_dtValue.argvalue;
			if (tempVar)
			{
				result.setSuccess(false);
				result.getReasons().add("The field must contain a time value");
			}
		}

		return result;
	}
}