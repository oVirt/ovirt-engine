/************************************************************************************************
 The following are helper SP for hecleaner utility and are not exposed to the application DAOs

If you add a function here, drop it in hecleaner_sp_drop.sql
************************************************************************************************/

CREATE OR REPLACE FUNCTION DeleteHostedEngineStorageVM()
  RETURNS void AS
$procedure$
DECLARE
    v_HostedEngineVmName varchar;
    v_vm_guid UUID;
    v_vm_guid_check UUID;
    v_storage_id UUID;
    v_image_guid UUID;
    v_disk_id UUID;
BEGIN
    SELECT option_value
        INTO v_HostedEngineVmName
        FROM getvdcoptionbyname('HostedEngineVmName', 'general') ;
    BEGIN
        SELECT vm_guid
            INTO STRICT v_vm_guid
            FROM getvmstaticbyname(v_HostedEngineVmName);
        EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE EXCEPTION 'VM % not found', v_HostedEngineVmName;
        WHEN TOO_MANY_ROWS THEN
            RAISE EXCEPTION 'VM % not unique', v_HostedEngineVmName;
    END;
    BEGIN
        SELECT storage_id, image_guid, disk_id
            INTO STRICT v_storage_id, v_image_guid, v_disk_id
            FROM getdisksvmguids(ARRAY[v_vm_guid]);
        EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE EXCEPTION 'VM % disk not found', v_HostedEngineVmName;
        WHEN TOO_MANY_ROWS THEN
            RAISE EXCEPTION 'VM % disk not unique', v_HostedEngineVmName;
    END;
    BEGIN
        SELECT vm_guid
            INTO STRICT v_vm_guid_check
            FROM getvmsbystoragedomainid(v_storage_id);
        EXCEPTION
        WHEN NO_DATA_FOUND THEN
            RAISE WARNING 'VM % is not on the hosted-engine storage domain', v_HostedEngineVmName;
        WHEN TOO_MANY_ROWS THEN
            RAISE WARNING 'The hosted-engine storage domain contains disks for more than one vm; this procedure will filter out all the disks on the hosted-engine storage domain including other VMs ones.';
        IF v_vm_guid <> v_vm_guid_check THEN
            RAISE EXCEPTION 'The hosted-engine storage domain contains a vm that is not the hosted-engine one.';
        END IF;
    END;
    PERFORM DeleteLUN(lun_id) FROM (
        SELECT (GetLUNsBystorage_server_connection(id)).lun_id FROM (
            SELECT id
                FROM GetStorageServerConnectionsForDomain(v_storage_id)
    ) t) t;
    PERFORM deletestorage_server_connections(id)
        FROM (
            SELECT id
                FROM getstorageserverconnectionsfordomain(v_storage_id)
        ) t;
    PERFORM Force_Delete_storage_domain(v_storage_id);
END;
$procedure$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION DeleteHostedEngineHosts()
  RETURNS void AS
$procedure$
BEGIN
    UPDATE vm_dynamic
        SET migrating_to_vds = NULL
        WHERE migrating_to_vds IN (
            SELECT vds_id
                FROM vds_statistics
                    WHERE ha_score IS NOT NULL AND ha_configured
        );
    UPDATE vm_dynamic
        SET
            run_on_vds = NULL,
            status = 0, -- 0=Down
            exit_status = 0, -- 0=Normal
            exit_reason = 6 -- 6=AdminShutdown
        WHERE run_on_vds IN (
            SELECT vds_id
                FROM vds_statistics
                    WHERE ha_score IS NOT NULL AND ha_configured
        );
    PERFORM deletevds(vds_id)
        FROM (
            SELECT vds_id
                FROM vds_statistics
                WHERE ha_score IS NOT NULL AND ha_configured
        ) t;
    UPDATE vds_spm_id_map SET vds_spm_id=(SELECT MAX(vds_spm_id)+1 FROM vds_spm_id_map) WHERE vds_spm_id=1;
END;
$procedure$
LANGUAGE plpgsql;
