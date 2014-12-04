update users set group_ids = regexp_replace(group_ids, '([,]*)00000000-0000-0000-0000-000000000000([,]*)', '');
