package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;

public class GetEventSubscribersBySubscriberIdGroupedQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {
    public GetEventSubscribersBySubscriberIdGroupedQuery(P parameters) {
        super(parameters);
    }

    @Override
    protected void executeQueryCommand() {
        List<event_subscriber> list = DbFacade
                .getInstance()
                .getEventDao()
                .getAllForSubscriber(getParameters().getId());
        if (list.size() > 0) {
            HashMap<String, event_subscriber> dic = new HashMap<>();

            for (event_subscriber ev : list) {
                // event_subscriber foundEv = groupedList.FirstOrDefault(a =>
                // a.event_up_name == ev.event_up_name);
                if (dic.containsKey(ev.getevent_up_name())) {
                    dic.get(ev.getevent_up_name()).settag_name(
                            dic.get(ev.getevent_up_name()).gettag_name() + ", " + ev.gettag_name());
                } else {
                    dic.put(ev.getevent_up_name(), ev);
                }
            }

            ArrayList<event_subscriber> groupedList = new ArrayList<>(dic.values());
            for (event_subscriber event : groupedList) {
                event.settag_name(StringUtils.strip(event.gettag_name(), ", "));
            }
            getQueryReturnValue().setReturnValue(groupedList);
        } else {
            getQueryReturnValue().setReturnValue(list);
        }
    }
}
