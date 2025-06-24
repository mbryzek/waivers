-- Test data for development

insert into projects (id, name, slug, description, waiver_template, updated_by_user_id, hash_code) values
  ('prj-1', 'Test Project', 'test-project', 'A test project for development', 'This is a sample waiver template for the test project. By signing this waiver, you agree to the terms and conditions.', 'system', 1),
  ('prj-2', 'Demo Project', 'demo-project', 'A demo project for testing', 'Demo waiver template content goes here. This is just for testing purposes.', 'system', 2);

insert into waivers (id, project_id, version, title, content, is_current, updated_by_user_id, hash_code) values
  ('wvr-1', 'prj-1', 1, 'Test Project Waiver v1', 'This is the first version of the test project waiver. Please read carefully before signing.', true, 'system', 1),
  ('wvr-2', 'prj-2', 1, 'Demo Project Waiver v1', 'Demo waiver content for the demo project. This is version 1.', true, 'system', 2);

insert into users (id, email, lower_email, first_name, last_name, phone, updated_by_user_id, hash_code) values
  ('usr-1', 'john.doe@example.com', 'john.doe@example.com', 'John', 'Doe', '555-1234', 'system', 1),
  ('usr-2', 'jane.smith@example.com', 'jane.smith@example.com', 'Jane', 'Smith', '555-5678', 'system', 2);

insert into signatures (id, user_id, waiver_id, status, ip_address, updated_by_user_id, hash_code) values
  ('sig-1', 'usr-1', 'wvr-1', 'pending', '127.0.0.1', 'system', 1),
  ('sig-2', 'usr-2', 'wvr-2', 'signed', '192.168.1.1', 'system', 2);