-- Migration (DB script 04_05_0370): pin i440fx VM emulated machines and rewrite cluster defaults to Q35-only
--
-- Rationale: RHEL 10 replaced the legacy i440fx machine types that RHEL 9 still provides
-- with pc-i440fx-rhel10.0.0 (i440fx is deprecated in RHEL 10 and will be removed in a
-- future major version). No i440fx machine type is supported by both RHEL 9 and RHEL 10,
-- so the cluster default is simplified to Q35-only. i440fx VMs must have their emulated machine set explicitly via
-- vm_static.custom_emulated_machine (VMs) and via vm_static.custom_emulated_machine on
-- template rows (vm_static rows with entity_type = 'TEMPLATE'; there is no separate
-- vm_templates table in the schema).
--
-- Scope: this script applies only to clusters already at compatibility level >= 4.8, the first
-- level where CentOS 10 hosts exist (CentOS 10 removed the legacy i440fx machine types that
-- CentOS 9 still provides, so no i440fx machine type is common to both). Clusters below 4.8 are
-- left completely untouched: they keep the legacy dual-format cluster default
-- ("pc-i440fx-...;pc-q35-...") and their unpinned i440fx VMs, which the runtime resolves via
-- the dual-format-aware readers. Such a cluster is migrated when an admin raises its level to
-- >= 4.8 (see UpdateClusterCommand), which is the natural moment for it: the raise is validated
-- against the hosts' common i440fx machine types and the VMs are pinned in the same command.
-- Clusters at level <= 4.3 (whose machine lists contain no Q35 type) are unaffected either way.
--
-- The 4.8 ClusterEmulatedMachines config value change (0000_config.sql) stays unconditional:
-- config values are keyed per version and never modify existing cluster rows.
--
-- Steps (all limited to clusters at compatibility level >= 4.8):
-- NOTE: compatibility_version >= '4.8' is a lexicographic string compare on the 'major.minor'
-- column value; it matches an existing codebase pattern (e.g. >= '3.6') and is exact for the
-- currently existing levels (a hypothetical future '4.10' would not sort after '4.8').
-- 1. Pin custom_emulated_machine on running i440fx VMs (copy from vm_dynamic)
-- 2. Pin custom_emulated_machine on down i440fx VMs (extract from cluster.emulated_machine)
-- 2b. Pin custom_emulated_machine on i440fx templates (extract from cluster.emulated_machine)
-- 3. Rewrite cluster.emulated_machine to Q35-only (guarded; deviant rows fall back to detect)
-- 3b. Single-entry i440fx defaults: reset to detect

-- Step 0: Capture the entities the pinning steps are about to touch, with the pin each
-- will receive, so the audit-log summary in Step 4 can report exact counts (after the
-- steps run, freshly pinned rows are indistinguishable from previously pinned ones).
-- The conditions below mirror Steps 1/2/2b exactly: a running VM is pinned from
-- vm_dynamic (Step 1); anything else only if the dual default has a usable i440fx part.
DROP TABLE IF EXISTS tmp_i440fx_migration_pinned;
CREATE TEMP TABLE tmp_i440fx_migration_pinned AS
SELECT vm_static.cluster_id,
       vm_static.vm_guid,
       COALESCE(vm_dynamic.emulated_machine, split_part(cluster.emulated_machine, ';', 1)) AS pin
FROM vm_static
JOIN cluster
  ON vm_static.cluster_id = cluster.cluster_id
 AND cluster.compatibility_version >= '4.8'
LEFT JOIN vm_dynamic
  ON vm_static.vm_guid = vm_dynamic.vm_guid
 AND vm_dynamic.emulated_machine LIKE '%i440fx%'
WHERE vm_static.bios_type = 1
  AND (vm_static.custom_emulated_machine IS NULL OR vm_static.custom_emulated_machine = '')
  AND (vm_dynamic.emulated_machine IS NOT NULL
       OR (cluster.emulated_machine IS NOT NULL
           AND cluster.emulated_machine LIKE '%i440fx%'
           AND cluster.emulated_machine NOT LIKE ';%'
           AND split_part(cluster.emulated_machine, ';', 1) NOT LIKE '%q35%'));

-- Step 1: Pin custom_emulated_machine on running i440fx VMs
-- For running VMs, copy vm_dynamic.emulated_machine -> vm_static.custom_emulated_machine
-- bios_type = 1 corresponds to I440FX_SEA_BIOS
UPDATE vm_static
SET custom_emulated_machine = vm_dynamic.emulated_machine
FROM vm_dynamic, cluster
WHERE vm_static.bios_type = 1
  AND (vm_static.custom_emulated_machine IS NULL OR vm_static.custom_emulated_machine = '')
  AND vm_static.vm_guid = vm_dynamic.vm_guid
  AND vm_static.cluster_id = cluster.cluster_id
  AND cluster.compatibility_version >= '4.8'
  AND vm_dynamic.emulated_machine IS NOT NULL
  AND vm_dynamic.emulated_machine LIKE '%i440fx%';

-- Step 2: Pin custom_emulated_machine on down i440fx VMs
-- For down VMs, extract the i440fx portion from the cluster's emulated_machine.
-- The format is "i440fxType;q35Type" or just "i440fxType" if single. The i440fx type is
-- always the FIRST part (ClusterEmulatedMachines.build writes i440fx first; historic
-- rows produced by the engine follow the same order) — the chipset guard below keeps
-- the pin safe even if a hand-edited row ever deviated from that invariant.
UPDATE vm_static
SET custom_emulated_machine = split_part(cluster.emulated_machine, ';', 1)
FROM cluster
WHERE vm_static.bios_type = 1
  AND (vm_static.custom_emulated_machine IS NULL OR vm_static.custom_emulated_machine = '')
  AND vm_static.cluster_id = cluster.cluster_id
  AND cluster.compatibility_version >= '4.8'
  AND cluster.emulated_machine IS NOT NULL
  AND cluster.emulated_machine LIKE '%i440fx%'
  AND cluster.emulated_machine NOT LIKE ';%'  -- skip if i440fx entry is empty
  AND split_part(cluster.emulated_machine, ';', 1) NOT LIKE '%q35%';  -- chipset guard: pin must be i440fx

-- Step 2b: Pin custom_emulated_machine on i440fx templates
-- Templates are rows in vm_static with entity_type = 'TEMPLATE'.
-- For running VMs that were cloned from templates: the VM gets pinned in Step 1/2 above.
-- The template itself needs pinning so that future VMs created from it inherit the pinned value.
-- Same part-order invariant and chipset guard as Step 2.
UPDATE vm_static
SET custom_emulated_machine = split_part(cluster.emulated_machine, ';', 1)
FROM cluster
WHERE vm_static.entity_type = 'TEMPLATE'
  AND vm_static.bios_type = 1
  AND (vm_static.custom_emulated_machine IS NULL OR vm_static.custom_emulated_machine = '')
  AND vm_static.cluster_id = cluster.cluster_id
  AND cluster.compatibility_version >= '4.8'
  AND cluster.emulated_machine IS NOT NULL
  AND cluster.emulated_machine LIKE '%i440fx%'
  AND cluster.emulated_machine NOT LIKE ';%'  -- skip if i440fx entry is empty
  AND split_part(cluster.emulated_machine, ';', 1) NOT LIKE '%q35%';  -- chipset guard: pin must be i440fx

-- Step 3: Rewrite cluster.emulated_machine to Q35-only
-- Extract Q35 portion from dual-format entries ("i440fxType;q35Type").
-- Chipset guard (mirrors Steps 2/2b, which guard against hand-edited deviant rows):
-- only rewrite when the first part is the i440fx side and the second part is a usable
-- non-i440fx entry. A deviant "q35...;i440fx..." row would otherwise become an
-- i440fx-only default (the exact state this migration is meant to eliminate), and an
-- "i440fx...;" row would become an empty string instead of NULL.
UPDATE cluster
SET emulated_machine = split_part(emulated_machine, ';', 2)
WHERE emulated_machine IS NOT NULL
  AND emulated_machine LIKE '%;%'
  AND compatibility_version >= '4.8'
  AND split_part(emulated_machine, ';', 1) NOT LIKE '%q35%'   -- first part must be i440fx
  AND split_part(emulated_machine, ';', 1) LIKE '%i440fx%'    -- (redundant with NOT q35, kept explicit)
  AND split_part(emulated_machine, ';', 2) <> ''              -- second part must be non-empty
  AND split_part(emulated_machine, ';', 2) NOT LIKE '%i440fx%'; -- second part must be Q35

-- Step 3c: Dual rows that violate the i440fx-first invariant were deliberately left
-- alone by Step 3 (see guard above). Whatever the deviation (Q35-first row, empty
-- second part, i440fx in the second part), do not guess which side to trust: fall
-- back to detection like Step 3b, so no >= 4.8 cluster is left with an i440fx-only
-- or otherwise unusable default; the next UP host sets the value.
UPDATE cluster
SET emulated_machine = NULL,
    detect_emulated_machine = TRUE
WHERE emulated_machine IS NOT NULL
  AND emulated_machine LIKE '%;%'
  AND compatibility_version >= '4.8';

-- Step 3b: Rewrite single-entry i440fx cluster defaults that were never given a Q35 entry.
-- These are clusters whose only host was i440fx-only and therefore never had a semicolon format.
-- After this migration they will have detectEmulatedMachine=true so the next UP host sets the value.
UPDATE cluster
SET emulated_machine = NULL,
    detect_emulated_machine = TRUE
WHERE emulated_machine IS NOT NULL
  AND emulated_machine LIKE '%i440fx%'
  AND emulated_machine NOT LIKE '%;%'
  AND compatibility_version >= '4.8';

-- Step 4: One audit-log summary row per migrated cluster. The engine cannot emit audit
-- events during engine-setup (the service is not running), so the migration writes the
-- row itself — the same pattern as the ENGINE_BACKUP_* events. The row appears in the
-- cluster's Events tab and flows through normal event processing. Pins that no current
-- host supports (e.g. an all-EL10 cluster with a stale pre-refresh default) escalate
-- the row to WARNING severity and are called out in the message text.
INSERT INTO audit_log (log_time, log_type_name, log_type, severity, message, cluster_id, cluster_name)
SELECT now(),
       'CLUSTER_I440FX_SETUP_MIGRATED',
       15023,
       CASE WHEN x.dead_pins > 0 THEN 1 ELSE 0 END,
       'Cluster ' || x.cluster_name || ': engine-setup pinned ' || x.pinned ||
           ' unpinned i440fx VM(s)/template(s); the cluster default emulated machine' ||
           ' is now Q35-only' ||
           CASE WHEN x.dead_pins > 0
                THEN ' (' || x.dead_pins ||
                     ' of them to machine types no current host supports - review those VMs)'
                ELSE '' END,
       x.cluster_id,
       x.cluster_name
FROM (
    SELECT cluster.cluster_id,
           cluster.name AS cluster_name,
           count(*) AS pinned,
           count(*) FILTER (WHERE NOT EXISTS (
               SELECT 1
               FROM vds_static vs
               JOIN vds_dynamic vd ON vd.vds_id = vs.vds_id
               WHERE vs.cluster_id = tmp.cluster_id
                 -- exact comma-token match, mirroring the engine's Java-side check
                 -- (substring LIKE would false-match prefix-sharing type names)
                 AND EXISTS (SELECT 1
                             FROM unnest(string_to_array(vd.supported_emulated_machines, ',')) AS m
                             WHERE trim(m) = tmp.pin))) AS dead_pins
    FROM tmp_i440fx_migration_pinned tmp
    JOIN cluster ON cluster.cluster_id = tmp.cluster_id
    GROUP BY cluster.cluster_id, cluster.name
) x;

DROP TABLE IF EXISTS tmp_i440fx_migration_pinned;
