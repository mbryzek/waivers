drop table if exists public.waivers;

set search_path to public;

create table waivers(
  id                  text primary key check(util.non_empty_trimmed_string(id)),
  project_id          text not null check(util.non_empty_trimmed_string(project_id)),
  version             integer not null,
  title               text not null check(util.non_empty_trimmed_string(title)),
  "content"           text not null check(util.non_empty_trimmed_string("content")),
  status              text not null check(util.non_empty_trimmed_string(status)),
  created_at          timestamptz default now() not null,
  updated_at          timestamptz default now() not null,
  updated_by_user_id  text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code           bigint not null
);

create index waivers_project_id_idx on waivers(project_id);

alter table waivers
  add constraint waivers_project_id_fk
  foreign key(project_id)
  references public.projects;

select schema_evolution_manager.create_updated_at_trigger('public', 'waivers');