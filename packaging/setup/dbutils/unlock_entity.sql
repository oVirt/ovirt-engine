/************************************************************************************************
 The following are helper SP for unlock_entity.sh utility and are not exposed to the application DAOs

If you add a function here, drop it in unlock_entity_drop.sql
************************************************************************************************/
-- Unlocks all locked entities
create or replace FUNCTION fn_db_unlock_all()
returns void
AS $procedure$
declare
    DOWN integer;
    OK integer;
    LOCKED integer;
    TEMPLATE_OK integer;
    TEMPLATE_LOCKED integer;
    IMAGE_LOCKED integer;
    SNAPSHOT_OK varchar;
    SNAPSHOT_LOCKED varchar;
    ILLEGAL integer;
BEGIN
    DOWN:=0;
    OK:=1;
    LOCKED:=2;
    TEMPLATE_OK:=0;
    TEMPLATE_LOCKED:=1;
    IMAGE_LOCKED:=15;
    SNAPSHOT_OK:='OK';
    SNAPSHOT_LOCKED:='LOCKED';
    ILLEGAL:=4;
    update vm_static set template_status = TEMPLATE_OK where template_status = TEMPLATE_LOCKED;
    update vm_dynamic set status = DOWN where status = IMAGE_LOCKED;
    update images set imagestatus = OK where imagestatus = LOCKED;
    update snapshots set status = SNAPSHOT_OK where status ilike SNAPSHOT_LOCKED;
    UPDATE images SET imagestatus = OK WHERE imagestatus = ILLEGAL;
END; $procedure$
LANGUAGE plpgsql;
