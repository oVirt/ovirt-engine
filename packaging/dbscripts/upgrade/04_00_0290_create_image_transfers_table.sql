-- Add image_transfers table
CREATE TABLE image_transfers
(
    -- identity, command status
    command_id UUID NOT NULL,
    command_type INTEGER NOT NULL,
    phase INTEGER NOT NULL,
    last_updated TIMESTAMP WITH TIME ZONE NOT NULL,
    message VARCHAR,

    -- operation info
    vds_id UUID,
    disk_id UUID,
    imaged_ticket_id UUID,
    proxy_uri VARCHAR,
    signed_ticket VARCHAR,

    -- statistics
    bytes_sent BIGINT,
    bytes_total BIGINT,

    CONSTRAINT pk_image_transfers PRIMARY KEY (command_id)
);

CREATE INDEX idx_image_transfers_disk_id ON image_transfers (disk_id);
