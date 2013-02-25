update base_disks set alignment=0 where alignment is null;
alter table base_disks alter column alignment set not null;
