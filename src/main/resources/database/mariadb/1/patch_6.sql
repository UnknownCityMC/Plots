alter table plot_home
    drop primary key;

alter table plot_home
    add primary key (plot_id);
