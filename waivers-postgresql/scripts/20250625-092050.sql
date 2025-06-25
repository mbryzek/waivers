-- Insert initial Pickleball project with waiver template
set search_path to public;

-- Generate UUIDs for the project and waiver
-- Using format: {3-char-prefix}-{uuid} as per database conventions

INSERT INTO projects (
  id,
  name,
  slug,
  description,
  waiver_template,
  status,
  created_at,
  updated_at,
  updated_by_user_id,
  hash_code
) VALUES (
  'prj-' || replace(gen_random_uuid()::text, '-', ''),
  'Lake View Summit Pickleball',
  'pickleball',
  'Digital waiver system for Lake View Summit Pickleball participants. Participants must sign liability waivers before playing.',
  '# Waiver, Release of Claims, Covenant Not to Sue, and Indemnity Agreement

In consideration of being allowed to participate in programs and activities and to use the courts, spectator areas and public areas of **Lake View Summit Pickleball** (collectively, the "Facility"), the undersigned Participant (hereafter "Participant") and Participant''s parent(s) or legal guardian(s) agree to the following **Waiver, Release of Claims, Covenant Not to Sue, and Indemnity Agreement** (hereafter the "Waiver, Release and Agreement"). This Waiver, Release and Agreement will apply from the date hereof and to each time that the Participant participates in activities at the Facility.

It is the intent of the undersigned Participant and Participant''s parents or legal guardians and Participant''s permitted invitees to release: **Lake View Summit Pickleball** (collectively, "Bryzek"), past or present coaches and Participants participating in the Facility''s events, and all Bryzek''s direct and indirect owners, members, partners, officers, directors, employees, agents, affiliates, and (hereafter "Bryzek Parties"), both as organizations and each person individually from any claims or liability to the fullest extent possible under the law, and to advance that intent the undersigned hereby agrees as follows:

---

### 1. Assumption of Risk and Medical Acknowledgment

The Participant and his/her parent(s) or legal guardian(s) acknowledge that the Facility''s activity may involve strenuous and hazardous physical activities and that participation involves certain inherent risks, including, without limitation:

* Risk of serious bodily injury
* Permanent disability
* Death
* Property damage

The Participant and his/her parent(s) or legal guardian(s):

* Acknowledge understanding and expressly assume all such inherent risks.
* Certify that the Participant has no physical limitations preventing participation and is not participating against medical advice.
* Agree to notify Bryzek Parties immediately if observing any hazard or condition that jeopardizes safety.
* Grant permission for Bryzek Parties to provide emergency medical treatment if needed.
* Acknowledge that Bryzek does not guarantee or sponsor any medical services and is not liable for the adequacy or continuation of such services.
* Understand that Bryzek Parties are not liable for any services provided in connection with Facility programs, including coaching, counseling, transportation, or security.

---

### 2. Indemnity and Release of Claims

The Participant and his/her parent(s) or legal guardian(s):

* Indemnify, hold harmless, and release the Bryzek Parties from any and all liability for claims, demands, losses, damages, and costs (including reasonable attorneys'' fees) arising out of:

  * Personal injury
  * Sickness
  * Accidents
  * Delays
  * Property damage
  * Other loss or expenses of any kind

* This includes losses relating to equipment supplied or used at the Facility and any caused by the negligence of Bryzek Parties.

* Exclusion: This release does **not** apply to gross negligence or willful misconduct by Bryzek Parties.

* Accept responsibility for all medical expenses related to illness or injury in connection with the Facility.

* Understand this waiver applies to **both known and unknown claims**.

---

### 3. Facility Use Terms

* Participant may only use the Facility at times specified by Bryzek.
* No other person may access or use the Facility without Bryzek''s prior consent.
* Participant and his/her parent(s) or legal guardian(s) may **not** copy or transfer any means of access (e.g., key, passcode).
* Bryzek Parties may terminate Facility access at any time for any reason, with no right to compensation or damages.

---

### 4. (Intentionally omitted)

---

### 5. Severability

If any portion of this Waiver, Release and Agreement is deemed invalid or unenforceable, the remaining portions shall remain in full force and effect.

---

### 6. Governing Law and Jurisdiction

* This agreement is governed by the laws of the State of **Pennsylvania**.
* Any legal action must be brought exclusively in the state or federal courts of Pennsylvania.
* If the Participant and/or parent(s)/guardian(s) sue and lose, they are responsible for Bryzek Parties'' legal fees and costs.

---

### 7. Electronic Signatures

* This Waiver, Release and Agreement may be executed by manual or electronic signature.
* Electronic signatures have the same force and effect as manual signatures under applicable law, including:

  * The Electronic Signatures in Global and National Commerce Act of 2000 (15 U.S.C. §§ 7001 to 7031)
  * The Pennsylvania Uniform Electronic Transactions Act (UETA)

---

### Acknowledgment

As the Participant or Participant''s parent/guardian, I acknowledge:

* I have read this waiver form.
* I allow my Participant to participate in the activity at the Facility.
* I have explained all risks to the Participant.
* Both I and the Participant understand the seriousness of the risks and our personal responsibility for adhering to rules and regulations.',
  'active',
  now(),
  now(),
  'sys-migration',
  extract(epoch from now())::bigint
);

-- Create the initial waiver version for the project
INSERT INTO waivers (
  id,
  project_id,
  version,
  title,
  content,
  status,
  created_at,
  updated_at,
  updated_by_user_id,
  hash_code
) VALUES (
  'wvr-' || replace(gen_random_uuid()::text, '-', ''),
  (SELECT id FROM projects WHERE slug = 'pickleball'),
  1,
  'Pickleball Liability Waiver and Release',
  '# Waiver, Release of Claims, Covenant Not to Sue, and Indemnity Agreement

In consideration of being allowed to participate in programs and activities and to use the courts, spectator areas and public areas of **Lake View Summit Pickleball** (collectively, the "Facility"), the undersigned Participant (hereafter "Participant") and Participant''s parent(s) or legal guardian(s) agree to the following **Waiver, Release of Claims, Covenant Not to Sue, and Indemnity Agreement** (hereafter the "Waiver, Release and Agreement"). This Waiver, Release and Agreement will apply from the date hereof and to each time that the Participant participates in activities at the Facility.

It is the intent of the undersigned Participant and Participant''s parents or legal guardians and Participant''s permitted invitees to release: **Lake View Summit Pickleball** (collectively, "Bryzek"), past or present coaches and Participants participating in the Facility''s events, and all Bryzek''s direct and indirect owners, members, partners, officers, directors, employees, agents, affiliates, and (hereafter "Bryzek Parties"), both as organizations and each person individually from any claims or liability to the fullest extent possible under the law, and to advance that intent the undersigned hereby agrees as follows:

---

### 1. Assumption of Risk and Medical Acknowledgment

The Participant and his/her parent(s) or legal guardian(s) acknowledge that the Facility''s activity may involve strenuous and hazardous physical activities and that participation involves certain inherent risks, including, without limitation:

* Risk of serious bodily injury
* Permanent disability
* Death
* Property damage

The Participant and his/her parent(s) or legal guardian(s):

* Acknowledge understanding and expressly assume all such inherent risks.
* Certify that the Participant has no physical limitations preventing participation and is not participating against medical advice.
* Agree to notify Bryzek Parties immediately if observing any hazard or condition that jeopardizes safety.
* Grant permission for Bryzek Parties to provide emergency medical treatment if needed.
* Acknowledge that Bryzek does not guarantee or sponsor any medical services and is not liable for the adequacy or continuation of such services.
* Understand that Bryzek Parties are not liable for any services provided in connection with Facility programs, including coaching, counseling, transportation, or security.

---

### 2. Indemnity and Release of Claims

The Participant and his/her parent(s) or legal guardian(s):

* Indemnify, hold harmless, and release the Bryzek Parties from any and all liability for claims, demands, losses, damages, and costs (including reasonable attorneys'' fees) arising out of:

  * Personal injury
  * Sickness
  * Accidents
  * Delays
  * Property damage
  * Other loss or expenses of any kind

* This includes losses relating to equipment supplied or used at the Facility and any caused by the negligence of Bryzek Parties.

* Exclusion: This release does **not** apply to gross negligence or willful misconduct by Bryzek Parties.

* Accept responsibility for all medical expenses related to illness or injury in connection with the Facility.

* Understand this waiver applies to **both known and unknown claims**.

---

### 3. Facility Use Terms

* Participant may only use the Facility at times specified by Bryzek.
* No other person may access or use the Facility without Bryzek''s prior consent.
* Participant and his/her parent(s) or legal guardian(s) may **not** copy or transfer any means of access (e.g., key, passcode).
* Bryzek Parties may terminate Facility access at any time for any reason, with no right to compensation or damages.

---

### 4. (Intentionally omitted)

---

### 5. Severability

If any portion of this Waiver, Release and Agreement is deemed invalid or unenforceable, the remaining portions shall remain in full force and effect.

---

### 6. Governing Law and Jurisdiction

* This agreement is governed by the laws of the State of **Pennsylvania**.
* Any legal action must be brought exclusively in the state or federal courts of Pennsylvania.
* If the Participant and/or parent(s)/guardian(s) sue and lose, they are responsible for Bryzek Parties'' legal fees and costs.

---

### 7. Electronic Signatures

* This Waiver, Release and Agreement may be executed by manual or electronic signature.
* Electronic signatures have the same force and effect as manual signatures under applicable law, including:

  * The Electronic Signatures in Global and National Commerce Act of 2000 (15 U.S.C. §§ 7001 to 7031)
  * The Pennsylvania Uniform Electronic Transactions Act (UETA)

---

### Acknowledgment

As the Participant or Participant''s parent/guardian, I acknowledge:

* I have read this waiver form.
* I allow my Participant to participate in the activity at the Facility.
* I have explained all risks to the Participant.
* Both I and the Participant understand the seriousness of the risks and our personal responsibility for adhering to rules and regulations.',
  'active',
  now(),
  now(),
  'sys-migration',
  extract(epoch from now())::bigint
);