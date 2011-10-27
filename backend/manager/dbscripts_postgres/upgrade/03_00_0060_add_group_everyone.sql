INSERT into ad_groups (id,name,status,domain,distinguishedname)
 select getGlobalIds('everyone'),
 'Everyone',
 1,
 '',
 ''
where not exists (
 select id from ad_groups where id = getGlobalIds('everyone'))
