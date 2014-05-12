

----------------------------------------------------------------
-- [disk_image_dynamic] Table
--




Create or replace FUNCTION Insertdisk_image_dynamic(v_image_id UUID,
	v_read_rate INTEGER ,
	v_write_rate INTEGER ,
	v_actual_size BIGINT,
	v_read_latency_seconds numeric(18,9),
	v_write_latency_seconds numeric(18,9),
	v_flush_latency_seconds numeric(18,9)
)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO disk_image_dynamic(image_id, read_rate, write_rate, actual_size, read_latency_seconds, write_latency_seconds, flush_latency_seconds)
	VALUES(v_image_id, v_read_rate, v_write_rate, v_actual_size, v_read_latency_seconds, v_write_latency_seconds, v_flush_latency_seconds);
END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Updatedisk_image_dynamic(v_image_id UUID,
	v_read_rate INTEGER ,
	v_write_rate INTEGER ,
	v_actual_size BIGINT ,
	v_read_latency_seconds numeric(18,9) ,
	v_write_latency_seconds numeric(18,9) ,
	v_flush_latency_seconds numeric(18,9))
RETURNS VOID

	--The [disk_image_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE disk_image_dynamic
      SET read_rate = v_read_rate,write_rate = v_write_rate,actual_size = v_actual_size,read_latency_seconds = v_read_latency_seconds,write_latency_seconds = v_write_latency_seconds,flush_latency_seconds = v_flush_latency_seconds, _update_date = LOCALTIMESTAMP
      WHERE image_id = v_image_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Updatedisk_image_dynamic_by_disk_id(v_image_group_id UUID,
	v_read_rate INTEGER ,
	v_write_rate INTEGER ,
	v_actual_size BIGINT ,
	v_read_latency_seconds numeric(18,9) ,
	v_write_latency_seconds numeric(18,9) ,
	v_flush_latency_seconds numeric(18,9))
RETURNS VOID

	--The [disk_image_dynamic] table doesn't have a timestamp column. Optimistic concurrency logic cannot be generated
   AS $procedure$
BEGIN
      UPDATE disk_image_dynamic
      SET read_rate = v_read_rate,write_rate = v_write_rate,actual_size = v_actual_size,read_latency_seconds = v_read_latency_seconds,write_latency_seconds = v_write_latency_seconds,flush_latency_seconds = v_flush_latency_seconds, _update_date = LOCALTIMESTAMP
      WHERE image_id in (SELECT distinct image_guid
                FROM   images
                WHERE  image_group_id = v_image_group_id and active = true);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION Deletedisk_image_dynamic(v_image_id UUID)
RETURNS VOID
   AS $procedure$
BEGIN

   DELETE FROM disk_image_dynamic
   WHERE image_id = v_image_id;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION GetAllFromdisk_image_dynamic() RETURNS SETOF disk_image_dynamic STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM disk_image_dynamic;

END; $procedure$
LANGUAGE plpgsql;





Create or replace FUNCTION Getdisk_image_dynamicByimage_id(v_image_id UUID)
RETURNS SETOF disk_image_dynamic STABLE
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM disk_image_dynamic
   WHERE image_id = v_image_id;

END; $procedure$
LANGUAGE plpgsql;


