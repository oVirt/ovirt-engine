package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.EventMap;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>EventSubscriberDAODbFacadeImpl</code> provides an implementation of {@link EventSubscriberDAO} that uses the
 * refactored {@link DbFacade} code.
 */
public class EventDAODbFacadeImpl extends BaseDAODbFacade implements EventDAO {

    private static final class EventSubscriberRowMapper implements RowMapper<event_subscriber> {
        public static final EventSubscriberRowMapper instance = new EventSubscriberRowMapper();

        @Override
        public event_subscriber mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            event_subscriber entity = new event_subscriber();
            entity.setevent_up_name(rs.getString("event_up_name"));
            entity.setmethod_id(rs.getInt("method_id"));
            entity.setmethod_address(rs.getString("method_address"));
            entity.setsubscriber_id(Guid.createGuidFromString(rs
                    .getString("subscriber_id")));
            entity.settag_name(rs.getString("tag_name"));
            return entity;
        }
    }

    private static final class EventNotificationMethodRowMapper implements RowMapper<EventNotificationMethod> {
        public static final EventNotificationMethodRowMapper instance = new EventNotificationMethodRowMapper();

        @Override
        public EventNotificationMethod mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventNotificationMethod entity = new EventNotificationMethod();
            entity.setmethod_id(rs.getInt("method_id"));
            entity.setmethod_type(EventNotificationMethods.EMAIL);
            return entity;
        }
    }

    private static final class EventMapRowMapper implements RowMapper<EventMap> {
        public static final EventMapRowMapper instance = new EventMapRowMapper();

        @Override
        public EventMap mapRow(ResultSet rs, int rowNum) throws SQLException {
            EventMap entity = new EventMap();
            entity.setEventUpName(rs.getString("event_up_name"));
            entity.setEventDownName(rs.getString("event_down_name"));
            return entity;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<event_subscriber> getAllForSubscriber(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("subscriber_id", id);
        return getCallsHandler().executeReadList("Getevent_subscriberBysubscriber_id",
                EventSubscriberRowMapper.instance,
                parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EventNotificationMethod> getEventNotificationMethodsById(int method_id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("method_id", method_id);
        return getCallsHandler().executeReadList("GetEventNotificationMethodById",
                EventNotificationMethodRowMapper.instance,
                parameterSource);
    }

    @Override
    public void subscribe(event_subscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("method_id", subscriber.getmethod_id())
                .addValue("method_address", subscriber.getmethod_address())
                .addValue("subscriber_id", subscriber.getsubscriber_id())
                .addValue("tag_name", subscriber.gettag_name());

        getCallsHandler().executeModification("Insertevent_subscriber", parameterSource);
    }

    @Override
    public void update(event_subscriber subscriber, int oldMethodId) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("old_method_id", oldMethodId)
                .addValue("new_method_id", subscriber.getmethod_id())
                .addValue("subscriber_id", subscriber.getsubscriber_id());

        getCallsHandler().executeModification("Updateevent_subscriber", parameterSource);
    }

    @Override
    public void unsubscribe(event_subscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("method_id", subscriber.getmethod_id())
                .addValue("subscriber_id", subscriber.getsubscriber_id())
                .addValue("tag_name", subscriber.gettag_name());

        getCallsHandler().executeModification("Deleteevent_subscriber", parameterSource);
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<EventMap> getEventMapByName(String event_up_name) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource().addValue("event_name", event_up_name);
        return getCallsHandler().executeReadList("GetEventMapByName", EventMapRowMapper.instance, parameterSource);
    }
}

