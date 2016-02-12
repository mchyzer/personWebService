  
  
CREATE TABLE person_ws_daemon_log
(
  UUID               VARCHAR2(40 CHAR)          NOT NULL,
  DAEMON_NAME        VARCHAR2(40 CHAR)          NOT NULL,
  STATUS             VARCHAR2(40 CHAR)          NOT NULL,
  THE_TIMESTAMP      INTEGER                    NOT NULL,
  VERSION_NUMBER     INTEGER,
  STARTED_TIME       VARCHAR2(40 CHAR),
  ENDED_TIME         VARCHAR2(40 CHAR),
  MILLIS             INTEGER,
  RECORDS_PROCESSED  INTEGER,
  DELETED_ON         INTEGER,
  SERVER_NAME        VARCHAR2(100 CHAR)          NOT NULL,
  LAST_UPDATED       INTEGER                     NOT NULL,
  PROCESS_ID         VARCHAR2(100 CHAR)          ,
  DETAILS            VARCHAR2(2000 CHAR)
);

COMMENT ON TABLE person_ws_daemon_log IS 'log of daemon processing in person web service';

COMMENT ON COLUMN person_ws_daemon_log.UUID IS 'uuid of this record';

COMMENT ON COLUMN person_ws_daemon_log.DAEMON_NAME IS 'name of the daemon';

COMMENT ON COLUMN person_ws_daemon_log.STATUS IS 'if success or error';

COMMENT ON COLUMN person_ws_daemon_log.THE_TIMESTAMP IS 'timestamp this record was inserted or updated';

COMMENT ON COLUMN person_ws_daemon_log.VERSION_NUMBER IS 'for hibernate, though not really used since only inserting';

COMMENT ON COLUMN person_ws_daemon_log.STARTED_TIME IS 'string of starting time of daemon';

COMMENT ON COLUMN person_ws_daemon_log.ENDED_TIME IS 'string of ended time of daemon';

COMMENT ON COLUMN person_ws_daemon_log.MILLIS IS 'millis the daemon took to process';

COMMENT ON COLUMN person_ws_daemon_log.RECORDS_PROCESSED IS 'number of records processed';

COMMENT ON COLUMN person_ws_daemon_log.DELETED_ON IS 'millis since 1970 that this row was deleted';

COMMENT ON COLUMN person_ws_daemon_log.SERVER_NAME IS 'server name the daemon ran on';

COMMENT ON COLUMN person_ws_daemon_log.PROCESS_ID IS 'process id of the daemon';

COMMENT ON COLUMN person_ws_daemon_log.LAST_UPDATED IS 'millis since 1970 that this record has been updated';

COMMENT ON COLUMN person_ws_daemon_log.DETAILS IS 'details of the daemon log';

CREATE INDEX pws_daemon_log_DELETE_IDX ON person_ws_daemon_log
(DELETED_ON);


CREATE INDEX person_ws_daemon_log_NAME_IDX ON person_ws_daemon_log
(DAEMON_NAME, STATUS, ENDED_TIME);


CREATE UNIQUE INDEX person_ws_daemon_log_PK ON person_ws_daemon_log
(UUID);


ALTER TABLE person_ws_daemon_log ADD (
  CONSTRAINT person_ws_daemon_log_PK
  PRIMARY KEY
  (UUID)
  USING INDEX person_ws_daemon_log_PK);

  
  
  
 