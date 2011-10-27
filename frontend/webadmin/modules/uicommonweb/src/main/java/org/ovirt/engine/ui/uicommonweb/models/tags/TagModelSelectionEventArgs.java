package org.ovirt.engine.ui.uicommonweb.models.tags;
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
import org.ovirt.engine.ui.uicommonweb.models.*;

@SuppressWarnings("unused")
public final class TagModelSelectionEventArgs extends EventArgs
{
	private boolean privateSelection;
	public boolean getSelection()
	{
		return privateSelection;
	}
	private void setSelection(boolean value)
	{
		privateSelection = value;
	}

	public TagModelSelectionEventArgs(boolean selection)
	{
		setSelection(selection);
	}
}