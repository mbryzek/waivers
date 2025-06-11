drop table if exists public.users;

set search_path to public;

create table users(
  id                  text primary key check(util.non_empty_trimmed_string(id)),
  email               text not null check(util.non_empty_trimmed_string(email)),
  lower_email         text not null check(util.non_empty_trimmed_string(lower_email)),
  first_name          text not null check(util.non_empty_trimmed_string(first_name)),
  last_name           text not null check(util.non_empty_trimmed_string(last_name)),
  phone               text check(util.null_or_non_empty_trimmed_string(phone)),
  created_at          timestamptz default now() not null,
  updated_at          timestamptz default now() not null,
  updated_by_user_id  text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code           bigint not null
);

select schema_evolution_manager.create_updated_at_trigger('public', 'users');