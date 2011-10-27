package org.ovirt.engine.ui.uicommon.models.userportal;
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
import org.ovirt.engine.ui.uicommon.validation.*;
import org.ovirt.engine.ui.uicommon.*;
import org.ovirt.engine.ui.uicommon.models.*;

@SuppressWarnings("unused")
public class AttachCdModel extends Model
{

	private ListModel privateIsoImage;
	public ListModel getIsoImage()
	{
		return privateIsoImage;
	}
	private void setIsoImage(ListModel value)
	{
		privateIsoImage = value;
	}


	public AttachCdModel()
	{
		setIsoImage(new ListModel());
	}
}