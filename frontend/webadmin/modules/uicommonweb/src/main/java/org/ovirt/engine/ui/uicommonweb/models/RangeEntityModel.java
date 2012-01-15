package org.ovirt.engine.ui.uicommonweb.models;

import org.ovirt.engine.core.compat.PropertyChangedEventArgs;

@SuppressWarnings("unused")
public class RangeEntityModel extends EntityModel
{
    private double min;

    public double getMin()
    {
        return min;
    }

    public void setMin(double value)
    {
        if (min != value)
        {
            min = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Min"));
        }
    }

    private double max;

    public double getMax()
    {
        return max;
    }

    public void setMax(double value)
    {
        if (max != value)
        {
            max = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Max"));
        }
    }

    private double interval;

    public double getInterval()
    {
        return interval;
    }

    public void setInterval(double value)
    {
        if (interval != value)
        {
            interval = value;
            OnPropertyChanged(new PropertyChangedEventArgs("Interval"));
        }
    }

    private boolean isAllValuesSet;

    public boolean getIsAllValuesSet()
    {
        return isAllValuesSet;
    }

    public void setIsAllValuesSet(boolean value)
    {
        if (isAllValuesSet != value)
        {
            isAllValuesSet = value;
            OnPropertyChanged(new PropertyChangedEventArgs("IsAllValuesSet"));
        }
    }
}
