import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This utility takes the FieldsInVDConfig.xml and generates an SQL It generates a "insert if not exists" SQL for each
 * one of the fields (existence by name + version) For fields with override it also provides an update statement
 */
public class FieldsInVDCConfigXmlToSQL {

    
    public static final String JDBC_PROPERTIES = "jdbc.properties";
    public static final String INSERT_NOT_EXIST_QUERY =
            "insert into vdc_options (option_name,option_value,version) select  '%1$s',%4$s'%2$s','%3$s' where not exists (select option_name,version from vdc_options where"
                    +
                    " option_name='%1$s' and version='%3$s');";
    public static final String UPDATE_QUERY =
            "update vdc_options set option_value=%4$s'%2$s' where option_name='%1$s' and version='%3$s';";
    
    public static final String DELETE_QUERY = "delete from vdc_options where option_name='%1$s' and version='%2$s';";

    public static final String CHARS_FOR_ESCAPING = "\\%";
    private static final String COMMENT = "--Handling %1$s";

    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Parameters:<path to FieldsInVDCConfig.xml>");
            return;
        }
        FieldsInVDCConfigXmlToSQL gen = new FieldsInVDCConfigXmlToSQL();
        gen.initPGDriver();
        long numOfExistingOptions = gen.getNumOfExistingOptions();
        if (numOfExistingOptions < 20) { //We have more than 20 options - this is just a safe measure
            System.err.println("The script must be run after create_db.sh is run and the database is filled with options");
            return;
        }
        gen.generate(args[0]);

    }

    public void initPGDriver() {
        try {
            Class.forName("org.postgresql.Driver");
        } catch (ClassNotFoundException cnfe) {
            System.err.println("Couldn't find driver jdbc class");
            System.exit(1);
        }
    }

    public long getNumOfExistingOptions() {
        Connection c = null;
        try {
            c = getConnection();
            Statement statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
            ResultSet rs = statement.executeQuery("select count(*) cnt from vdc_options");
            rs.beforeFirst();
            rs.next(); 
            long result = rs.getLong("cnt");
            return result;
        } catch (Exception se) {
            se.printStackTrace();
            System.exit(1);
            return -1;
        } finally {
            closeConnection(c);
        }
    }

    private void closeConnection(Connection c) {
        if (c != null) {
            try {
                c.close();
            }
            catch (Exception ex) {
                ex.printStackTrace();
                System.exit(1);
            }
        }
    }

    private void generate(String pathToFieldsInVDCConfigXML) {

        FileInputStream fis = null;
        try {
            fis = new FileInputStream(pathToFieldsInVDCConfigXML);
            DocumentBuilderFactory domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setNamespaceAware(true);
            DocumentBuilder builder = domFactory.newDocumentBuilder();
            Document doc = builder.parse(fis);
            deleteOptionsNotInXML(doc);
            NodeList list = getVersionNodes(doc);
            for (int counter = 0; counter < list.getLength(); counter++) {
                handleVersion((Element) list.item(counter));
            }
	    System.out.println("update vdc_options set option_value = 'Postgres' where option_name = 'DBEngine';");
        } catch (Exception ex) {
            ex.printStackTrace();
            System.err.println("Failure in generating script" + ex.getMessage());
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                }
            }
        }

    }

    private Connection getConnection() throws Exception {
        Properties props = new Properties();
        props.load(new FileInputStream(JDBC_PROPERTIES));
        String user = props.getProperty("user");
        String password = props.getProperty("password");
        String url = props.getProperty("url");
        Connection c = DriverManager.getConnection(url,
                                              user, password);
        return c;
    }

    private void deleteOptionsNotInXML(Document doc) throws IOException, FileNotFoundException, SQLException,
            XPathExpressionException {
        Connection c = null;
        try {
            c = getConnection();
            compareDatabaseAndXML(doc, c);
        } catch (Exception se) {
            se.printStackTrace();
            System.exit(1);
        }
        finally {
            closeConnection(c);
        }
    }

    private void compareDatabaseAndXML(Document doc, Connection c) throws SQLException, XPathExpressionException {
        Statement statement = c.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                ResultSet.CONCUR_UPDATABLE);
        ResultSet rs = statement.executeQuery("select * from vdc_options");
        rs.beforeFirst();
        while (rs.next()) {
            String name = rs.getString("option_name");
            String version = rs.getString("version");
            if (!existsInVersion(doc,version,name)) {
                System.out.println(String.format(DELETE_QUERY,name,version));
            }
        }
    }

    private NodeList getVersionNodes(Document doc) throws XPathExpressionException {
        // Get all XML element named Version - they contain the fields per version
        XPath xpath = XPathFactory.newInstance().newXPath();
        XPathExpression expr = xpath.compile("//Version");
        NodeList list = (NodeList) expr.evaluate(doc, XPathConstants.NODESET);
        return list;
    }

    private boolean existsInVersion(Document doc, String version, String name) throws XPathExpressionException {
            try {
                NodeList versionNodes = getVersionNodes(doc);
                for (int counter = 0; counter < versionNodes.getLength(); counter++) {
                    if (fieldExists((Element) versionNodes.item(counter),name)) {
                        return true;
                    }
                }
                
                return false;

            } catch (XPathExpressionException e) {
                throw e;
            }
    }

    private boolean fieldExists(Element versionElement, String name) throws XPathExpressionException {
        NodeList list = getFieldsNodes(versionElement);
        for (int counter = 0; counter < list.getLength(); counter++) {
            Element fieldElement = (Element)list.item(counter);
            String value = fieldElement.getAttribute(name);
            if (value != null && !value.isEmpty()) {
                return true;
            }
        }
        return false;
    }

    private void handleVersion(Element versionElement) {
        String version = versionElement.getAttribute("name");
        try {
            NodeList list = getFieldsNodes(versionElement);
            for (int counter = 0; counter < list.getLength(); counter++) {
                handleOption((Element) list.item(counter), version);
            }
        } catch (Exception ex) {
            System.err.println("Error in generating queries for verison " + version + "exception is: "
                    + ex.getMessage());
            System.exit(1);
        }

    }

    private NodeList getFieldsNodes(Element versionElement) throws XPathExpressionException {
        // Get all XML elements named Field - they contain the field information
        NodeList list = (NodeList) versionElement.getElementsByTagName("Field");
        return list;
    }

    private boolean shouldEscape(String value) {

        for (int counter = 0; counter < CHARS_FOR_ESCAPING.length(); counter++) {
            char charForEscaping = CHARS_FOR_ESCAPING.charAt(counter);
            if (value.indexOf(charForEscaping) != -1) {
                return true;
            }
        }
        return false;
    }

    private void handleOption(Element field, String version) {
        // For each option get the key , value and override
        // If override equals true - provide also an update statement
        // The value must be checked if it should be escaped or not
        String keyStr = field.getAttribute("key");
        String valueStr = field.getAttribute("value");
        String toolTip = field.getAttribute("toolTip");
        String escapePrefix = "";
        if (shouldEscape(valueStr)) {
            escapePrefix = "E";
            valueStr = escape(valueStr);
        }
        String overrideStr = field.getAttribute("override");
        boolean override = Boolean.parseBoolean(overrideStr);
        if (toolTip != null && !toolTip.isEmpty()) {
            System.out.println(String.format(COMMENT,toolTip));
        }
        System.out.println(String.format(INSERT_NOT_EXIST_QUERY, keyStr, valueStr, version, escapePrefix));
        if (override) {
            System.out.println(String.format(UPDATE_QUERY, keyStr, valueStr, version, escapePrefix));
        }
    }

    private String escape(String valueStr) {
        return valueStr.replace("\\", "\\\\");
    }
}
