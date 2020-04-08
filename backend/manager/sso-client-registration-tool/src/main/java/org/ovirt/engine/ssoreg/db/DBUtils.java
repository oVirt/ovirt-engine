package org.ovirt.engine.ssoreg.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import javax.sql.DataSource;

public class DBUtils {

    private DataSource ds;
    private String unregisterClientSql;
    private String registerClientSql;
    private String scopes;

    public DBUtils() throws SQLException {
        ds = new StandaloneDataSource();
        unregisterClientSql = "select sso_oauth_unregister_client(?)";
        registerClientSql = "select sso_oauth_register_client(?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
        scopes = "openid ovirt-app-portal ovirt-app-admin ovirt-app-api ovirt-ext=auth:identity " +
                "ovirt-ext=token:password-access ovirt-ext=auth:sequence-priority ovirt-ext=token:login-on-behalf " +
                "ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search " +
                "ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all";
    }

    public void unregisterClient(String clientId) throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(unregisterClientSql)
        ) {
            prepareStatement.setString(1, clientId);
            prepareStatement.execute();
        }
    }

    public void registerClient(String clientId, String clientSecret, String certificate, String callbackPrefix, boolean encryptedUserInfo)
            throws SQLException {
        try (
                Connection connection = ds.getConnection();
                PreparedStatement prepareStatement = connection.prepareStatement(registerClientSql)
        ) {
            prepareStatement.setString(1, clientId); //client_id
            prepareStatement.setString(2, clientSecret); //client_secret
            prepareStatement.setString(3, scopes); //scopes
            prepareStatement.setString(4, certificate); //certificate
            prepareStatement.setString(5, callbackPrefix); //callback_prefix
            prepareStatement.setString(6, "oVirt Engine Client"); //description
            prepareStatement.setString(7, ""); //email
            prepareStatement.setBoolean(8, encryptedUserInfo); //encrypted_userinfo
            prepareStatement.setBoolean(9, Boolean.TRUE); //trusted
            prepareStatement.setString(10, ""); //notification_callback
            prepareStatement.setString(11, "TLS"); //notification_callback_host_protocol
            prepareStatement.setBoolean(12, Boolean.FALSE); //notification_callback_host_verification
            prepareStatement.setBoolean(13, Boolean.TRUE); //notification_callback_chain_validation
            prepareStatement.execute();
        }
    }
}
