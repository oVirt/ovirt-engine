package org.ovirt.engine.ui.uicommon.models.autocomplete;
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
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class SuggestItemPartModel extends Model
{

	private SuggestItemPartType privatePartType = SuggestItemPartType.values()[0];
	public SuggestItemPartType getPartType()
	{
		return privatePartType;
	}
	public void setPartType(SuggestItemPartType value)
	{
		privatePartType = value;
	}
	private String privatePartString;
	public String getPartString()
	{
		return privatePartString;
	}
	public void setPartString(String value)
	{
		privatePartString = value;
	}

}