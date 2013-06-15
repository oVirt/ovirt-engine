--Add ssh key fingerprint field into vds_static
SELECT fn_db_add_column('vds_static', 'sshKeyFingerprint', 'character varying(128)');
