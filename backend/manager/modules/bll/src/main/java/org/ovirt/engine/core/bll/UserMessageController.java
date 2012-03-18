package org.ovirt.engine.core.bll;

import java.util.LinkedList;
import java.util.List;

import org.ovirt.engine.core.common.businessentities.DbUser;
import org.ovirt.engine.core.common.businessentities.VmDynamic;
import org.ovirt.engine.core.compat.Guid;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.MultiValueMapUtils;

public class UserMessageController {
    private final static UserMessageController _instance = new UserMessageController();
    private final java.util.HashMap<Guid, List<String>> mUsersMessages = new java.util.HashMap<Guid, List<String>>();

    public static UserMessageController getInstance() {
        return _instance;
    }

    public void AddUserMessage(Guid user, String userMessage) {
        MultiValueMapUtils.addToMap(user, userMessage, mUsersMessages);
    }

    public void AddUserMessageByVds(Guid vdsId, String userMessage) {
        List<Guid> users = new LinkedList<Guid>();
        for (VmDynamic vm : DbFacade.getInstance().getVmDynamicDAO().getAllRunningForVds(vdsId)) {
            AddVmUsersToList(users, vm.getId());
        }
        AddUsersMessages(users, userMessage);
    }

    public void AddUserMessageByVm(Guid vmId, String userMessage) {
        List<Guid> users = new LinkedList<Guid>();
        AddVmUsersToList(users, vmId);
        AddUsersMessages(users, userMessage);
    }

    private static void AddVmUsersToList(List<Guid> input, Guid vmId) {
        List<DbUser> users = DbFacade.getInstance().getDbUserDAO()
                .getAllForVm(vmId);
        if (users != null) {
            for (DbUser user : users) {
                if (!input.contains((user.getuser_id()))) {
                    input.add(user.getuser_id());
                }
            }
        }
    }

    private void AddUsersMessages(Iterable<Guid> users, String message) {
        for (Guid userId : users) {
            AddUserMessage(userId, message);
        }
    }

    public String GetUserMessage(Guid user) {
        if (mUsersMessages.containsKey(user)) {
            List<String> userMessages = mUsersMessages.get(user);
            StringBuilder builder = new StringBuilder();
            for (String message : userMessages) {
                builder.append(message);
                builder.append("\n");
            }
            mUsersMessages.remove(user);
        }
        return "";
    }
}
