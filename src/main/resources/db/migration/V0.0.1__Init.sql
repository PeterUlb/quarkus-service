CREATE TABLE image
(
    id bigserial NOT NULL,
    externalKey character varying(20) NOT NULL,
    imageStatus character varying(20) NOT NULL DEFAULT 'REQUESTED',
    PRIMARY KEY (id),
    CONSTRAINT uq_external_id UNIQUE (externalKey)
);