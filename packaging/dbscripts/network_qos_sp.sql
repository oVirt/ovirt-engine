----------------------------------------------------------------
-- [network_qos] Table
----------------------------------------------------------------

Create or replace FUNCTION InsertNetworkQos(v_id uuid,
  v_name VARCHAR(50),
  v_storage_pool_id uuid,
  v_inbound_average INTEGER,
  v_inbound_peak INTEGER,
  v_inbound_burst INTEGER,
  v_outbound_average INTEGER,
  v_outbound_peak INTEGER,
  v_outbound_burst INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
INSERT INTO network_qos(id, name, storage_pool_id, inbound_average, inbound_peak, inbound_burst, outbound_average, outbound_peak, outbound_burst)
  VALUES(v_id, v_name, v_storage_pool_id, v_inbound_average, v_inbound_peak, v_inbound_burst, v_outbound_average, v_outbound_peak, v_outbound_burst);
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION UpdateNetworkQos(v_id uuid,
  v_name VARCHAR(50),
  v_storage_pool_id uuid,
  v_inbound_average INTEGER,
  v_inbound_peak INTEGER,
  v_inbound_burst INTEGER,
  v_outbound_average INTEGER,
  v_outbound_peak INTEGER,
  v_outbound_burst INTEGER)
RETURNS VOID
   AS $procedure$
BEGIN
      UPDATE network_qos
      SET name = v_name, storage_pool_id = v_storage_pool_id, inbound_average = v_inbound_average, inbound_peak = v_inbound_peak, inbound_burst = v_inbound_burst,
      outbound_average = v_outbound_average, outbound_peak = v_outbound_peak, outbound_burst = v_outbound_burst, _update_date = LOCALTIMESTAMP
      WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION DeleteNetworkQos(v_id UUID) RETURNS VOID
   AS $procedure$
BEGIN
   DELETE FROM network_qos
   WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;



Create or replace FUNCTION GetAllFromNetworkQoss() RETURNS SETOF network_qos
   AS $procedure$
BEGIN
   RETURN QUERY SELECT *
   FROM network_qos;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetNetworkQosByNetworkQosId(v_id UUID) RETURNS SETOF network_qos
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM network_qos
   WHERE id = v_id;
END; $procedure$
LANGUAGE plpgsql;


Create or replace FUNCTION GetAllNetworkQosForStoragePool(v_storage_pool_id UUID) RETURNS SETOF network_qos
   AS $procedure$
BEGIN
RETURN QUERY SELECT *
   FROM network_qos
   WHERE storage_pool_id = v_storage_pool_id;
END; $procedure$
LANGUAGE plpgsql;
