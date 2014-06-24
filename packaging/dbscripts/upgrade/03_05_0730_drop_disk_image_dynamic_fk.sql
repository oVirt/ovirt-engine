ALTER TABLE disk_image_dynamic DROP CONSTRAINT fk_disk_image_dynamic_images;


CREATE OR REPLACE FUNCTION fn_image_deleted() RETURNS TRIGGER AS $$
BEGIN
    DELETE FROM disk_image_dynamic dim WHERE DIM.image_id = OLD.image_guid;
    RETURN OLD;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER delete_disk_image_dynamic_for_image BEFORE DELETE ON IMAGES FOR EACH ROW
EXECUTE PROCEDURE fn_image_deleted()
