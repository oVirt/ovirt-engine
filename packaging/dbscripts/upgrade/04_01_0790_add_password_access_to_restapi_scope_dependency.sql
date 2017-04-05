UPDATE sso_scope_dependency
SET dependencies = dependencies || ' ovirt-ext=token:password-access'
WHERE scope = 'ovirt-app-api';
