CREATE TABLE plot_sign_backup AS SELECT * FROM plot_sign;

CREATE TABLE plot_sign_new
(
    id BIGINT NOT NULL AUTO_INCREMENT,
    plot_id   VARCHAR(256) NOT NULL,
    x         DOUBLE,
    y         DOUBLE,
    z         DOUBLE,
    CONSTRAINT plot_sign_new_pk
        PRIMARY KEY (id),
    CONSTRAINT plot_sign_new_plot_fk
        FOREIGN KEY (plot_id) REFERENCES plot (id)
            ON DELETE CASCADE
);

INSERT INTO plot_sign_new (plot_id, x, y, z)
SELECT plot_id, x, y, z
FROM plot_sign;

DROP TABLE plot_sign;

ALTER TABLE plot_sign_new RENAME TO plot_sign;
