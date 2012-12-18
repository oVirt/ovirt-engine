-- Inserting a UserVmManager permission on every PowerUser permission directly on a VM
INSERT INTO permissions (id, role_id, ad_element_id, object_id, object_type_id)
      (SELECT uuid_generate_v1(),
             'def00006-0000-0000-0000-def000000006',
             ad_element_id,
             object_id,
             object_type_id
       FROM permissions perm1
       WHERE role_id = '00000000-0000-0000-0001-000000000002'
       AND object_type_id = 2 AND NOT EXISTS (
           SELECT *
           FROM permissions perm2
           WHERE role_id = 'def00006-0000-0000-0000-def000000006'
           AND object_type_id = 2
           AND perm1.object_id = perm2.object_id
           AND perm1.ad_element_id = perm2.ad_element_id
       )
);
