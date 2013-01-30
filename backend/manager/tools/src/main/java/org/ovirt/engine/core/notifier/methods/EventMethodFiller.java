package org.ovirt.engine.core.notifier.methods;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.ovirt.engine.core.common.EventNotificationMethods;
import org.ovirt.engine.core.common.businessentities.EventNotificationMethod;

/**
 * Populates a event notification method list by a database content<br>
 * If no valid method type method is provided (meaning a method type which isn't defined in
 * {@link EventNotificationMethods}),<br>
 * a default value of {@code EventNotificationMethods.EMAIL} is set.
 */
public class EventMethodFiller {

    private List<EventNotificationMethod> methods = new ArrayList<EventNotificationMethod>();

    /**
     * Populates the list of event notification methods by its content in table <i>event_notification_methods</i>
     * @param conn
     *            a connection to the database
     * @throws SQLException
     */
    public void fillEventNotificationMethods(Connection conn) throws SQLException {
        Statement stmt = null;
        ResultSet rs = null;
        try {
            stmt = conn.createStatement();
            rs = stmt.executeQuery("select * from event_notification_methods");
            EventNotificationMethod method;
            while (rs.next()) {
                method = new EventNotificationMethod();
                method.setmethod_id(rs.getInt("method_id"));
                method.setmethod_type(getMethodTypeByName(rs.getString("method_type")));
                methods.add(method);
            }
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (SQLException e) {
                }
            }
            if (stmt != null) {
                stmt.close();
            }
        }
    }

    private EventNotificationMethods getMethodTypeByName(String methodName) {
        for (EventNotificationMethods value : EventNotificationMethods.values()) {
            if (value.name().equalsIgnoreCase(methodName)) {
                return value;
            }
        }
        return EventNotificationMethods.EMAIL;
    }

    /**
     * A getter of the created methods list. Should be called and used after
     * {@link #fillEventNotificationMethods(Connection)} was invoked
     * @return list of configured notification methods
     */
    public List<EventNotificationMethod> getEventNotificationMethods() {
        return methods;
    }
}
