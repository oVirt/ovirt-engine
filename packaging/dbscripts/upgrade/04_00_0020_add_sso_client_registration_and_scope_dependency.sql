CREATE SEQUENCE sso_clients_seq INCREMENT BY 1 START WITH 1;
CREATE SEQUENCE sso_scope_dependency_seq INCREMENT BY 1 START WITH 1;

CREATE TABLE sso_clients
(
  id bigint DEFAULT nextval('sso_clients_seq'::regclass) NOT NULL,
  client_id VARCHAR(128) NOT NULL,
  client_secret VARCHAR(1024) NOT NULL,
  callback_prefix VARCHAR(1024),
  certificate_location VARCHAR(1024),
  notification_callback VARCHAR(1024),
  description TEXT,
  email VARCHAR(256),
  scope VARCHAR(1024),
  trusted BOOLEAN NOT NULL DEFAULT TRUE,
  notification_callback_protocol VARCHAR(32) NOT NULL,
  notification_callback_verify_host BOOLEAN NOT NULL DEFAULT FALSE,
  notification_callback_verify_chain BOOLEAN NOT NULL DEFAULT TRUE,
  CONSTRAINT pk_sso_clients PRIMARY KEY (id)
);

CREATE TABLE sso_scope_dependency
(
  id bigint DEFAULT nextval('sso_scope_dependency_seq'::regclass) NOT NULL,
  scope VARCHAR(128) NOT NULL,
  dependencies TEXT,
  CONSTRAINT pk_sso_scope_dependency PRIMARY KEY (id)
);

INSERT INTO sso_scope_dependency (scope,
                                  dependencies)
VALUES ('ovirt-app-admin',
        'ovirt-app-api ovirt-ext=token:password-access ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all');

INSERT INTO sso_scope_dependency (scope,
                                  dependencies)
VALUES ('ovirt-app-portal',
        'ovirt-app-api ovirt-ext=token:password-access ovirt-ext=token-info:validate ovirt-ext=revoke:revoke-all');

INSERT INTO sso_scope_dependency (scope,
                                  dependencies)
VALUES ('ovirt-app-api',
        'ovirt-ext=token-info:validate');
