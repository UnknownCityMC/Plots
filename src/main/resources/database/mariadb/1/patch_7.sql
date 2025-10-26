alter table plot_reset_data
    drop foreign key plot_reset_data_plot_id_fk;
alter table plot_reset_data
    add constraint plot_reset_data_plot_id_fk
        foreign key (plot_id) references plot (id)
            on delete cascade;