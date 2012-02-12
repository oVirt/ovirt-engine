package org.ovirt.engine.core.itests.ldap;

import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.configuration.ConfigurationUtils;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.apache.commons.configuration.PropertiesConfiguration;
import org.apache.commons.configuration.tree.ConfigurationNode;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.springframework.ldap.core.LdapTemplate;
import org.springframework.ldap.core.support.LdapContextSource;

public class LdapTestsSetup {
    private Configuration testProperties;

    private IPAGroupDaoImpl ipaGroupDao = new IPAGroupDaoImpl();
    private IPAPersonDaoImpl ipaPersonDao = new IPAPersonDaoImpl();
    private ADPersonDaoImpl adPersonDao = new ADPersonDaoImpl();
    private ADGroupDaoImpl adGroupDao = new ADGroupDaoImpl();
    private LdapTemplate ipaLdapTemplate;
    private LdapTemplate adLdapTemplate;
    private LdapContextSource ipaLdapContext;
    private LdapContextSource adLdapContext;
    private Map<String, Person> users = new HashMap<String, Person>();
    private Map<String, Group> groups = new HashMap<String, Group>();
    private Map<String, String> ldapConfiguration = new HashMap<String, String>();

    private static Log log = LogFactory.getLog(LdapTestsSetup.class);

    public LdapTestsSetup() {
        String confFile = System.getenv("LDAP_TESTER_PROPERTIES_FILE");
        if (confFile == null) {
            confFile = "ldap.integ/ldap-test.properties";
        }

        try {
            testProperties = new PropertiesConfiguration(confFile);

            Configuration usersSubset = testProperties.subset("users");
            HierarchicalConfiguration usersConfig = ConfigurationUtils.convertToHierarchical(usersSubset);
            List<ConfigurationNode> childrens = usersConfig.getRootNode().getChildren();
            for (ConfigurationNode node : childrens) {
                String name = node.getName();
                users.put(name, new Person(usersSubset.subset(name)));
            }

            Configuration groupsSubset = testProperties.subset("groups");
            HierarchicalConfiguration groupsConfig = ConfigurationUtils.convertToHierarchical(groupsSubset);
            childrens = groupsConfig.getRootNode().getChildren();
            for (ConfigurationNode node : childrens) {
                String name = node.getName();
                groups.put(name, new Group(groupsSubset.subset(name)));
            }

            Configuration ldapConfigurationSubset = testProperties.subset("configuration");
            HierarchicalConfiguration ldapConfig = ConfigurationUtils.convertToHierarchical(ldapConfigurationSubset);
            childrens = ldapConfig.getRootNode().getChildren();
            for (ConfigurationNode node : childrens) {
                String key = node.getName();
                String value = (String) node.getValue();
                ldapConfiguration.put(key, value);
            }
        } catch (ConfigurationException ex) {
            String message = "Problem loading configuration: " + ex.getMessage();
            log.error(message);
            throw new IllegalStateException(message);
        }
    }

    public void setup() throws Exception {
        setIpaLdapContext(ContextSourceFactory.getIPAContextSource(ldapConfiguration));
        getIpaLdapContext().afterPropertiesSet();
        setAdLdapContext(ContextSourceFactory.getADContextSource(ldapConfiguration));
        getAdLdapContext().afterPropertiesSet();
        ipaLdapTemplate = new LdapTemplate(getIpaLdapContext());
        adLdapTemplate = new LdapTemplate(getAdLdapContext());
        ipaPersonDao.setLdapTemplate(ipaLdapTemplate);
        ipaGroupDao.setLdapTemplate(ipaLdapTemplate);
        adPersonDao.setLdapTemplate(adLdapTemplate);
        adGroupDao.setLdapTemplate(adLdapTemplate);
    }

    public void populateUsersAndGroups() throws ConfigurationException, URISyntaxException {
        for (Person p : users.values()) {
            ipaPersonDao.create(p);
            adPersonDao.create(p);
        }

        List<String> list = new ArrayList<String>(groups.keySet());
        Collections.sort(list);
        for (String groupName : list ) {
            Group group = groups.get(groupName);
            ipaGroupDao.create(group);
            adGroupDao.create(group);
            System.out.println(groupName);
        }
    }

    public Person getUser(String userName) {
        return users.get(userName);
    }

    public Group getGroup(String groupName) {
        return groups.get(groupName);
    }

    public void cleanup() {
        for (Person p : users.values()) {
            ipaPersonDao.delete(p);
            adPersonDao.delete(p);
        }

        for (Group g : groups.values()) {
            ipaGroupDao.delete(g);
            adGroupDao.delete(g);
        }
    }

    public IPAGroupDaoImpl getIpaGroupDao() {
        return ipaGroupDao;
    }

    public IPAPersonDaoImpl getIpaPersonDao() {
        return ipaPersonDao;
    }

    public ADPersonDaoImpl getAdPersonDao() {
        return adPersonDao;
    }

    public ADGroupDaoImpl getAdGroupDao() {
        return adGroupDao;
    }

    public void setIpaLdapContext(LdapContextSource ipaLdapContext) {
        this.ipaLdapContext = ipaLdapContext;
    }

    public LdapContextSource getIpaLdapContext() {
        return ipaLdapContext;
    }

    public void setAdLdapContext(LdapContextSource adLdapContext) {
        this.adLdapContext = adLdapContext;
    }

    public LdapContextSource getAdLdapContext() {
        return adLdapContext;
    }
}
