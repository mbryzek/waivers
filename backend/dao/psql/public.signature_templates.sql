drop table if exists public.signature_templates;

set search_path to public;

create table signature_templates(
  id                    text primary key check(util.non_empty_trimmed_string(id)),
  project_id            text not null check(util.non_empty_trimmed_string(project_id)),
  provider              text not null check(util.non_empty_trimmed_string(provider)),
  provider_template_id  text not null check(util.non_empty_trimmed_string(provider_template_id)),
  name                  text not null check(util.non_empty_trimmed_string(name)),
  status                text not null check(util.non_empty_trimmed_string(status)),
  created_at            timestamptz not null,
  updated_at            timestamptz not null,
  updated_by_user_id    text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code             bigint not null
);