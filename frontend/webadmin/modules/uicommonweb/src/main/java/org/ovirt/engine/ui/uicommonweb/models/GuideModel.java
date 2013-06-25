package org.ovirt.engine.ui.uicommonweb.models;

import java.util.List;

import org.ovirt.engine.ui.uicommonweb.UICommand;
import org.ovirt.engine.ui.uicompat.ObservableCollection;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class GuideModel extends EntityModel
{

    private List<UICommand> compulsoryActions;

    public List<UICommand> getCompulsoryActions()
    {
        return compulsoryActions;
    }

    public void setCompulsoryActions(List<UICommand> value)
    {
        if (compulsoryActions != value)
        {
            compulsoryActions = value;
            onPropertyChanged(new PropertyChangedEventArgs("CompulsoryActions")); //$NON-NLS-1$
        }
    }

    private List<UICommand> optionalActions;

    public List<UICommand> getOptionalActions()
    {
        return optionalActions;
    }

    public void setOptionalActions(List<UICommand> value)
    {
        if (optionalActions != value)
        {
            optionalActions = value;
            onPropertyChanged(new PropertyChangedEventArgs("OptionalActions")); //$NON-NLS-1$
        }
    }

    public GuideModel()
    {
        setCompulsoryActions(new ObservableCollection<UICommand>());
        setOptionalActions(new ObservableCollection<UICommand>());
    }
}
