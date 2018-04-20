package org.ovirt.engine.ui.uicommonweb.models.vms;

import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

public class PriorityUtil {

    private UnitVmModel model;

    private Integer cachedMaxPriority;

    public PriorityUtil(UnitVmModel model) {
        this.model = model;
    }

    public void initPriority(final int priority, final PriorityUpdatingCallbacks callbacks) {
        AsyncDataProvider.getInstance().getMaxVmPriority(new AsyncQuery<>(
                returnValue -> {

                    cachedMaxPriority = returnValue;

                    int value = AsyncDataProvider.getInstance().getRoundedPriority(priority, cachedMaxPriority);
                    EntityModel tempVar = new EntityModel();
                    tempVar.setEntity(value);
                    before(callbacks);
                    model.getPriority().setSelectedItem(tempVar);
                    after(callbacks);
                    updatePriority(callbacks);

                }));
    }

    private void updatePriority(final PriorityUpdatingCallbacks callbacks) {
        if (cachedMaxPriority == null) {
            AsyncDataProvider.getInstance().getMaxVmPriority(new AsyncQuery<>(
                    returnValue -> {
                        cachedMaxPriority = returnValue;
                        postUpdatePriority(callbacks);

                    }));
        } else {
            postUpdatePriority(callbacks);
        }
    }

    private void postUpdatePriority(PriorityUpdatingCallbacks callbacks) {
        before(callbacks);
        List<EntityModel<Integer>> items = new ArrayList<>();
        EntityModel tempVar = new EntityModel();
        tempVar.setTitle(ConstantsManager.getInstance().getConstants().lowTitle());
        tempVar.setEntity(1);
        items.add(tempVar);
        EntityModel tempVar2 = new EntityModel();
        tempVar2.setTitle(ConstantsManager.getInstance().getConstants().mediumTitle());
        tempVar2.setEntity(cachedMaxPriority / 2);
        items.add(tempVar2);
        EntityModel tempVar3 = new EntityModel();
        tempVar3.setTitle(ConstantsManager.getInstance().getConstants().highTitle());
        tempVar3.setEntity(cachedMaxPriority);
        items.add(tempVar3);

        // If there was some priority selected before, try select it again.
        EntityModel<Integer> oldPriority = model.getPriority().getSelectedItem();

        model.getPriority().setItems(items);

        if (oldPriority != null) {
            for (EntityModel<Integer> item : items) {
                Integer val1 = item.getEntity();
                Integer val2 = oldPriority.getEntity();
                if (val1 != null && val1.equals(val2)) {
                    model.getPriority().setSelectedItem(item);
                    break;
                }
            }
        } else {
            model.getPriority().setSelectedItem(Linq.firstOrNull(items));
        }

        after(callbacks);
    }

    private void before(PriorityUpdatingCallbacks callbacks) {
        if (callbacks != null) {
            callbacks.beforeUpdates();
        }
    }

    private void after(PriorityUpdatingCallbacks callbacks) {
        if (callbacks != null) {
            callbacks.afterUpdates();
        }
    }

    public interface PriorityUpdatingCallbacks {
        void beforeUpdates();

        void afterUpdates();
    }
}
