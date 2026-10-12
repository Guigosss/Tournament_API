-- Roles

INSERT INTO role_ (name) VALUES ('user');
INSERT INTO role_ (name) VALUES ('admin');

-- Users
INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('admin','admin@test.be','$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'admin'),CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('guillaume', 'guillaume@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('valentin', 'valentin@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('lionel', 'lionel@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('seb', 'seb@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('anthony', 'anthony@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('thibault', 'thibault@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('romain', 'romain@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('elodie', 'elodie@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('rocio', 'rocio@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('jerome', 'jerome@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('younes', 'younes@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('julien', 'julien@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO user_ (username, email, password, role_id, created_at, updated_at)
VALUES ('tristan', 'tristan@test.be', '$2a$10$T.hS506DjX00K6WKB.FzhO8r1Gs1p0Rt5LcrVH5SSkdK.waPLjFl6',
        (SELECT id FROM role_ WHERE name = 'user'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Teams
-- Team 1
INSERT INTO team (name, team_size, number_of_wins, captain_id, created_at, updated_at)
VALUES ('Team Guillaume - Valentin',2,0,
           (SELECT id FROM user_ WHERE username = 'guillaume'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO team_member (team_id, user_id)
SELECT
    (SELECT id FROM team WHERE name = 'Team Guillaume - Valentin'),
    id
FROM user_
WHERE username IN ('guillaume', 'valentin');

-- Team 2
INSERT INTO team (name, team_size, number_of_wins, captain_id, created_at, updated_at)
VALUES ('Team Lionel - Seb',2,0,
           (SELECT id FROM user_ WHERE username = 'lionel'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO team_member (team_id, user_id)
SELECT
    (SELECT id FROM team WHERE name = 'Team Lionel - Seb'),
    id
FROM user_
WHERE username IN ('lionel', 'seb');

-- Team 3
INSERT INTO team (name, team_size, number_of_wins, captain_id, created_at, updated_at)
VALUES ('Team Anthony - Thibault',2,0,
           (SELECT id FROM user_ WHERE username = 'anthony'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO team_member (team_id, user_id)
SELECT
    (SELECT id FROM team WHERE name = 'Team Anthony - Thibault'),
    id
FROM user_
WHERE username IN ('anthony', 'thibault');

-- Team 4
INSERT INTO team (name, team_size, number_of_wins, captain_id, created_at, updated_at)
VALUES ('Team Romain - Elodie',2,0,
           (SELECT id FROM user_ WHERE username = 'romain'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO team_member (team_id, user_id)
SELECT
    (SELECT id FROM team WHERE name = 'Team Romain - Elodie'),
    id
FROM user_
WHERE username IN ('romain', 'elodie');

-- Team 5
INSERT INTO team (name, team_size, number_of_wins, captain_id, created_at, updated_at)
VALUES ('Team Rocio - Jerome',2,0,
           (SELECT id FROM user_ WHERE username = 'rocio'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO team_member (team_id, user_id)
SELECT
    (SELECT id FROM team WHERE name = 'Team Rocio - Jerome'),
    id
FROM user_
WHERE username IN ('rocio', 'jerome');

-- Team 6
INSERT INTO team (name, team_size, number_of_wins, captain_id, created_at, updated_at)
VALUES ('Team Younes - Julien',2,0,
           (SELECT id FROM user_ WHERE username = 'younes'), CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO team_member (team_id, user_id)
SELECT
    (SELECT id FROM team WHERE name = 'Team Younes - Julien'),
    id
FROM user_
WHERE username IN ('younes', 'julien');

-- Tournaments
INSERT INTO tournament (name, description, max_participants, format, status, start_date, end_date, registration_start_date, registration_end_date, organizer_id, participant_type, created_at, updated_at)
VALUES ('Tournament Test User','Tournament generated for testing',128,'SINGLE_ELIMINATION','REGISTRATION_CLOSED',CURRENT_DATE,CURRENT_DATE + INTERVAL '2 months',CURRENT_DATE,CURRENT_DATE,
        (SELECT id FROM user_ WHERE username = 'admin'),'PLAYER', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

INSERT INTO tournament (name, description, max_participants, format, status, start_date, end_date, registration_start_date, registration_end_date, organizer_id, participant_type, created_at, updated_at)
VALUES ('Tournament Test Team','Tournament generated for testing',128,'SINGLE_ELIMINATION','REGISTRATION_CLOSED',CURRENT_DATE,CURRENT_DATE + INTERVAL '2 months',CURRENT_DATE,CURRENT_DATE,
        (SELECT id FROM user_ WHERE username = 'admin'),'TEAM', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Participants
INSERT INTO participant (tournament_id, user_id)
SELECT
    (SELECT id FROM tournament WHERE name = 'Tournament Test User'),
    id
FROM user_
WHERE username IN (
                    'guillaume',
                    'valentin',
                    'lionel',
                    'seb',
                    'anthony',
                    'thibault',
                    'romain',
                    'elodie',
                    'rocio',
                    'jerome',
                    'younes',
                    'julien',
                    'tristan'
    );

INSERT INTO participant (tournament_id, team_id)
SELECT
    (SELECT id FROM tournament WHERE name = 'Tournament Test Team'),
    id
FROM team
WHERE name IN (
               'Team Guillaume - Valentin',
               'Team Lionel - Seb',
               'Team Anthony - Thibault',
               'Team Romain - Elodie',
               'Team Rocio - Jerome',
               'Team Younes - Julien'
    );