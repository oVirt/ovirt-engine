package org.ovirt.engine.core.bll;

import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.ovirt.engine.core.common.action.DetachAdGroupFromTimeLeasedPoolParameters;
import org.ovirt.engine.core.common.action.DetachUserFromTimeLeasedPoolParameters;
import org.ovirt.engine.core.common.action.VmPoolToAdGroupParameters;
import org.ovirt.engine.core.common.action.VmPoolUserParameters;
import org.ovirt.engine.core.common.businessentities.ad_groups;
import org.ovirt.engine.core.common.businessentities.time_lease_vm_pool_map;
import org.ovirt.engine.core.common.queries.GetAdGroupByIdParameters;
import org.ovirt.engine.core.common.queries.VdcQueryReturnValue;
import org.ovirt.engine.core.common.queries.VdcQueryType;
import org.ovirt.engine.core.common.users.VdcUser;
import org.ovirt.engine.core.utils.MultiValueMapUtils;
import org.ovirt.engine.core.utils.log.Log;
import org.ovirt.engine.core.utils.log.LogFactory;
import org.ovirt.engine.core.dal.dbbroker.DbFacade;
import org.ovirt.engine.core.utils.timer.OnTimerMethodAnnotation;
import org.ovirt.engine.core.utils.timer.SchedulerUtilQuartzImpl;

/**
 * This class handle desktop pool of time leased typ
 */
public final class TimeLeasedVmPoolManager {
    private static Log log = LogFactory.getLog(TimeLeasedVmPoolManager.class);

    private static final TimeLeasedVmPoolManager _instance = new TimeLeasedVmPoolManager();

    private TimeLeasedVmPoolManager() {
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this, "OnStartTimer", new Class[0], new Object[0],
                0, 3, TimeUnit.SECONDS);
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this, "OnEndTimer", new Class[0], new Object[0],
                0, 3, TimeUnit.SECONDS);
        SchedulerUtilQuartzImpl.getInstance().scheduleAFixedDelayJob(this, "OnActionsTimer", new Class[0],
                new Object[0], 0, 3, TimeUnit.SECONDS);

        List<time_lease_vm_pool_map> allPools = DbFacade.getInstance().getVmPoolDAO().getAllTimeLeasedVmPoolMaps();
        for (time_lease_vm_pool_map map : allPools) {
            try {
                ConcreteAddAction(map, false);
                PrintQueues();
            } catch (java.lang.Exception e) {
                log.error(e);
            }
        }
        log.infoFormat("TimeLeasedVmPoolManager constractor entered");
    }

    public static TimeLeasedVmPoolManager getInstance() {
        return _instance;
    }

    /**
     * Queue of users/groups with vm pool map, wating to be attached
     */
    private SortedMap<Date, List<time_lease_vm_pool_map>> _startQueue =
            new TreeMap<Date, List<time_lease_vm_pool_map>>();
    /**
     * Queue of users/groups with vm pool map, wating to be detach
     */
    private SortedMap<Date, List<time_lease_vm_pool_map>> _endQueue = new TreeMap<Date, List<time_lease_vm_pool_map>>();

    /**
     * Time to attach vm pools to user/group
     */
    @OnTimerMethodAnnotation("OnStartTimer")
    public void OnStartTimer() {
        try {
            DoAction(_startQueue, true);
        } catch (java.lang.Exception e) {
        }
    }

    /**
     * Time to detach vm pools from user/group
     */
    @OnTimerMethodAnnotation("OnEndTimer")
    public void OnEndTimer() {
        try {
            DoAction(_endQueue, false);
        } catch (java.lang.Exception e) {
        }
    }

    /**
     * This function called then time lease starts/ends. Generally it call by
     * start/end timers. Algorithm: check appropriate timer on actions can be
     * done, do these actions, remove it from queues.
     *
     * @param queue
     * @param isAttach
     */
    private void DoAction(SortedMap<Date, List<time_lease_vm_pool_map>> queue, boolean isAttach) {
        if (queue.size() != 0) {
            /**
             * Store all times to remove in another list
             */
            java.util.ArrayList<java.util.Date> toRemove = new java.util.ArrayList<java.util.Date>();
            for (Map.Entry<Date, List<time_lease_vm_pool_map>> value : queue.entrySet()) {
                if (value.getKey().after(new java.util.Date())) {
                    break;
                }
                toRemove.add(value.getKey());
                for (time_lease_vm_pool_map map : value.getValue()) {
                    time_lease_vm_pool_map currMap = DbFacade.getInstance().getVmPoolDAO().getTimeLeasedVmPoolMapByIdForVmPool(
                            map.getid(), map.getvm_pool_id());
                    if (currMap != null) {
                        CommandBase command = createCommand(currMap, isAttach);
                        command.ExecuteAction();
                    }
                }
            }
            for (java.util.Date time : toRemove) {
                queue.remove(time);
            }
        }
    }

    /**
     * This function used to create appropriate command should be executed
     *
     * @param map
     * @param isAttach
     * @return
     */
    private CommandBase createCommand(time_lease_vm_pool_map map, boolean isAttach) {
        CommandBase returnValue = null;
        if (isAttach) {
            if (map.gettype() == 0) {
                /**
                 * User will be attached to pool only if it not attached yet
                 */
                if (DbFacade.getInstance().getTagDAO().getVmPoolTagsByVmPoolIdAndAdElementId(map.getvm_pool_id(), map.getid())
                        .size() == 0)
                // DbFacade.Instance.GetUserVmPoolByUserIdAndPoolId(map.id,
                // map.vm_pool_id) == null)
                {
                    VmPoolUserParameters tempVar = new VmPoolUserParameters(map.getvm_pool_id(), new VdcUser(
                            map.getid(), "", ""), true);
                    tempVar.setShouldBeLogged(true);

                    // old time-lease pools implementation
                    // should be re-implemented, don't remove comments below
                    // returnValue =
                    // CommandsFactory.CreateCommand(VdcActionType.AttachVmPoolToUser,
                    // tempVar);
                }
            } else {
                /**
                 * group will be attached to pool only if it not attached yet
                 */
                if (DbFacade.getInstance().getTagDAO().getVmPoolTagsByVmPoolIdAndAdElementId(map.getvm_pool_id(), map.getid()) == null) {
                    VdcQueryReturnValue ret = Backend.getInstance().runInternalQuery(VdcQueryType.GetAdGroupById,
                            new GetAdGroupByIdParameters(map.getid()));

                    if (ret != null && ret.getReturnValue() != null) {
                        Object tempVar2 = ret.getReturnValue();
                        ad_groups adGroup = (ad_groups) ((tempVar2 instanceof ad_groups) ? tempVar2 : null);
                        if (adGroup != null) {
                            VmPoolToAdGroupParameters tempVar3 = new VmPoolToAdGroupParameters(map.getvm_pool_id(),
                                    adGroup, true);
                            tempVar3.setShouldBeLogged(true);
                            // old time-lease pools implementation
                            // returnValue =
                            // CommandsFactory.CreateCommand(VdcActionType.AttachVmPoolToAdGroup,
                            // tempVar3);
                        }
                    }
                }
            }
        } else {
            if (map.gettype() == 0) {
                returnValue = new DetachUserFromTimeLeasedPoolCommand(new DetachUserFromTimeLeasedPoolParameters(
                        map.getvm_pool_id(), map.getid(), true));
            } else {
                returnValue = new DetachAdGroupFromTimeLeasedPoolCommand(new DetachAdGroupFromTimeLeasedPoolParameters(
                        map.getid(), map.getvm_pool_id(), true));
            }
        }

        return returnValue;
    }

    /**
     * Action types. Action - operation on queue.
     */
    private enum Actions {
        Add, Update, Remove;

        public int getValue() {
            return this.ordinal();
        }

        public static Actions forValue(int value) {
            return values()[value];
        }
    }

    /**
     * asyncronious actions handling. Action not handle directly on request -
     * instead there is actions queue where all actions stored and one time in 3
     * seconds all actions run in row. Actions timer on only where there is
     * actions in queue
     */
    @OnTimerMethodAnnotation("OnActionsTimer")
    public void OnActionsTimer() {
        // lock (sync)
        // {
        try {
            while (_actions.size() > 0) {
                TimeLeasedAction currentAction = _actions.poll();
                switch (currentAction._action) {
                case Add: {
                    ConcreteAddAction(currentAction._map, true);
                    break;
                }
                case Remove: {
                    ConcreteRemoveAction(currentAction._map);
                    break;
                }
                case Update: {
                    ConcreteUpdateAction(currentAction._map);
                    break;
                }
                }
                ActionDescription(currentAction);
            }
        } catch (java.lang.Exception e) {
        }
        // }
    }

    /**
     * This command run when some user/group should be added to pool. Its called
     * from two places: 1. during Vdc initialization. In this case no need to
     * update timers since there are many actions added and in the end of
     * initialization timers initialized. 2. When user/group added to pool from
     * Gui.
     *
     * @param map
     * @param proceedTimers
     *            true if command called from gui, otherwise false
     */
    private void ConcreteAddAction(time_lease_vm_pool_map map, boolean proceedTimers) {
        /**
         * if actions time in the past - generate detach command and run it
         */
        if (proceedTimers) {
            log.infoFormat("ConcreteAddAction entered with: {0}", map);
            PrintQueues();
        }
        if (map.getend_time().compareTo(new java.util.Date()) < 0) {
            CommandBase command = createCommand(map, false);
            command.ExecuteAction();
            return;
        } else if (map.getstart_time().compareTo(new java.util.Date()) < 0) {
            /**
             * if actios's start time in the past - run it
             */
            CommandBase command = createCommand(map, true);
            if (command != null) {
                command.ExecuteAction();
            }
        } else {
            /*
             * Add new action to start queue. If action must be scheduled before
             * first scheduled action - recalculate timer's timeout
             */
            MultiValueMapUtils.addToMap(map.getstart_time(), map, _startQueue);
        }

        /*
         * Add new action to end queue. If action must be scheduled before first
         * scheduled action - recalculate timer's timeout
         */
        MultiValueMapUtils.addToMap(map.getend_time(), map, _endQueue);
    }

    /**
     * This action called when user detach user/group from time leased pool
     * manually or then user updated times.
     *
     * @param map
     */
    private void ConcreteRemoveAction(time_lease_vm_pool_map map) {
        RemoveAction(_startQueue, map, map.getstart_time());
        RemoveAction(_endQueue, map, map.getend_time());
    }

    private void RemoveAction(SortedMap<java.util.Date, List<time_lease_vm_pool_map>> queue,
                              time_lease_vm_pool_map map, java.util.Date time) {
        List<time_lease_vm_pool_map> list = queue.get(time);
        if (list != null) {
            time_lease_vm_pool_map toRemove = null;
            for (time_lease_vm_pool_map currMap : list) {
                if (currMap.getvm_pool_id().equals(map.getvm_pool_id()) && currMap.getid().equals(map.getid())) {
                    toRemove = currMap;
                    break;
                }
            }
            if (toRemove != null) {
                list.remove(toRemove);
                if (list.isEmpty()) {
                    queue.remove(time);
                }
            }
        }
    }

    public void AddAction(time_lease_vm_pool_map map) {
        synchronized (_actions) {
            TimeLeasedAction action = new TimeLeasedAction(Actions.Add, map);
            InternalAddAction(action);
        }
    }

    public void RemoveAction(time_lease_vm_pool_map map) {
        synchronized (_actions) {
            TimeLeasedAction action = new TimeLeasedAction(Actions.Remove, map);
            InternalAddAction(action);
        }
    }

    private void InternalAddAction(TimeLeasedAction action) {
        _actions.offer(action);
    }

    public void UpdateAction(time_lease_vm_pool_map map) {
        synchronized (_actions) {
            TimeLeasedAction action = new TimeLeasedAction(Actions.Update, map);
            InternalAddAction(action);
        }
    }

    private void ConcreteUpdateAction(time_lease_vm_pool_map map) {
        ConcreteRemoveAction(map.oldMap);
        ConcreteAddAction(map, true);
    }

    private static class TimeLeasedAction {
        public TimeLeasedAction(Actions action, time_lease_vm_pool_map map) {
            _action = action;
            _map = map;
        }

        public Actions _action = Actions.forValue(0);
        public time_lease_vm_pool_map _map;
    }

    private Queue<TimeLeasedAction> _actions = new LinkedList<TimeLeasedAction>();

    private void ActionDescription(TimeLeasedAction action) {
        log.infoFormat("Time leased manager action: {0} for {1}", action._action.toString(), action._map.toString());
        PrintQueues();
    }

    public void PrintQueues() {
        log.infoFormat("Start queue time leased manager. Next Peektime");
        for (List<time_lease_vm_pool_map> list : _startQueue.values()) {
            for (time_lease_vm_pool_map map : list) {
                log.infoFormat("{0}", map.toString());
            }
        }
        log.info("End queue time leased manager:");
        for (List<time_lease_vm_pool_map> list : _endQueue.values()) {
            for (time_lease_vm_pool_map map : list) {
                log.infoFormat("{0}", map.toString());
            }
        }
    }

}
