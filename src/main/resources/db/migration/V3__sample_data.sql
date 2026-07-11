-- =============================================================================
-- V3__sample_data.sql
-- Forensic Medicine Department — Sample Test Data
-- Covers: 1 clinical case + 4 autopsy cases (all 4 manner-of-death categories)
-- =============================================================================

-- ---------------------------------------------------------------------------
-- Patients
-- ---------------------------------------------------------------------------
INSERT INTO patients (full_name, age, gender, address, nic_passport_no, contact_info) VALUES
    -- Clinical patient
    ('Priya Mendis',          28, 'FEMALE', '14, Galle Road, Colombo 03',          '199805634521', '+94777890001'),
    -- Autopsy patients
    ('Thilak Gunaratne',      72, 'MALE',   '22, Kandy Road, Peradeniya',          '195112345678', NULL),
    ('Sarath Fernando',       35, 'MALE',   '8, Flower Road, Nugegoda',            '199023456789', NULL),
    ('Malini Seneviratne',    45, 'FEMALE', '3, Hospital Lane, Kurunegala',        '197934567890', NULL),
    ('Kasun Wijesinghe',      22, 'MALE',   'Unknown / Unidentified at scene',      NULL,           NULL);

-- ---------------------------------------------------------------------------
-- Cases
-- ---------------------------------------------------------------------------
INSERT INTO cases (case_number, case_type, patient_id, incident_date, referring_authority, referred_by, case_status, assigned_doctor_id) VALUES
    -- Clinical case: assault/trauma
    ('CW/01/24', 'CLINICAL', 1, '2024-03-10', 'Colombo Magistrate Court No. 2',  'COURT',    'REPORT_DRAFTED', 1),
    -- Autopsy: natural death
    ('PM/01/24', 'AUTOPSY',  2, '2024-03-12', 'Peradeniya Teaching Hospital',    'HOSPITAL', 'SUBMITTED',      1),
    -- Autopsy: accidental death
    ('PM/02/24', 'AUTOPSY',  3, '2024-04-05', 'Nugegoda Police Station',         'POLICE',   'IN_PROGRESS',    2),
    -- Autopsy: suicidal death
    ('PM/03/24', 'AUTOPSY',  4, '2024-04-20', 'Kurunegala Magistrate Court',     'COURT',    'REPORT_DRAFTED', 1),
    -- Autopsy: homicidal death
    ('PM/04/24', 'AUTOPSY',  5, '2024-05-03', 'Colombo Crimes Division (CCD)',   'POLICE',   'OPEN',           2);

-- ---------------------------------------------------------------------------
-- MLEF (clinical stream)
-- ---------------------------------------------------------------------------
INSERT INTO mlef (case_id, examining_doctor_id, date_of_issue, examination_date_time,
                  nature_of_bodily_harm, causative_weapon, alcohol_drug_test_results,
                  findings, report_status) VALUES
    (1, 1, '2024-03-11', '2024-03-10 14:30:00',
     'Multiple contusions and lacerations to face and upper limbs',
     'Blunt object (iron rod suspected)',
     'Blood alcohol: 0.00 g/dL; Drug screen: Negative',
     'Patient conscious and oriented. Three lacerations on forehead (3cm, 2cm, 1.5cm). '
     || 'Extensive bruising on both forearms (defensive wounds). Swelling right wrist. '
     || 'X-ray: No fractures. Injuries consistent with blunt force trauma.',
     'ISSUED');

-- ---------------------------------------------------------------------------
-- POSTMORTEM records (autopsy stream — all 4 manner-of-death categories)
-- ---------------------------------------------------------------------------
INSERT INTO postmortem (case_id, doctor_id, inquest_order_ref, inquest_date, place_of_pm,
                        cause_of_death_category, findings, cause_of_death) VALUES
    -- Natural
    (2, 1, 'PH/INQ/2024/0312', '2024-03-13',
     'Forensic Medicine Department Mortuary, Teaching Hospital Peradeniya',
     'NATURAL',
     'Elderly male. Pale, jaundiced. Moderate ascites. Post-mortem lividity consistent with supine position. '
     || 'Internal: Enlarged cirrhotic liver (1850g). Oesophageal varices present. Moderate pulmonary oedema.',
     'Hepatic failure secondary to liver cirrhosis'),

    -- Accidental
    (3, 2, 'NG/INQ/2024/0405', '2024-04-06',
     'Forensic Medicine Department Mortuary, National Hospital Colombo',
     'ACCIDENTAL',
     'Young male. Multiple abrasions and lacerations consistent with road traffic accident. '
     || 'Compound fracture of right femur. Subdural haematoma (right parietal). Haemothorax left side. '
     || 'Road debris embedded in wounds.',
     'Traumatic brain injury and haemothorax following road traffic collision'),

    -- Suicidal
    (4, 1, 'KL/INQ/2024/0420', '2024-04-21',
     'Forensic Medicine Department Mortuary, Kurunegala General Hospital',
     'SUICIDAL',
     'Middle-aged female. Ligature mark around neck (complete, above thyroid cartilage). '
     || 'Petechial haemorrhages in conjunctivae. Congested facial features. '
     || 'Internal: Laryngeal fracture. Haemorrhage into strap muscles.',
     'Asphyxia due to hanging'),

    -- Homicidal
    (5, 2, 'CC/INQ/2024/0503', '2024-05-04',
     'Forensic Medicine Department Mortuary, National Hospital Colombo',
     'HOMICIDAL',
     'Young male, unidentified. Decomposed remains, found in plastic wrapping. '
     || 'Multiple stab wounds to chest (7 in total, 2–5 cm depth). Defensive injuries on hands. '
     || 'Blunt force trauma to posterior skull. Toxicology samples collected.',
     'Pending — multiple stab wounds to chest (preliminary)');

-- ---------------------------------------------------------------------------
-- Evidence
-- ---------------------------------------------------------------------------
INSERT INTO evidence (case_id, evidence_type, description, storage_location, collected_by, collected_at) VALUES
    (1, 'CLOTHING',        'Blood-stained blue shirt worn by victim',          'Evidence Locker A-03', 3, '2024-03-10 15:00:00'),
    (1, 'PHOTOGRAPH',      'Crime scene and injury photographs (12 images)',   'Document Store /CW-01-24/', 3, '2024-03-10 15:30:00'),
    (5, 'BIOLOGICAL',      'Blood swabs from victim (x5) and scene (x3)',      'Cold Storage B-01',    3, '2024-05-04 09:00:00'),
    (5, 'PHYSICAL_OBJECT', 'Plastic wrapping material (evidence bag PM-04-E1)','Evidence Locker C-07', 3, '2024-05-04 09:15:00'),
    (3, 'CLOTHING',        'Torn clothing from accident victim',               'Evidence Locker A-07', 3, '2024-04-05 17:00:00');

-- ---------------------------------------------------------------------------
-- Evidence Custody Log
-- ---------------------------------------------------------------------------
INSERT INTO evidence_custody_log (evidence_id, transferred_from, transferred_to, transfer_timestamp, reason) VALUES
    -- CW/01/24 shirt: police → JMO → Lab
    (1, NULL, 3, '2024-03-10 15:00:00', 'Initial collection at hospital by JMO'),
    (1, 3,    4, '2024-03-11 09:00:00', 'Transferred to laboratory for analysis'),
    -- PM/04/24 blood swabs: police → JMO → Lab
    (3, NULL, 3, '2024-05-04 09:00:00', 'Initial collection at scene by JMO'),
    (3, 3,    4, '2024-05-05 08:30:00', 'Transferred to toxicology laboratory');

-- ---------------------------------------------------------------------------
-- Laboratory Tests
-- ---------------------------------------------------------------------------
INSERT INTO laboratory_tests (case_id, test_type, requested_by, result, result_date) VALUES
    (1, 'BLOOD_ALCOHOL',     1, '0.00 g/dL — No alcohol detected',                            '2024-03-11'),
    (1, 'DRUG_SCREEN',       1, 'Negative for common substances',                              '2024-03-11'),
    (5, 'DNA_PROFILING',     2, 'Pending — samples submitted to State DNA Laboratory',         NULL),
    (5, 'TOXICOLOGY_PANEL',  2, 'Pending',                                                     NULL),
    (3, 'HISTOPATHOLOGY',    2, 'Axonal injury consistent with traumatic brain injury',        '2024-04-10');

-- ---------------------------------------------------------------------------
-- Court Reports
-- ---------------------------------------------------------------------------
INSERT INTO court_reports (case_id, report_type, submission_date, report_status,
                           court_name, date_of_trial, certificate_of_receipt_ref) VALUES
    (1, 'MLR', '2024-03-15', 'ISSUED',
     'Colombo Magistrate Court No. 2', '2024-06-20', 'CMC2/REC/2024/0315'),
    (2, 'PMR', '2024-03-18', 'ISSUED',
     'Peradeniya Magistrate Court',    '2024-07-10', 'PMC/REC/2024/0318'),
    (4, 'PMR', '2024-05-01', 'ISSUED',
     'Kurunegala High Court',          '2024-08-15', 'KHC/REC/2024/0501');

-- ---------------------------------------------------------------------------
-- Notifications
-- ---------------------------------------------------------------------------
INSERT INTO notifications (notification_type, related_case_id, target_user_id, message, notification_status) VALUES
    ('COURT_DATE_UPCOMING', 1, 2,
     'Court date for case CW/01/24 at Colombo Magistrate Court No. 2 is on 2024-06-20 (within 14 days).', 'UNREAD'),
    ('MLEF_PENDING', 1, 1,
     'MLEF for case CW/01/24 has been in DRAFT status for more than 7 days.', 'READ'),
    ('COD_PENDING', 5, 2,
     'Cause of death for autopsy case PM/04/24 has not been finalised. Report remains in DRAFT.', 'UNREAD');
