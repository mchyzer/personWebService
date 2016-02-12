/* Formatted on 2/12/2016 3:20:14 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW PCD_WS_FORMAT2_V
(
   PENN_ID,
   KERBEROS_PRINCIPAL,
   ADMIN_VIEW_PREF_FIRST_NAME,
   ADMIN_VIEW_PREF_MIDDLE_NAME,
   ADMIN_VIEW_PREF_LAST_NAME,
   ADMIN_VIEW_PREF_NAME,
   ADMIN_VIEW_PREF_EMAIL_ADDRESS,
   BIRTH_DATE,
   GENDER,
   LAST_UPDATED,
   DIRECTORY_PRIM_CENT_AFFIL_CODE,
   CHAR_PENN_ID
)
AS
   SELECT penn_id,
          kerberos_principal,
          admin_view_pref_first_name,
          admin_view_pref_middle_name,
          admin_view_pref_last_name,
          admin_view_pref_name,
          admin_view_pref_email_address,
          birth_date,
          gender,
          last_updated,
          directory_prim_cent_affil_code,
          char_penn_id
     FROM computed_person
    WHERE     active_code = 'A'
          AND (   is_active_faculty = 'Y'
               OR is_active_staff = 'Y'
               OR is_active_student = 'Y'
               OR directory_prim_cent_affil_id IS NOT NULL);

COMMENT ON TABLE PCD_WS_FORMAT2_V IS 'format2 view';
