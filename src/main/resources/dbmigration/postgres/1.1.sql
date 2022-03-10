-- apply changes
alter table url add constraint uq_url_name unique  (name);
