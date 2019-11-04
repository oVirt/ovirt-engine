INSERT INTO sso_scope_dependency (scope,
                                  dependencies)
VALUES ('ovirt-app-admin',
        'ovirt-app-api ovirt-ext=token:password-access ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all');

INSERT INTO sso_scope_dependency (scope,
                                  dependencies)
VALUES ('ovirt-app-portal',
        'ovirt-app-api ovirt-ext=token:password-access ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all');

INSERT INTO sso_scope_dependency (scope,
                                  dependencies)
VALUES ('ovirt-app-api',
        'ovirt-ext=token-info:authz-search ovirt-ext=token-info:public-authz-search ovirt-ext=token-info:validate ovirt-ext=token:password-access');

