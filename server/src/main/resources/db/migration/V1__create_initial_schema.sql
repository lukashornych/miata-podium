CREATE TABLE races (
    id          BIGSERIAL    NOT NULL,
    external_id INTEGER      NOT NULL,
    name        VARCHAR(255) NOT NULL,
    date        DATE         NOT NULL,

    CONSTRAINT pk_races PRIMARY KEY (id),
    CONSTRAINT uq_races_external_id UNIQUE (external_id)
);

CREATE TABLE racers (
    id          BIGSERIAL    NOT NULL,
    external_id INTEGER      NOT NULL,
    car_number  INTEGER      NOT NULL,
    first_name  VARCHAR(255) NOT NULL,
    last_name   VARCHAR(255) NOT NULL,

    CONSTRAINT pk_racers PRIMARY KEY (id),
    CONSTRAINT uq_racers_external_id UNIQUE (external_id)
);

CREATE TABLE laps (
    id               BIGSERIAL    NOT NULL,
    external_id      INTEGER      NOT NULL,
    race_id          BIGINT       NOT NULL,
    racer_id         BIGINT       NOT NULL,
    time             TIMESTAMPTZ  NOT NULL,
    time_prev        TIMESTAMPTZ  NOT NULL,
    lap_time_ms      INTEGER      NOT NULL,
    time_s1          TIMESTAMPTZ  NOT NULL,
    time_s2          TIMESTAMPTZ  NOT NULL,
    time_s3          TIMESTAMPTZ  NOT NULL,
    s1_ms            INTEGER      NOT NULL,
    s2_ms            INTEGER      NOT NULL,
    s3_ms            INTEGER      NOT NULL,
    air_temp         REAL         NOT NULL,
    track_temp       REAL         NOT NULL,
    humidity         REAL         NOT NULL,
    round            INTEGER      NOT NULL,
    car_number       INTEGER      NOT NULL,
    category         VARCHAR(50)  NOT NULL,
    make             VARCHAR(100) NOT NULL,
    model            VARCHAR(100) NOT NULL,
    tag              VARCHAR(255) NOT NULL,
    is_race_lap      BOOLEAN      NOT NULL,

    CONSTRAINT pk_laps PRIMARY KEY (id),
    CONSTRAINT fk_laps_race FOREIGN KEY (race_id) REFERENCES races(id),
    CONSTRAINT fk_laps_racer FOREIGN KEY (racer_id) REFERENCES racers(id),
    CONSTRAINT uq_laps_external_id_race UNIQUE (external_id, race_id)
);

CREATE INDEX idx_laps_race_id ON laps(race_id);
CREATE INDEX idx_laps_racer_id ON laps(racer_id);
