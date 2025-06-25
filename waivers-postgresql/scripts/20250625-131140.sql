drop table if exists public.signature_requests;

set search_path to public;

create table signature_requests(
  id                     text primary key check(util.non_empty_trimmed_string(id)),
  signature_template_id  text not null check(util.non_empty_trimmed_string(signature_template_id)),
  provider               text not null check(util.non_empty_trimmed_string(provider)),
  provider_request_id    text not null check(util.non_empty_trimmed_string(provider_request_id)),
  signing_url            text check(util.null_or_non_empty_trimmed_string(signing_url)),
  status                 text not null check(util.non_empty_trimmed_string(status)),
  metadata               text check(util.null_or_non_empty_trimmed_string(metadata)),
  created_at             timestamptz not null,
  updated_at             timestamptz not null,
  updated_by_user_id     text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code              bigint not null
);