-- Add new column
select fn_db_add_column('vds_groups', 'ha_reservation', 'boolean not null default false');

-- Insert new weight and balance methods
INSERT INTO policy_units (id, name, is_internal, custom_properties_regex, type, description) VALUES ('7f262d70-6cac-11e3-981f-0800200c9a66', 'OptimalForHaReservation', true, '{
  "ScaleDown" : "(100|[1-9]|[1-9][0-9])$"
}', 1, 'Weights hosts according to their HA score regardless of hosted engine');

INSERT INTO cluster_policy_units (cluster_policy_id, policy_unit_id, filter_sequence, factor) SELECT cluster_policies.id, '7f262d70-6cac-11e3-981f-0800200c9a66', 0, 1 FROM cluster_policies;

-- Add new notifications
INSERT INTO event_map(event_up_name, event_down_name) VALUES ('CLUSTER_ALERT_HA_RESERVATION', '');
