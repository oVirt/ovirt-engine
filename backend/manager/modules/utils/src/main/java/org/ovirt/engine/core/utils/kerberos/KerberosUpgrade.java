/**
 *
 */
package org.ovirt.engine.core.utils.kerberos;

import static org.ovirt.engine.core.utils.kerberos.InstallerConstants.ERROR_PREFIX;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.ovirt.engine.core.compat.StringHelper;
import org.ovirt.engine.core.engineencryptutils.EncryptionUtils;
import org.ovirt.engine.core.utils.CLIParser;

/**
 *
 */
public class KerberosUpgrade {

    public enum Arguments {
        user,
        password,
        connection_string,
        jaas_file,
        krb5_conf_path,
        mixed_mode;
    }

    public enum OptionNames {
        AdUserName,
        AdUserPassword,
        DomainName,
        CertAlias,
        keystorePass,
        keystoreUrl;
    }

    public KerberosUpgrade() {

    }

    public static void main(String[] args) {
        KerberosUpgrade util = new KerberosUpgrade();
        CLIParser parser = new CLIParser(args);
        if (!util.validate(parser)) {
            System.exit(1);
        }

        String krb5ConfPath = parser.getArg(Arguments.krb5_conf_path.name());

        try {
            util.registerJDBCDriver();
        } catch (Exception e) {
            handleError(e, "Error in registering JDBC driver");
        }

        Connection connection = null;
        try {
            connection = util.openConnection(parser);
        } catch (Exception e) {
            handleError(e, "Error in connecting to oVirt database");
        }

        Map<String, String> results = null;
        String adUserName = null;
        String adPassword = null;
        String adDomainName = null;

        try {
            // Get relevant records from vdc_options
            results = util.getOptions(connection);

            // Get all relevant values from the records, decrypt username and password of AD user if possible
            adUserName = results.get(OptionNames.AdUserName.name());
            adPassword = results.get(OptionNames.AdUserPassword.name());
            adDomainName = results.get(OptionNames.DomainName.name());
            String certAlias = results.get(OptionNames.CertAlias.name());
            String keystoreUrl = results.get(OptionNames.keystoreUrl.name());
            String keystorePass = results.get(OptionNames.keystorePass.name());
            adPassword = decryptADPassword(adPassword, certAlias, keystoreUrl, keystorePass);

            // Create the krb5 conf file
            util.runKrb5ConfCreator(parser, krb5ConfPath, adDomainName);

        } catch (SQLException e) {
            handleError(e, "Error in querying oVirt database for Active-Directory user, password and domains");
        }

        KerberosConfigCheck configCheck = new KerberosConfigCheck();
        StringBuffer userGuid = new StringBuffer();
        // Check authentication and user information retrieval
        try {
            configCheck.checkInstallation(adDomainName,
                    adUserName,
                    adPassword,
                    parser.getArg(Arguments.jaas_file.name()),
                    krb5ConfPath,
                    userGuid, null);
        } catch (Exception e) {
            System.err.println(ERROR_PREFIX + e.getMessage());
            System.exit(1);
        }

    }

    private static String decryptADPassword(
            String adPassword,
            String certAlias,
            String keystoreUrl,
            String keystorePass) {
        try {
            return EncryptionUtils.decrypt(adPassword, keystoreUrl, keystorePass, certAlias);
        } catch (Exception e) {
            System.err.println("Failed to decrypt AD password");
            return adPassword;
        }
    }

    public void runKrb5ConfCreator(CLIParser parser, String krb5ConfPath, String adDomainName) {
        KrbConfCreator confCreator = null;
        StringBuffer buffer = null;
        try {
            confCreator = new KrbConfCreator(adDomainName);
        } catch (Exception ex) {
            handleError(ex, "Error in loading kerberos configuration template file");
        }
        try {
            buffer = confCreator.parse(parser.getArg(Arguments.mixed_mode.name()));
        } catch (AuthenticationException authException) {
            handleError(authException, "Error in creating kerberos configuration file");
        }
        try {
            confCreator.toFile(krb5ConfPath, buffer);
        } catch (Exception ex) {
            handleError(ex, "Error in writing kerberos configuration file");
        }
    }


    private Map<String, String> getOptions(Connection connection) throws SQLException {
        Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);

        ResultSet rs =
                statement.executeQuery("select option_name,option_value from vdc_options where option_name "
                        + getOptionsSQLClause());
        Map<String, String> results = new HashMap<String, String>();
        rs.first();
        while (!rs.isAfterLast()) {
            for (OptionNames optionName : OptionNames.values()) {
                String optionNameFromDB = rs.getString("option_name");
                String optionValueFromDB = rs.getString("option_value");
                if (optionNameFromDB.equals(optionName.name()) && optionValueFromDB != null) {
                    results.put(optionName.name(), optionValueFromDB);
                    break;
                }
            }
            rs.next();
        }
        return results;
    }

    private String getOptionsSQLClause() {
        StringBuilder sb = new StringBuilder();
        OptionNames[] enumValues = OptionNames.values();
        sb.append(" in ('");

        sb.append(StringHelper.join("','", enumValues));
        sb.append("')");
        System.out.println(sb.toString());
        return sb.toString();
    }

    private static void handleError(Exception e, String string) {
        e.printStackTrace();
        System.out.println(InstallerConstants.ERROR_PREFIX + e.getMessage());
        if (e instanceof AuthenticationException) {
            System.exit(((AuthenticationException) e).getAuthResult().getExitCode());
        }
        System.exit(1);
    }

    private Connection openConnection(CLIParser parser) throws SQLException {
        String connString = parser.getArg(Arguments.connection_string.toString());
        String dbUserName = parser.getArg(Arguments.user.toString());
        String password = parser.getArg(Arguments.password.toString());
        return java.sql.DriverManager.getConnection(connString, dbUserName, password);
    }

    private void registerJDBCDriver() throws Exception {
        Driver d = (Driver) Class.forName("com.microsoft.sqlserver.jdbc.SQLServerDriver").newInstance();
    }

    private boolean validate(CLIParser parser) {
        for (Arguments arg : Arguments.values()) {
            if (!parser.hasArg(arg.name())) {
                System.out.println(arg.name() + " is required");
                return false;
            }
        }
        return true;
    }

}
