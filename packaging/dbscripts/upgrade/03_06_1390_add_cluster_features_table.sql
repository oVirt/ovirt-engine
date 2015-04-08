-- ----------------------------------------------------------------------
--  tables cluster_features, supported_cluster_features and supported_host_features
--  Maintains the list of features which are supported by Engine. This is on top of the
--  standard cluster compatibility version check.
-- ----------------------------------------------------------------------

-- The value of category for a feature decides if that feature is applicable for (GLuster or Virt or Both).
-- The value of category is represented by a unique binary number. Value of category should be a power of 2.
-- Current valid values of category which can be set for a feature are -
-- 1. Virt     0000 0001
-- 2. Gluster  0000 0010
-- 3. All      1111 1111

CREATE TABLE cluster_features
(
  feature_id UUID NOT NULL,
  feature_name VARCHAR(256) NOT NULL,
  version VARCHAR(40),
  category INTEGER NOT NULL,
  description TEXT,
  CONSTRAINT PK_cluster_features PRIMARY KEY (feature_id)
);

CREATE INDEX IDX_cluster_features_version_and_category ON cluster_features(category, version);

CREATE TABLE supported_cluster_features
(
  cluster_id UUID NOT NULL,
  feature_id UUID NOT NULL,
  is_enabled BOOLEAN,
  CONSTRAINT PK_supported_cluster_features PRIMARY KEY (cluster_id, feature_id),
  FOREIGN KEY (cluster_id) REFERENCES vds_groups(vds_group_id) ON DELETE CASCADE,
  FOREIGN KEY (feature_id) REFERENCES cluster_features(feature_id) ON DELETE CASCADE
) ;

CREATE UNIQUE INDEX IDX_supported_cluster_features ON supported_cluster_features(cluster_id, feature_id);

CREATE TABLE supported_host_features
(
  host_id UUID NOT NULL,
  feature_name VARCHAR(256) NOT NULL,
  CONSTRAINT PK_supported_host_features PRIMARY KEY (host_id, feature_name),
  FOREIGN KEY (host_id) REFERENCES vds_static(vds_id) ON DELETE CASCADE
) ;

INSERT INTO cluster_features VALUES ('00000017-0017-0017-0017-000000000066', 'GLUSTER_GEO_REPLICATION', '3.5', 2, 'Gluster Geo-Replication');
INSERT INTO cluster_features VALUES ('00000018-0018-0018-0018-000000000093', 'GLUSTER_SNAPSHOT', '3.5', 2, 'Gluster Volume Snapshot Management');
INSERT INTO cluster_features VALUES ('00000019-0019-0019-0019-000000000300', 'GLUSTER_BRICK_MANAGEMENT', '3.5', 2, 'Gluster Brick Provisioning');
