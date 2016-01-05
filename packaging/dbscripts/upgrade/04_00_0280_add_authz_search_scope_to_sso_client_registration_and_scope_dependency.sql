UPDATE sso_clients
SET scope = scope || ' ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search';

UPDATE sso_scope_dependency
SET dependencies = 'ovirt-app-api ovirt-ext=token:password-access ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all'
WHERE scope = 'ovirt-app-admin';


UPDATE sso_scope_dependency
SET dependencies = 'ovirt-app-api ovirt-ext=token:password-access ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all'
WHERE scope = 'ovirt-app-portal';

UPDATE sso_scope_dependency
SET dependencies = 'ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search ovirt-ext=token-info:validate'
WHERE scope = 'ovirt-app-api';
