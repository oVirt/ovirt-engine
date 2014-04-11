Create or replace FUNCTION InsertMacPool(
  v_id UUID,
  v_name VARCHAR(40),
  v_allow_duplicate_mac_addresses BOOLEAN,
  v_description VARCHAR(4000))
RETURNS VOID
   AS $procedure$
BEGIN
  INSERT INTO
      mac_pools(id,
               name,
               allow_duplicate_mac_addresses,
               description)
  VALUES(v_id,
         v_name,
         v_allow_duplicate_mac_addresses,
         v_description);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateMacPool(
  v_id      UUID,
  v_name    VARCHAR(40),
  v_allow_duplicate_mac_addresses BOOLEAN,
  v_description VARCHAR(4000))
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE
          mac_pools
      SET id=v_id,
          name=v_name,
          allow_duplicate_mac_addresses=v_allow_duplicate_mac_addresses,
          description=v_description
      WHERE
          id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteMacPool(v_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN
    DELETE FROM mac_pools WHERE id=v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetMacPoolByMacPoolId(v_id UUID) RETURNS SETOF mac_pools STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM mac_pools
   WHERE id = v_id;


END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetDefaultMacPool() RETURNS SETOF mac_pools STABLE
   AS $procedure$

BEGIN
   RETURN QUERY SELECT *
   FROM mac_pools
   where default_pool is true;

END; $procedure$
LANGUAGE plpgsql;

Create or replace FUNCTION GetMacPoolByDataCenterId(v_id UUID) RETURNS SETOF mac_pools STABLE
   AS $procedure$

BEGIN
   RETURN QUERY SELECT mp.*
   FROM mac_pools mp join storage_pool sp on sp.mac_pool_id=mp.id
   where sp.id=v_id;

END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllFromMacPools()
  RETURNS SETOF mac_pools STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM mac_pools;

END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetMacPoolUsageCountById(v_id UUID)
  RETURNS SETOF BIGINT STABLE
AS $procedure$
BEGIN
   RETURN QUERY SELECT count(*)
   FROM storage_pool sp
   WHERE sp.mac_pool_id=v_id;

END; $procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION GetAllMacsByMacPoolId(v_id UUID)
  RETURNS SETOF VARCHAR STABLE
AS $procedure$
BEGIN
  RETURN QUERY SELECT mac_addr
               FROM   vm_interface
               WHERE  EXISTS(SELECT 1
                            FROM  vm_static
                            JOIN  vds_groups ON vm_static.vds_group_id = vds_groups.vds_group_id
                            WHERE vds_groups.storage_pool_id IN (SELECT sp.id
                                                                 FROM   storage_pool sp
                                                                 WHERE  sp.mac_pool_id = v_id)
                              AND vm_static.vm_guid = vm_interface.vm_guid);
END; $procedure$
LANGUAGE plpgsql;


-- Procedures for MAC ranges
Create or replace FUNCTION InsertMacPoolRange(
  v_mac_pool_id UUID,
  v_from_mac CHARACTER VARYING(17),
  v_to_mac CHARACTER VARYING(17))
RETURNS VOID
AS $procedure$
BEGIN
  INSERT
  INTO   mac_pool_ranges(mac_pool_id,
                         from_mac,
                         to_mac)
  VALUES (v_mac_pool_id,
          v_from_mac,
          v_to_mac);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteMacPoolRangesByMacPoolId(v_id UUID)
RETURNS VOID
AS $procedure$
BEGIN
    DELETE
    FROM   mac_pool_ranges
    WHERE  mac_pool_id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllMacPoolRangesByMacPoolId(v_id UUID)
RETURNS SETOF mac_pool_ranges STABLE
AS $procedure$
BEGIN
    RETURN QUERY
    SELECT *
    FROM   mac_pool_ranges
    WHERE  mac_pool_id = v_id;

END; $procedure$
LANGUAGE plpgsql;



