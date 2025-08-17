CREATE TABLE IF NOT EXISTS plot_group
(
    name VARCHAR(256) NOT NULL PRIMARY KEY
);

CREATE TABLE IF NOT EXISTS plot
(
    id             VARCHAR(256) NOT NULL PRIMARY KEY,
    owner_id       UUID,
    region_id      VARCHAR(256) NOT NULL,
    group_name     VARCHAR(256),
    world          VARCHAR(256) NOT NULL,
    state          ENUM ('SOLD', 'AVAILABLE', 'UNAVAILABLE') DEFAULT 'UNAVAILABLE',
    payment_type   ENUM ('BUY', 'RENT')                      DEFAULT 'BUY',
    price          DOUBLE                                    DEFAULT 0.0,

    rent_interval  LONG                                      DEFAULT 0,
    last_rent_paid DATETIME,
    claimed        DATETIME                                  DEFAULT NOW(),

    CONSTRAINT plot_plot_group_group_name_name_fk
        FOREIGN KEY (group_name) REFERENCES plot_group (name)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_location
(
    plot_id VARCHAR(256) NOT NULL,
    name    VARCHAR(256) NOT NULL,
    public  BOOLEAN DEFAULT true,
    x       DOUBLE,
    y       DOUBLE,
    z       DOUBLE,
    yaw     DOUBLE,
    pitch   DOUBLE,
    CONSTRAINT plot_location_pk
        PRIMARY KEY (plot_id, name),
    CONSTRAINT plot_location_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_sign
(
    plot_id VARCHAR(256) NOT NULL,
    id      INTEGER,
    x       DOUBLE,
    y       DOUBLE,
    z       DOUBLE,
    CONSTRAINT plot_sign_pk
        PRIMARY KEY (plot_id, id),
    CONSTRAINT plot_sign_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_member
(
    plot_id VARCHAR(256) NOT NULL,
    user_id VARCHAR(36)  NOT NULL,
    role    ENUM ('CO_OWNER', 'MEMBER', 'TEMP_MEMBER') DEFAULT 'MEMBER',

    CONSTRAINT plot_member_pk
        PRIMARY KEY (user_id, plot_id),
    CONSTRAINT plot_member_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_denied
(
    plot_id VARCHAR(256) NOT NULL,
    user_id VARCHAR(36)  NOT NULL,

    CONSTRAINT plot_member_pk
        PRIMARY KEY (user_id, plot_id),
    CONSTRAINT plot_denied_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_flag
(
    plot_id VARCHAR(256) NOT NULL,
    flag_id VARCHAR(256) NOT NULL,
    value   TEXT         NOT NULL,

    CONSTRAINT plot_flag_pk
        PRIMARY KEY (plot_id, flag_id),
    CONSTRAINT plot_flag_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS plot_interactables
(
    block_type      VARCHAR(256) NOT NULL,
    plot_id         VARCHAR(256) NOT NULL,
    access_modifier ENUM ('OWNER', 'CO_OWNER', 'MEMBER', 'TEMP_MEMBER', 'EVERYBODY', 'NOBODY') DEFAULT 'MEMBER',

    CONSTRAINT plot_flag_pk
        PRIMARY KEY (plot_id, block_type),
    CONSTRAINT plot_interactables_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);