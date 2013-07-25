update vds_static set ssh_username='root' where ssh_username is null;
update vds_static set ssh_port=22 where ssh_port is null;
