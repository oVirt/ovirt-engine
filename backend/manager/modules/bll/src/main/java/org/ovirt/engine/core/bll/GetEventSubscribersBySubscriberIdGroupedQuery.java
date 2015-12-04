package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetEventSubscribersBySubscriberIdGroupedQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetEventSubscribersBySubscriberIdGroupedQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<EventSubscriber> list = DbFacade
                .getInstance()
                .getEventDao()
                .getAllForSubscriber(getParameters().getId());
        if (list.size() > 0) {
            HashMap<String, EventSubscriber> dic = new HashMap<>();

            for (EventSubscriber ev : list) {
                if (dic.containsKey(ev.getEventUpName())) {
                    dic.get(ev.getEventUpName()).setTagName(
                            dic.get(ev.getEventUpName()).getTagName() + ", " + ev.getTagName());
                } else {
                    dic.put(ev.getEventUpName(), ev);
                }
            }

            ArrayList<EventSubscriber> groupedList = new ArrayList<>(dic.values());
            for (EventSubscriber event : groupedList) {
                event.setTagName(StringUtils.strip(event.getTagName(), ", "));
            }
            getQueryReturnValue().setReturnValue(groupedList);
        } else {
            getQueryReturnValue().setReturnValue(list);
        }
    }
}
