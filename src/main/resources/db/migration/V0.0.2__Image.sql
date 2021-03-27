CREATE TABLE image
(
    id          bigserial                NOT NULL PRIMARY KEY,
    externalKey character varying(20)    NOT NULL,
    accountId   bigint                   NOT NULL,
    title       text,
    description text,
    fileName    character varying(255)   NOT NULL,
    extension   character varying(10),
    mimeType    character varying(50),
    size        bigint,
    width       bigint,
    height      bigint,
    imageStatus character varying(20)    NOT NULL DEFAULT 'REQUESTED',
    createdAt   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    updatedAt   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT now(),
    CONSTRAINT uq_external_id UNIQUE (externalKey)
);

CREATE TRIGGER set_update_timestamp
    BEFORE UPDATE
    ON image
    FOR EACH ROW
EXECUTE PROCEDURE set_update_timestamp();

CREATE TABLE imageTag
(
    id  bigserial                     NOT NULL PRIMARY KEY,
    tag character varying(255) UNIQUE NOT NULL
);

CREATE TABLE image_imageTag
(
    image_id     BIGINT NOT NULL,
    image_tag_id BIGINT NOT NULL,
    PRIMARY KEY (image_id, image_tag_id),
    CONSTRAINT image_image_tag_fk1 FOREIGN KEY (image_id) REFERENCES image (id),
    CONSTRAINT image_image_tag_fk2 FOREIGN KEY (image_tag_id) REFERENCES imageTag (id)
)