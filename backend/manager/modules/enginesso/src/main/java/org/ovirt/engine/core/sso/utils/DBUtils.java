package org.ovirt.engine.core.sso.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import org.apache.commons.lang.StringUtils;

public class DBUtils {
    public static final String DATA_SOURCE = "java:/ENGINEDataSource";

    public static Map<String, ClientInfo> getAllSsoClientsInfo() {
        DataSource ds;

        try {
            ds = (DataSource) new InitialContext().lookup(DATA_SOURCE);
            if (ds == null) {
                throw new RuntimeException("Failed to obtain data source");
            }
            Map<String, ClientInfo> map = new HashMap<>();
            String sql = "SELECT client_id, client_secret, certificate_location, callback_prefix, " +
                    "notification_callback, scope, trusted, notification_callback_protocol, " +
                    "notification_callback_verify_host, notification_callback_verify_chain from sso_clients";
            try (
                    Connection connection = ds.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        String clientId = rs.getString("client_id");
                        map.put(clientId, new ClientInfo().withClientId(clientId)
                                .withClientSecret(rs.getString("client_secret"))
                                .withCertificateLocation(rs.getString("certificate_location"))
                                .withCallbackPrefix(rs.getString("callback_prefix"))
                                .withClientNotificationCallback(StringUtils.defaultIfEmpty(rs.getString("notification_callback"), ""))
                                .withScope(SsoUtils.scopeAsList(rs.getString("scope")))
                                .withIsTrusted(rs.getBoolean("trusted"))
                                .withNotificationCallbackProtocol(rs.getString("notification_callback_protocol"))
                                .withNotificationCallbackVerifyHost(rs.getBoolean("notification_callback_verify_host"))
                                .withNotificationCallVerifyChain(rs.getBoolean("notification_callback_verify_chain")));
                    }
                }
            }
            return map;
        } catch (SQLException ex) {
            throw new RuntimeException("Database query failed", ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Error looking up resource " + DATA_SOURCE, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to initialize client registry", ex);
        }
    }

    public static Map<String, List<String>> getAllSsoScopeDependencies() {
        DataSource ds;

        try {
            ds = (DataSource) new InitialContext().lookup(DATA_SOURCE);
            if (ds == null) {
                throw new RuntimeException("Failed to obtain data source");
            }
            Map<String, List<String>> map = new HashMap<>();
            String sql = "SELECT scope, dependencies FROM sso_scope_dependency";
            try (
                    Connection connection = ds.getConnection();
                    PreparedStatement ps = connection.prepareStatement(sql)
            ) {
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) {
                        map.put(rs.getString("scope"), SsoUtils.scopeAsList(rs.getString("dependencies")));
                    }
                }
            }
            return map;
        } catch (SQLException ex) {
            throw new RuntimeException("Database query failed", ex);
        } catch (NamingException ex) {
            throw new RuntimeException("Error looking up resource " + DATA_SOURCE, ex);
        } catch (Exception ex) {
            throw new RuntimeException("Unable to initialize scope dependencies", ex);
        }
    }
}
