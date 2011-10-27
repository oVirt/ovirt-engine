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
public class AsciiOrNoneValidation implements IValidation
{

	public static final String ONLY_ASCII_OR_NONE = "[^\u0000-\u007F]";


	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();
		//note: in backend java code the regex is [\\p{ASCII}]* which is not compatible with c#
		if (value != null && Regex.IsMatch(value.toString(), ONLY_ASCII_OR_NONE, RegexOptions.None))
		{
			result.setSuccess(false);
			result.setReasons(new java.util.ArrayList<String>(java.util.Arrays.asList(new String[] { "The field contains special characters. Only numbers, letters, '-' and '_' are allowed." })));
		}
		return result;
	}
}