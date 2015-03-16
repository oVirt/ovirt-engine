package org.ovirt.engine.core.dao;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.ovirt.engine.core.common.EventNotificationMethod;
import org.ovirt.engine.core.common.businessentities.event_subscriber;
import org.ovirt.engine.core.compat.Guid;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;

/**
 * <code>EventSubscriberDAODbFacadeImpl</code> provides an implementation of {@link EventSubscriberDAO} that uses the
 * refactored {@link org.ovirt.engine.core.dal.dbbroker.DbFacade} code.
 */
public class EventDAODbFacadeImpl extends BaseDAODbFacade implements EventDAO {

    private static final class EventSubscriberRowMapper implements RowMapper<event_subscriber> {
        public static final EventSubscriberRowMapper instance = new EventSubscriberRowMapper();

        @Override
        public event_subscriber mapRow(ResultSet rs, int rowNum)
                throws SQLException {
            event_subscriber entity = new event_subscriber();
            entity.setevent_up_name(rs.getString("event_up_name"));
            entity.setevent_notification_method(EventNotificationMethod.valueOfString(rs.getString("notification_method")));
            entity.setmethod_address(rs.getString("method_address"));
            entity.setsubscriber_id(getGuidDefaultEmpty(rs, "subscriber_id"));
            entity.settag_name(rs.getString("tag_name"));
            return entity;
        }
    }

    @Override
    public List<event_subscriber> getAllForSubscriber(Guid id) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("subscriber_id", id);
        return getCallsHandler().executeReadList("Getevent_subscriberBysubscriber_id",
                EventSubscriberRowMapper.instance,
                parameterSource);
    }

    @Override
    public void subscribe(event_subscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("notification_method", subscriber.getevent_notification_method().getAsString())
                .addValue("method_address", subscriber.getmethod_address())
                .addValue("subscriber_id", subscriber.getsubscriber_id())
                .addValue("tag_name", subscriber.gettag_name());

        getCallsHandler().executeModification("Insertevent_subscriber", parameterSource);
    }

    @Override
    public void unsubscribe(event_subscriber subscriber) {
        MapSqlParameterSource parameterSource = getCustomMapSqlParameterSource()
                .addValue("event_up_name", subscriber.getevent_up_name())
                .addValue("notification_method", subscriber.getevent_notification_method().getAsString())
                .addValue("subscriber_id", subscriber.getsubscriber_id())
                .addValue("tag_name", subscriber.gettag_name());

        getCallsHandler().executeModification("Deleteevent_subscriber", parameterSource);
    }
}

