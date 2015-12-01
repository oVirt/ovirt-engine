package org.ovirt.engine.ui.uicommonweb.models.macpool;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.MacPool;
import org.ovirt.engine.core.common.businessentities.MacRange;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicommonweb.models.ListModel;
import org.ovirt.engine.ui.uicompat.Event;
import org.ovirt.engine.ui.uicompat.EventArgs;
import org.ovirt.engine.ui.uicompat.IEventListener;
import org.ovirt.engine.ui.uicompat.PropertyChangedEventArgs;

public class MacPoolModel extends EntityModel<MacPool> {

    private final EntityModel<Boolean> allowDuplicates = new EntityModel<>();
    private final ListModel<MacRangeModel> macRanges = new ListModel<>();

    public EntityModel<Boolean> getAllowDuplicates() {
        return allowDuplicates;
    }

    public ListModel<MacRangeModel> getMacRanges() {
        return macRanges;
    }

    public MacPoolModel() {
        getEntityChangedEvent().addListener(new IEventListener<EventArgs>() {

            @Override
            public void eventRaised(Event<? extends EventArgs> ev, Object sender, EventArgs args) {
                init();
            }
        });
        getPropertyChangedEvent().addListener(new IEventListener<PropertyChangedEventArgs>() {

            @Override
            public void eventRaised(Event<? extends PropertyChangedEventArgs> ev, Object sender, PropertyChangedEventArgs args) {
                if ("IsChangable".equals(args.propertyName)) { //$NON-NLS-1$
                    boolean value = getIsChangable();
                    allowDuplicates.setIsChangeable(value);
                    macRanges.setIsChangeable(value);
                }
            }
        });
    }

    protected void init() {
        if (getEntity() == null) {
            return;
        }

        allowDuplicates.setEntity(getEntity().isAllowDuplicateMacAddresses());
        List<MacRangeModel> rangeModels = new ArrayList<>();
        for (MacRange range : getEntity().getRanges()) {
            rangeModels.add(new MacRangeModel(range));
        }
        Collections.sort(rangeModels, new Comparator<MacRangeModel>() {

            @Override
            public int compare(MacRangeModel range1, MacRangeModel range2) {
                int res = range1.getLeftBound().getEntity().compareTo(range2.getLeftBound().getEntity());
                if (res == 0) {
                    res = range1.getRightBound().getEntity().compareTo(range2.getRightBound().getEntity());
                }
                return res;
            }
        });
        macRanges.setItems(rangeModels);
    }

    public MacPool flush() {
        getEntity().setAllowDuplicateMacAddresses(allowDuplicates.getEntity());
        getEntity().getRanges().clear();
        for (MacRangeModel rangeModel : macRanges.getItems()) {
            getEntity().getRanges().add(rangeModel.flush());
        }
        return getEntity();
    }

    public boolean validate() {
        boolean valid = true;
        for (MacRangeModel range : macRanges.getItems()) {
            range.validate();
            valid &= range.getIsValid();
        }
        setIsValid(valid);
        return valid;
    }

}
