SELECT fn_db_create_constraint('providers', 'valid_auth_url', 'CHECK (auth_url ~ ''^http(s)?://[^/]*:[\d]+/(v3|v2\.0)/?$'')');
