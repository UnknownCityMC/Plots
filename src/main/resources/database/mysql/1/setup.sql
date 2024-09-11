CREATE TABLE IF NOT EXISTS plots
(
    id              VARCHAR(256) NOT NULL PRIMARY KEY,
    world           VARCHAR(256) NOT NULL,
    owner_id        VARCHAR(36),
    state           ENUM ('SOLD', 'AVAILABLE', 'UNAVAILABLE') DEFAULT 'UNAVAILABLE',
    price           DOUBLE                                    DEFAULT 0.0,
    sell_type       ENUM ('SELL', 'RENT')                     DEFAULT 'SELL',
    rent_interval   LONG                                      DEFAULT 0.0,
    last_rent_payed DATETIME
);

CREATE TABLE IF NOT EXISTS plot_members
(
    plot_id VARCHAR(256) NOT NULL,
    world   VARCHAR(256) NOT NULL,
    id      VARCHAR(36),
    role    ENUM ('CO_OWNER', 'MEMBER', 'TEMP_MEMBER'),

    CONSTRAINT plot_members_id
        PRIMARY KEY (plot_id, world, id),
    FOREIGN KEY (plot_id) REFERENCES plots (id, world)
        ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS plot_flags
(
    id              VARCHAR(256) NOT NULL,
    world           VARCHAR(256) NOT NULL,
    plot_id         VARCHAR(256) NOT NULL,
    access_modifier ENUM ('CO_OWNER', 'MEMBER', 'TEMP_MEMBER', 'ALL', 'OFF'),

    CONSTRAINT plot_flags_id
        PRIMARY KEY (plot_id, world, id),
    FOREIGN KEY (plot_id) REFERENCES plots (id, world)
        ON DELETE CASCADE
);