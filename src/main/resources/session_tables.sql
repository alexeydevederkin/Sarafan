CREATE TABLE `SPRING_SESSION` (
    `PRIMARY_ID` char(36) NOT NULL,
    `SESSION_ID` char(36) NOT NULL,
    `CREATION_TIME` bigint(20) NOT NULL,
    `LAST_ACCESS_TIME` bigint(20) NOT NULL,
    `MAX_INACTIVE_INTERVAL` int(11) NOT NULL,
    `EXPIRY_TIME` bigint(20) NOT NULL,
    `PRINCIPAL_NAME` varchar(300) DEFAULT NULL                   -- <= here was 100
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

ALTER TABLE `SPRING_SESSION`
    ADD PRIMARY KEY (`PRIMARY_ID`),
    ADD UNIQUE KEY `SPRING_SESSION_IX1` (`SESSION_ID`),
    ADD KEY `SPRING_SESSION_IX2` (`EXPIRY_TIME`),
    ADD KEY `SPRING_SESSION_IX3` (`PRINCIPAL_NAME`);



CREATE TABLE `SPRING_SESSION_ATTRIBUTES` (
    `SESSION_PRIMARY_ID` char(36) NOT NULL,
    `ATTRIBUTE_NAME` varchar(200) NOT NULL,
    `ATTRIBUTE_BYTES` blob NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 ROW_FORMAT=DYNAMIC;

ALTER TABLE `SPRING_SESSION_ATTRIBUTES`
    ADD PRIMARY KEY (`SESSION_PRIMARY_ID`,`ATTRIBUTE_NAME`);

ALTER TABLE `SPRING_SESSION_ATTRIBUTES`
    ADD CONSTRAINT `SPRING_SESSION_ATTRIBUTES_FK` FOREIGN KEY (`SESSION_PRIMARY_ID`) REFERENCES `SPRING_SESSION` (`PRIMARY_ID`) ON DELETE CASCADE;




/*
CREATE TABLE spring_session (
  primary_id            CHAR(36) NOT NULL
    CONSTRAINT spring_session_pk
    PRIMARY KEY,
  session_id            CHAR(36) NOT NULL,
  creation_time         BIGINT   NOT NULL,
  last_access_time      BIGINT   NOT NULL,
  max_inactive_interval INTEGER  NOT NULL,
  expiry_time           BIGINT   NOT NULL,
  principal_name        VARCHAR(300) -- <= here was 100
);

CREATE UNIQUE INDEX spring_session_ix1
  ON spring_session (session_id);

CREATE INDEX spring_session_ix2
  ON spring_session (expiry_time);

CREATE INDEX spring_session_ix3
  ON spring_session (principal_name);


CREATE TABLE spring_session_attributes (
  session_primary_id CHAR(36)     NOT NULL
    CONSTRAINT spring_session_attributes_fk
    REFERENCES spring_session
    ON DELETE CASCADE,
  attribute_name     VARCHAR(200) NOT NULL,
  attribute_bytes    BYTEA        NOT NULL,
  CONSTRAINT spring_session_attributes_pk
  PRIMARY KEY (session_primary_id, attribute_name)
);*/