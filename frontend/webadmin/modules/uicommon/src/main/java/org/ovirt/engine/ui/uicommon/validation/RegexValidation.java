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
public class RegexValidation implements IValidation
{

	//public const string NoSpacesRegex = @"^[^\s]+$";
	//public const string NoSpacesMessage = "This field can't contain spaces.";
	//public const string AtLeastOneCharRegex = @"[a-zA-Z]+";
	//public const string AtLeastOneCharMsg = "This field must contain at least one alphabetic character.";
	//public const string MemSizeRegex = @"^\d+\s*(m|mb|g|gb){0,1}\s*$";
	//public const string IpAddressRegex = @"^(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$";
	//public const string IpAddressMessage = "This field must contain an IP address in format xxx.xxx.xxx.xxx";
	//public const string EmailRegex = @"^[\w-]+(?:\.[\w-]+)*@(?:[\w-]+\.)+[a-zA-Z]{2,7}$";
	//public const string EmailMessage = "Invalid E-Mail address";
	//public const string MacRegex = @"^([\dabcdef]{2}:?){6}$";
	//public const string MacMessage = "Invalid MAC address";



	private String privateExpression;
	public String getExpression()
	{
		return privateExpression;
	}
	public void setExpression(String value)
	{
		privateExpression = value;
	}
	private String privateMessage;
	public String getMessage()
	{
		return privateMessage;
	}
	public void setMessage(String value)
	{
		privateMessage = value;
	}
	private boolean privateIsNegate;
	public boolean getIsNegate()
	{
		return privateIsNegate;
	}
	public void setIsNegate(boolean value)
	{
		privateIsNegate = value;
	}
	//public bool IgnoreCase { get; set; }


	public ValidationResult Validate(Object value)
	{
		ValidationResult result = new ValidationResult();

		//if (IgnoreCase)
		//{
		//    options = RegexOptions.IgnoreCase;
		//}

		if (value != null && value instanceof String && !StringHelper.isNullOrEmpty((String)value) && (getIsNegate() ? Regex.IsMatch(value.toString(), getExpression(), RegexOptions.None) : !Regex.IsMatch(value.toString(), getExpression(), RegexOptions.None)))
		{
			result.setSuccess(false);
			result.getReasons().add(getMessage());
		}

		return result;
	}
}