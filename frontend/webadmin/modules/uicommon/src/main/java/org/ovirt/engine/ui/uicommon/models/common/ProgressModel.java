package org.ovirt.engine.ui.uicommon.models.common;
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
public class ProgressModel extends Model
{

	private String currentOperation;
	public String getCurrentOperation()
	{
		return currentOperation;
	}
	public void setCurrentOperation(String value)
	{
		if (!StringHelper.stringsEqual(currentOperation, value))
		{
			currentOperation = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CurrentOperation"));
		}
	}

}