UPDATE users
  SET username = 'admin',
      name = 'admin'
  WHERE username = 'admin@internal'
    AND domain = 'internal-authz';
