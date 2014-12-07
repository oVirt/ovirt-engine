package org.ovirt.engine.ui.uicommonweb.models.vms;

import org.ovirt.engine.ui.frontend.AsyncQuery;
import org.ovirt.engine.ui.frontend.INewAsyncCallback;
import org.ovirt.engine.ui.uicommonweb.Linq;
import org.ovirt.engine.ui.uicommonweb.dataprovider.AsyncDataProvider;
import org.ovirt.engine.ui.uicommonweb.models.EntityModel;
import org.ovirt.engine.ui.uicompat.ConstantsManager;

import java.util.ArrayList;
import java.util.List;

public class PriorityUtil {

    private UnitVmModel model;

    private Integer cachedMaxPriority;

    public PriorityUtil(UnitVmModel model) {
        this.model = model;
    }

    public void initPriority(final int priority)
    {
        AsyncDataProvider.getInstance().getMaxVmPriority(new AsyncQuery(model,
                                                                        new INewAsyncCallback() {
                                                                            @Override
                                                                            public void onSuccess(Object target, Object returnValue) {

                                                                                UnitVmModel model = (UnitVmModel) target;
                                                                                cachedMaxPriority = (Integer) returnValue;

                                                                                int value = AsyncDataProvider.getInstance().getRoundedPriority(priority, cachedMaxPriority);
                                                                                EntityModel tempVar = new EntityModel();
                                                                                tempVar.setEntity(value);
                                                                                model.getPriority().setSelectedItem(tempVar);
                                                                                updatePriority();

                                                                            }
                                                                        }));
    }

    private void updatePriority()
    {
        if (cachedMaxPriority == null)
        {
            AsyncDataProvider.getInstance().getMaxVmPriority(new AsyncQuery(model,
                                                                            new INewAsyncCallback() {
                                                                                @Override
                                                                                public void onSuccess(Object target, Object returnValue) {
                                                                                    cachedMaxPriority = (Integer) returnValue;
                                                                                    postUpdatePriority();

                                                                                }
                                                                            }));
        }
        else
        {
            postUpdatePriority();
        }
    }

    private void postUpdatePriority()
    {
        List<EntityModel<Integer>> items = new ArrayList<EntityModel<Integer>>();
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

        if (oldPriority != null)
        {
            for (EntityModel<Integer> item : items)
            {
                Integer val1 = item.getEntity();
                Integer val2 = oldPriority.getEntity();
                if (val1 != null && val1.equals(val2))
                {
                    model.getPriority().setSelectedItem(item);
                    break;
                }
            }
        }
        else
        {
            model.getPriority().setSelectedItem(Linq.firstOrDefault(items));
        }
    }
}
