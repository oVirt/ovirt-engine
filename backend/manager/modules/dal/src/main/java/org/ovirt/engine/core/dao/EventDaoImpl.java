package org.ovirt.engine.core.dao;

import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.AuditLogType;
import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * {@code EventDaoImpl} provides an implementation of {@link EventDao}.
 */
@Named
@Singleton
public class EventDaoImpl extends BaseDao implements EventDao {

    private static final RowMapper<EventSubscriber> eventSubscriberRowMapper = (rs, rowNum) -> {
        EventSubscriber entity = new EventSubscriber();
        entity.setEventUpName(rs.getString("event_up_name"));
        entity.setEventNotificationMethod(EventNotificationMethod.valueOfString(rs.getString("notification_method")));
        entity.setMethodAddress(rs.getString("method_address"));
        entity.setSubscriberId(getGuidDefaultEmpty(rs, "subscriber_id"));
        entity.setTagName(rs.getString("tag_name"));
        return entity;
    };

    @Override
    public List<EventSubscriber> getAllForSubscriber(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("subscriber_id", id);
        return getCallsHandler().executeReadList("Getevent_subscriberBysubscriber_id",
                eventSubscriberRowMapper,
                parameterSource);
    }

    @Override
    public EventSubscriber getEventSubscription(Guid id, AuditLogType event) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("subscriber_id", id)
                .addValue("event_up_name", event.toString());
        return getCallsHandler().executeRead("Getevent_subscription",
                eventSubscriberRowMapper,
                parameterSource);
    }

    @Override
    public void subscribe(EventSubscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getEventUpName())
                .addValue("notification_method", subscriber.getEventNotificationMethod().getAsString())
                .addValue("method_address", subscriber.getMethodAddress())
                .addValue("subscriber_id", subscriber.getSubscriberId())
                .addValue("tag_name", subscriber.getTagName());

        getCallsHandler().executeModification("Insertevent_subscriber", parameterSource);
    }

    @Override
    public void unsubscribe(EventSubscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getEventUpName())
                .addValue("notification_method", subscriber.getEventNotificationMethod().getAsString())
                .addValue("subscriber_id", subscriber.getSubscriberId())
                .addValue("tag_name", subscriber.getTagName());

        getCallsHandler().executeModification("Deleteevent_subscriber", parameterSource);
    }

}
