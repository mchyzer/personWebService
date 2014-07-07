  
  
  
CREATE TABLE person_ws_daemon_log
(
  UUID               VARCHAR(40)          NOT NULL comment 'uuid of this record',
  DAEMON_NAME        VARCHAR(40)          NOT NULL comment 'name of the daemon',
  STATUS             VARCHAR(40)          NOT NULL comment 'if success or error',
  THE_TIMESTAMP      BIGINT(20)           NOT NULL comment 'timestamp this record was inserted or updated',
  VERSION_NUMBER     BIGINT(20)                    comment 'for hibernate, though not really used since only inserting',
  STARTED_TIME       VARCHAR(40)                   comment 'string of starting time of daemon',
  ENDED_TIME         VARCHAR(40)                   comment 'string of ended time of daemon',
  MILLIS             BIGINT(20)                    comment 'millis the daemon took to process', 
  RECORDS_PROCESSED  BIGINT(20)                    comment 'number of records processed',
  DELETED_ON         BIGINT(20)                    comment 'millis since 1970 that this row was deleted',
  SERVER_NAME        VARCHAR(100)         NOT NULL comment 'server name the daemon ran on',
  LAST_UPDATED       BIGINT(20)           NOT NULL comment 'millis since 1970 that this record has been updated',
  PROCESS_ID         VARCHAR(100)          comment 'process id of the daemon',
  DETAILS            VARCHAR(100)                  comment 'details of the daemon log'
);

alter table person_ws_daemon_log
  add primary key (uuid);

CREATE INDEX pws_daemon_log_delete_idx ON person_ws_daemon_log
(DELETED_ON);


CREATE INDEX person_ws_daemon_log_name_idx ON person_ws_daemon_log
(DAEMON_NAME, STATUS, ENDED_TIME);


CREATE UNIQUE INDEX person_ws_daemon_log_pk ON person_ws_daemon_log
(UUID);



 