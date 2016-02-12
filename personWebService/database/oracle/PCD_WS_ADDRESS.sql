/* Formatted on 2/12/2016 3:19:19 PM (QP5 v5.252.13127.32847) */
CREATE OR REPLACE FORCE VIEW PCD_WS_ADDRESS_V
(
   CHAR_PENN_ID,
   ADDRESS_SEQ,
   STREET1,
   STREET2,
   CITY,
   STATE,
   POSTAL_CODE,
   COUNTRY,
   SOURCE_ADDRESS,
   VIEW_TYPE,
   ADDRESS_TYPE
)
AS
     SELECT ddav.char_penn_id,
            ddav.address_seq,
            ddav.street1,
            ddav.street2,
            ddav.city,
            ddav.state,
            ddav.postal_code,
            ddav.country,
            ddav.source_address,
            ddav.view_type,
            ddav.address_type
       FROM diradmin.dir_detail_address_v ddav, pcd_ws_format2_v pwfv
      WHERE     source_address = 'Registrar'
            AND view_type = 'I'
            AND pref_flag_address = 'Y'
            AND ddav.char_penn_id = pwfv.char_penn_id
   ORDER BY address_seq;

COMMENT ON TABLE PCD_WS_ADDRESS_V IS 'address for people';
