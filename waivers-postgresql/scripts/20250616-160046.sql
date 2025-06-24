-- Add default waiver for demo project
INSERT INTO waivers (
  id, 
  project_id, 
  version, 
  title, 
  content, 
  is_current, 
  created_at, 
  updated_at, 
  updated_by_user_id, 
  hash_code
) VALUES (
  'wvr-demo-default', 
  'prj-38ddb78ef6854f18b3fdb80b868b958f', 
  1, 
  'Demo Project Waiver', 
  'This is the default waiver for the demo project. By signing this waiver, you acknowledge the terms and conditions and agree to participate at your own risk.', 
  true, 
  now(), 
  now(), 
  'system', 
  1234567890
);