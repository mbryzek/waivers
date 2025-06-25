drop table if exists public.signature_templates;

set search_path to public;

create table signature_templates(
  id                    text primary key check(util.non_empty_trimmed_string(id)),
  project_id            text not null check(util.non_empty_trimmed_string(project_id)),
  provider              text not null check(util.non_empty_trimmed_string(provider)),
  provider_template_id  text not null check(util.non_empty_trimmed_string(provider_template_id)),
  name                  text not null check(util.non_empty_trimmed_string(name)),
  status                text not null check(util.non_empty_trimmed_string(status)),
  created_at            timestamptz default now() not null,
  updated_at            timestamptz default now() not null,
  updated_by_user_id    text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code             bigint not null
);

create index signature_templates_project_id_idx on signature_templates(project_id);

alter table signature_templates
  add constraint signature_templates_project_id_fk
  foreign key(project_id)
  references public.projects;

select schema_evolution_manager.create_updated_at_trigger('public', 'signature_templates');