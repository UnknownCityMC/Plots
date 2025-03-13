CREATE TABLE IF NOT EXISTS plot_group
(
    name VARCHAR(256) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS plot
(
    id         VARCHAR(256) NOT NULL PRIMARY KEY,
    owner_id   UUID,
    region_id  VARCHAR(256) NOT NULL,
    group_name VARCHAR(256),
    world      VARCHAR(256) NOT NULL,
    state          ENUM ('SOLD', 'AVAILABLE', 'UNAVAILABLE') DEFAULT 'UNAVAILABLE',
    payment_type   ENUM ('BUY', 'RENT')                      DEFAULT 'BUY',
    price          DOUBLE                                    DEFAULT 0.0,

    rent_interval  LONG                                      DEFAULT 0,
    last_rent_paid DATETIME,

    CONSTRAINT plot_plot_group_group_name_name_fk
        FOREIGN KEY (group_name) REFERENCES plot_group (name)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_location
(
    plot_id VARCHAR(256) NOT NULL,
    type    VARCHAR(256) NOT NULL,
    x       DOUBLE,
    y       DOUBLE,
    z       DOUBLE,
    yaw     DOUBLE,
    pitch   DOUBLE,
    CONSTRAINT plot_member_pk
        PRIMARY KEY (plot_id, type),
    CONSTRAINT plot_location_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_member
(
    plot_id         VARCHAR(256) NOT NULL,
    user_id         VARCHAR(36)  NOT NULL,
    last_known_name VARCHAR(16)  NOT NULL,
    role            ENUM ('CO_OWNER', 'MEMBER', 'TEMP_MEMBER') DEFAULT 'MEMBER',

    CONSTRAINT plot_member_pk
        PRIMARY KEY (user_id, plot_id),
    CONSTRAINT plot_member_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_flag
(
    action_id       VARCHAR(256) NOT NULL,
    plot_id         VARCHAR(256) NOT NULL,
    access_modifier ENUM ('OWNER', 'CO_OWNER', 'MEMBER', 'TEMP_MEMBER', 'EVERYBODY', 'NOBODY') DEFAULT 'MEMBER',

    CONSTRAINT plot_flag_pk
        PRIMARY KEY (plot_id, action_id),
    CONSTRAINT plot_flag_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);