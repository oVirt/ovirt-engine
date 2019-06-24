


----------------------------------------------------------------
-- [event_notification_hist] Table
--
CREATE OR REPLACE FUNCTION insertevent_notification_hist (
    v_audit_log_id BIGINT,
    v_event_name VARCHAR(100),
    v_method_type CHAR(10),
    v_reason CHAR(255),
    v_sent_at TIMESTAMP WITH TIME ZONE,
    v_status BOOLEAN
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    INSERT INTO event_notification_hist (
        audit_log_id,
        event_name,
        method_type,
        reason,
        sent_at,
        status
        )
    VALUES (
        v_audit_log_id,
        v_event_name,
        v_method_type,
        v_reason,
        v_sent_at,
        v_status
        );
END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [event_subscriber] Table
--
CREATE OR REPLACE FUNCTION Insertevent_subscriber (
    v_event_up_name VARCHAR(100),
    v_notification_method VARCHAR(32),
    v_method_address VARCHAR(255),
    v_subscriber_id UUID,
    v_tag_name VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    IF NOT EXISTS (
            SELECT *
            FROM event_subscriber
            WHERE subscriber_id = v_subscriber_id
                AND event_up_name = v_event_up_name
                AND notification_method = v_notification_method
                AND tag_name = v_tag_name
            ) THEN
        INSERT INTO event_subscriber (
            event_up_name,
            notification_method,
            method_address,
            subscriber_id,
            tag_name
            )
        VALUES (
            v_event_up_name,
            v_notification_method,
            v_method_address,
            v_subscriber_id,
            v_tag_name
            );
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getevent_subscriberBysubscriber_id (v_subscriber_id UUID)
RETURNS SETOF event_subscriber STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM event_subscriber
    WHERE subscriber_id = v_subscriber_id;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Getevent_subscription (v_subscriber_id UUID, v_event_up_name VARCHAR(100))
RETURNS SETOF event_subscriber STABLE AS $PROCEDURE$
BEGIN
    RETURN QUERY

    SELECT *
    FROM event_subscriber
    WHERE subscriber_id = v_subscriber_id
        AND event_up_name = v_event_up_name;
END;$PROCEDURE$
LANGUAGE plpgsql;

CREATE OR REPLACE FUNCTION Deleteevent_subscriber (
    v_event_up_name VARCHAR(100),
    v_notification_method VARCHAR(32),
    v_subscriber_id UUID,
    v_tag_name VARCHAR(50)
    )
RETURNS VOID AS $PROCEDURE$
BEGIN
    IF (v_tag_name IS NULL) THEN
        DELETE
        FROM event_subscriber
        WHERE event_up_name = v_event_up_name
            AND notification_method = v_notification_method
            AND subscriber_id = v_subscriber_id;
    ELSE
        DELETE
        FROM event_subscriber
        WHERE event_up_name = v_event_up_name
            AND notification_method = v_notification_method
            AND subscriber_id = v_subscriber_id
            AND tag_name = v_tag_name;
    END IF;

END;$PROCEDURE$
LANGUAGE plpgsql;

----------------------------------------------------------------
-- [dbo].[event_notification_hist] Table
----------------------------------------------------------------
CREATE OR REPLACE FUNCTION Deleteevent_notification_hist (v_sent_at TIMESTAMP)
RETURNS VOID AS $PROCEDURE$
BEGIN
    DELETE
    FROM event_notification_hist
    WHERE sent_at < v_sent_at;
END;$PROCEDURE$
LANGUAGE plpgsql;


