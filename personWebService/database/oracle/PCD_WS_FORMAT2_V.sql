/* Formatted on 2/13/2016 10:54:21 AM (QP5 v5.252.13127.32847) */
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
   CHAR_PENN_ID,
   SCHOOL_OR_CENTER,
   ORG_OR_DIV,
   SEARCH_DESCRIPTION
)
   BEQUEATH DEFINER
AS
   SELECT penn_id,
          kerberos_principal,
          admin_view_pref_first_name,
          admin_view_pref_middle_name,
          admin_view_pref_last_name,
          admin_view_pref_name,
          admin_view_pref_email_address,
          NULL,
          NULL,
          last_updated,
          directory_prim_cent_affil_code,
          char_penn_id,
          school_or_center_or_override,
          org_or_div_or_override,
          search_description
     FROM computed_person
    WHERE     active_code = 'A'
          AND (   is_active_faculty = 'Y'
               OR is_active_staff = 'Y'
               OR is_active_student = 'Y'
               OR directory_prim_cent_affil_id IS NOT NULL);

COMMENT ON TABLE PCD_WS_FORMAT2_V IS 'format2 view';
