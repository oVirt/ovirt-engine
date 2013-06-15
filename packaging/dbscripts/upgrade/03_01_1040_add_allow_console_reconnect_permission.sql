-- Add the permission to force connect to virtual machines to the
-- superuser:

insert into roles_groups (
  role_id,
  action_group_id
)
select
  '00000000-0000-0000-0000-000000000001',
  13
where
  not exists (
    select
      *
    from
      roles_groups
    where
      role_id = '00000000-0000-0000-0000-000000000001' and
      action_group_id = 13
  )
;

