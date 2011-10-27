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
public class LengthValidation implements IValidation
{
	private int privateMaxLength;
	public int getMaxLength()
	{
		return privateMaxLength;
	}
	public void setMaxLength(int value)
	{
		privateMaxLength = value;
	}

	public LengthValidation()
	{
		setMaxLength(Integer.MAX_VALUE);
	}

	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();

		if (value != null && value instanceof String && ((String)value).length() > getMaxLength())
		{
			result.setSuccess(false);
			result.getReasons().add(StringFormat.format("Field content must not exceed %1$s characters.", getMaxLength()));
		}

		return result;
	}
}