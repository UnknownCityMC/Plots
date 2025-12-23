alter table plot
    drop foreign key plot_plot_group_group_name_name_fk;

alter table plot
    add constraint plot_plot_group_group_name_name_fk
        foreign key (group_name) references plot_group (name)
            on delete set null;

alter table plot_reset_data
    add constraint plot_reset_data_plot_id_fk
        foreign key (plot_id) references plot (id);