INSERT INTO permissions (
    id,
    role_id,
    ad_element_id,
    object_id,
    object_type_id
  )
  VALUES (
    '58ca604b-017d-0374-0220-00000000014e', -- default mac pool id
    'def00014-0000-0000-0000-def000000014', -- mac pool user role id
    getGlobalIds('everyone'), -- all users
    '58ca604b-017d-0374-0220-00000000014e',-- default mac pool id
    28 -- mac pool type
  );
