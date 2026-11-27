ALTER TABLE notification
DROP CONSTRAINT notification_type_check;

ALTER TABLE notification
    ADD CONSTRAINT notification_type_check
        CHECK (
            type IN (
                     'MEMBER_REMOVED',
                     'MEMBER_LEFT',
                     'TEAM_INVITATION',
                     'TEAM_JOIN_REQUEST'
                )
            );