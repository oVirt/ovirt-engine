package org.ovirt.engine.core.itests;

import java.util.ArrayList;
import java.util.Random;

import junit.framework.Assert;

import org.junit.Ignore;
import org.junit.Test;

import org.ovirt.engine.core.common.action.AddUserParameters;
import org.ovirt.engine.core.common.action.VdcActionType;
import org.ovirt.engine.core.common.businessentities.AdUser;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.interfaces.SearchType;
import org.ovirt.engine.core.common.queries.SearchParameters;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;

/**
 * Test class for various user scenarios
 *
 * @see {@link AddUserCommand} , {@link ad_groups} , {@link DbUser}
 */
@Ignore
public class UserTest extends AbstractBackendTest {

    @Test
    public void prepare() {
        runAsSuperAdmin();
    }

    /**
     * AddUserCommand test. Steps: - get a random user from active directory - fire the addUserCommand
     *
     * result - success if command getSucceded() return true
     */
    @Test
    public void addUser() {
        SearchParameters p = new SearchParameters(String.format("ADUSER@%s: allnames=t*", getUser()
                .getDomainControler()), SearchType.AdUser);
        p.setMaxCount(10);
        ArrayList<AdUser> users = (ArrayList<AdUser>) backend.runInternalQuery(VdcQueryType.Search, p).getReturnValue();
        AdUser newUser = users.get(new Random().nextInt(users.size()));

        AddUserParameters parameters = new AddUserParameters();
        parameters.setVdcUser(new VdcUser(newUser));
        Assert.assertTrue(backend.RunAction(VdcActionType.AddUser, parameters).getSucceeded());
    }

    /**
     * AddUserCommand test of type ad_groups (Active directory holds users and groups) Steps: - get a random group from
     * active directory - fire the addUserCommand
     *
     * result - success if command getSucceded() return true
     */
    @Test
    public void addGroup() {
        SearchParameters p = new SearchParameters(String.format("ADGROUP@%s : name=C*", getUser()
                .getDomainControler()), SearchType.AdGroup);
        p.setMaxCount(10);
        ArrayList<ad_groups> users =
                (ArrayList<ad_groups>) backend.runInternalQuery(VdcQueryType.Search, p).getReturnValue();
        ad_groups newGroup = users.get(new Random().nextInt(users.size()));

        AddUserParameters parameters = new AddUserParameters();
        parameters.setAdGroup(new ad_groups(newGroup.getid(), newGroup.getname(), newGroup.getdomain(),newGroup.getDistinguishedName()));
        Assert.assertTrue(backend.RunAction(VdcActionType.AddUser, parameters).getSucceeded());
    }
}
