rename table plot_location to plot_home;
create table IF NOT EXISTS plot_home_resets
(
    plot_id varchar(256) not null
        primary key,
    x       double       null,
    y       double       null,
    z       double       null,
    pitch   double       null,
    yaw     double       null,
    constraint plot_home_resets_plot_id_fk
        foreign key (plot_id) references plot (id)
            on delete cascade
);

INSERT INTO plot_home_resets (plot_id, x, y, z, pitch, yaw) SELECT plot_id, x, y, z, yaw, pitch from plot_home;