create schema util;
set search_path to util;

create or replace function non_empty_trimmed_string(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if p_value is null or trim(p_value) = '' or trim(p_value) != p_value then
      return false;
    else
      return true;
    end if;
  end
$$;

create or replace function null_or_lower_non_empty_trimmed_string(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if p_value is null then
      return true;
    else
      if lower(trim(p_value)) = p_value and p_value != '' then
        return true;
      else
        return false;
      end if;
    end if;
  end
$$;

create or replace function null_or_upper_non_empty_trimmed_string(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if p_value is null then
      return true;
    else
      if upper(trim(p_value)) = p_value and p_value != '' then
        return true;
      else
        return false;
      end if;
    end if;
  end
$$;

create or replace function null_or_non_empty_trimmed_string(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if p_value is null then
      return true;
    else
      if trim(p_value) = p_value and p_value != '' then
        return true;
      else
        return false;
      end if;
    end if;
  end
$$;

create or replace function lower_non_empty_trimmed_string(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if (util.non_empty_trimmed_string(p_value) and lower(p_value) = p_value) then
      return true;
    else
      return false;
    end if;
  end
$$;

create or replace function upper_non_empty_trimmed_string(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if (util.non_empty_trimmed_string(p_value) and upper(p_value) = p_value) then
      return true;
    else
      return false;
    end if;
  end
$$;

create or replace function email_address(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if util.non_empty_trimmed_string(p_value) and p_value ~ '^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}$' then
      return true;
    else
      return false;
    end if;
  end
$$;

create or replace function null_or_email_address(p_value text) returns boolean immutable cost 1 language plpgsql as $$
  begin
    if p_value is null or util.email_address(p_value) then
      return true;
    else
      return false;
    end if;
  end
$$;