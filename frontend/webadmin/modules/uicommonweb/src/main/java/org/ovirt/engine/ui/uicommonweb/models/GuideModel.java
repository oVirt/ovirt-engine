package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.compat.ObservableCollection;
import org.ovirt.engine.core.compat.PropertyChangedEventArgs;
import org.ovirt.engine.ui.uicommonweb.UICommand;

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
