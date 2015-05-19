--- The fence agents package broke backward compatibility by not supporting boolean flags to be provided without
--- a value. i.e. from now on for example 'lanplus' is not legal while 'lanplus=1' or 'lanplus=true' is OK
--- This upgrade script will change all known flags used by existing agents to have an explicit value.

Create or replace FUNCTION __temp_fix_bool_fence_agents_options()
RETURNS VOID
   AS $procedure$
   DECLARE
   v_agents varchar[] := array['inet4-only', 'inet6-only', 'lanplus', 'missing-as-off', 'notls', 'snmp-priv-passwd-script',
                               'ssh', 'ssl-insecure', 'ssl-secure', 'ssl', 'use-sudo', 'verbose', 'version'];
   v_temp_option_name varchar(50);
   v_option_true_val varchar(50);
   v_option_one_val varchar(50);

BEGIN
    for i in 1 .. array_upper(v_agents, 1)
    loop
        v_temp_option_name := 'temp__' || upper(v_agents[i]) || '__expr';
        v_option_true_val := v_agents[i] || '=' || 'true';
        v_option_one_val := v_agents[i] || '=' || '1';
        update fence_agents set options =  replace(options, v_option_one_val, v_temp_option_name) where not encrypt_options;
        update fence_agents set options =  replace(options, v_option_true_val, v_temp_option_name) where not encrypt_options;
        update fence_agents set options =  replace(options, v_agents[i], v_option_one_val) where not encrypt_options;
        update fence_agents set options =  replace(options, v_temp_option_name, v_option_one_val) where not encrypt_options;
    end loop;
    --fix options
    -- in case we have options that one is substring of the other we will get '=1-' expressions that
    -- should be replaced with '-' (for example 'ssl-insecure', 'ssl-secure', 'ssl')
    update fence_agents set options =  replace(options,'=1-','-') where not encrypt_options;
END; $procedure$
LANGUAGE plpgsql;

SELECT __temp_fix_bool_fence_agents_options();
DROP function __temp_fix_bool_fence_agents_options();

