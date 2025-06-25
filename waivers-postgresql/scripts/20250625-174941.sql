drop table if exists public.signatures;

set search_path to public;

create table signatures(
  id                     text primary key check(util.non_empty_trimmed_string(id)),
  user_id                text not null check(util.non_empty_trimmed_string(user_id)),
  waiver_id              text not null check(util.non_empty_trimmed_string(waiver_id)),
  signature_template_id  text check(util.null_or_non_empty_trimmed_string(signature_template_id)),
  signature_request_id   text check(util.null_or_non_empty_trimmed_string(signature_request_id)),
  status                 text not null check(util.non_empty_trimmed_string(status)),
  signed_at              timestamptz,
  pdf_url                text check(util.null_or_non_empty_trimmed_string(pdf_url)),
  ip_address             text check(util.null_or_non_empty_trimmed_string(ip_address)),
  created_at             timestamptz default now() not null,
  updated_at             timestamptz default now() not null,
  updated_by_user_id     text not null check(util.non_empty_trimmed_string(updated_by_user_id)),
  hash_code              bigint not null
);

create index signatures_user_id_idx on signatures(user_id);

create index signatures_waiver_id_idx on signatures(waiver_id);

alter table signatures
  add constraint signatures_waiver_id_fk
  foreign key(waiver_id)
  references public.waivers;

select schema_evolution_manager.create_updated_at_trigger('public', 'signatures');