package org.ovirt.engine.core.bll;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.apache.commons.lang.StringUtils;
import org.ovirt.engine.core.bll.context.EngineContext;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.common.queries.IdQueryParameters;
import org.ovirt.engine.core.dao.EventDao;

public class GetEventSubscribersBySubscriberIdGroupedQuery<P extends IdQueryParameters>
        extends QueriesCommandBase<P> {

    @Inject
    private EventDao eventDao;

    public GetEventSubscribersBySubscriberIdGroupedQuery(P parameters, EngineContext engineContext) {
        super(parameters, engineContext);
    }

    @Override
    protected void executeQueryCommand() {
        List<EventSubscriber> list = eventDao.getAllForSubscriber(getParameters().getId());
        if (list.size() > 0) {
            Map<String, EventSubscriber> dic = new HashMap<>();

            for (EventSubscriber ev : list) {
                if (dic.containsKey(ev.getEventUpName())) {
                    dic.get(ev.getEventUpName()).setTagName(
                            dic.get(ev.getEventUpName()).getTagName() + ", " + ev.getTagName());
                } else {
                    dic.put(ev.getEventUpName(), ev);
                }
            }

            List<EventSubscriber> groupedList = new ArrayList<>(dic.values());
            for (EventSubscriber event : groupedList) {
                event.setTagName(StringUtils.strip(event.getTagName(), ", "));
            }
            getQueryReturnValue().setReturnValue(groupedList);
        } else {
            getQueryReturnValue().setReturnValue(list);
        }
    }
}
