package org.ovirt.engine.ui.uicommonweb.models;
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
public class GuideModel extends EntityModel
{

	private java.util.List<UICommand> compulsoryActions;
	public java.util.List<UICommand> getCompulsoryActions()
	{
		return compulsoryActions;
	}
	public void setCompulsoryActions(java.util.List<UICommand> value)
	{
		if (compulsoryActions != value)
		{
			compulsoryActions = value;
			OnPropertyChanged(new PropertyChangedEventArgs("CompulsoryActions"));
		}
	}

	private java.util.List<UICommand> optionalActions;
	public java.util.List<UICommand> getOptionalActions()
	{
		return optionalActions;
	}
	public void setOptionalActions(java.util.List<UICommand> value)
	{
		if (optionalActions != value)
		{
			optionalActions = value;
			OnPropertyChanged(new PropertyChangedEventArgs("OptionalActions"));
		}
	}



	public GuideModel()
	{
		setCompulsoryActions(new ObservableCollection<UICommand>());
		setOptionalActions(new ObservableCollection<UICommand>());
	}
}