drop table if exists public.projects;

set search_path to public;

create table projects(
  id                  text primary key check(util.non_empty_trimmed_string(id)),
  name                text not null check(util.non_empty_trimmed_string(name)),
  slug                text not null check(util.non_empty_trimmed_string(slug)),
  lower_slug          text not null check(util.non_empty_trimmed_string(lower_slug)),
  status              text not null check(util.non_empty_trimmed_string(status)),
  created_at          timestamptz default now() not null,
  updated_at          timestamptz default now() not null,
  updated_by_user_id  text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code           bigint not null,
  unique(lower_slug)
);

select schema_evolution_manager.create_updated_at_trigger('public', 'projects');