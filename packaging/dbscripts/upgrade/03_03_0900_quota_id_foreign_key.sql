-- remove deleted quota from images and also Guid.Empty
UPDATE images
SET    quota_id = NULL
WHERE  quota_id NOT IN (SELECT id FROM quota);
-- remove deleted quota from vm_static
UPDATE vm_static
SET    quota_id = NULL
WHERE  quota_id NOT IN (SELECT id FROM quota);
-- when removing quota, set quota_id to null in images and vm_static
ALTER TABLE images ADD CONSTRAINT fk_images_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;
ALTER TABLE vm_static ADD CONSTRAINT fk_vm_static_quota FOREIGN KEY (quota_id) REFERENCES quota(id) ON DELETE SET NULL;
