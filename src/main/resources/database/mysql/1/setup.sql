CREATE TABLE IF NOT EXISTS plot
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

CREATE TABLE IF NOT EXISTS plot_member
(
    id      VARCHAR(36),
    plot_id VARCHAR(256) NOT NULL,
    role    ENUM ('CO_OWNER', 'MEMBER', 'TEMP_MEMBER'),

    CONSTRAINT plot_member_pk
        PRIMARY KEY (id, plot_id),
    CONSTRAINT plot_member_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);


CREATE TABLE IF NOT EXISTS plot_flag
(
    id              VARCHAR(256) NOT NULL,
    plot_id         VARCHAR(256) NOT NULL,
    access_modifier ENUM ('CO_OWNER', 'MEMBER', 'TEMP_MEMBER', 'ALL', 'OFF'),

    CONSTRAINT plot_flag_pk
        PRIMARY KEY (plot_id, id),
    CONSTRAINT plot_flag_plot_plot_id_id_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);
