package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import javax.inject.Named;
import javax.inject.Singleton;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.EventSubscriber;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>EventSubscriberDaoImpl</code> provides an implementation of {@link EventSubscriberDao} that uses the
 * refactored {@link org.ovirt.engine.core.dal.dbbroker.DbFacade} code.
 */
@Named
@Singleton
public class EventDaoImpl extends BaseDao implements EventDao {

    private static final class EventSubscriberRowMapper implements RowMapper<EventSubscriber> {
        public static final EventSubscriberRowMapper instance = new EventSubscriberRowMapper();

        @Override
        public EventSubscriber mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            EventSubscriber entity = new EventSubscriber();
            entity.setEventUpName(rs.getString("event_up_name"));
            entity.setEventNotificationMethod(EventNotificationMethod.valueOfString(rs.getString("notification_method")));
            entity.setMethodAddress(rs.getString("method_address"));
            entity.setSubscriberId(getGuidDefaultEmpty(rs, "subscriber_id"));
            entity.setTagName(rs.getString("tag_name"));
            return entity;
        }
    }

    @Override
    public List<EventSubscriber> getAllForSubscriber(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("subscriber_id", id);
        return getCallsHandler().executeReadList("Getevent_subscriberBysubscriber_id",
                EventSubscriberRowMapper.instance,
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

