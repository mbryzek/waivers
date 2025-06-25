alter table projects add constraint slug_lower_slug_ck check (lower(slug) = lower_slug);
