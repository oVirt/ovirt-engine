DELETE
FROM event_map
WHERE length(trim(event_down_name)) = 0 OR
      event_down_name = 'UNASSIGNED';

ALTER TABLE event_subscriber DROP CONSTRAINT fk_event_subscriber_event_map;
