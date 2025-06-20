
-- Создание таблицы users
CREATE TABLE IF NOT EXISTS users (
                                     id BIGSERIAL PRIMARY KEY,
                                     first_name VARCHAR NOT NULL,
                                     last_name VARCHAR NOT NULL,
                                     email VARCHAR NOT NULL,
                                     password VARCHAR NOT NULL,
                                     role SMALLINT NOT NULL,
                                     bucket_id BIGINT
);

-- Создание таблицы buckets
CREATE TABLE IF NOT EXISTS buckets (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR NOT NULL,
                                       size BIGINT,
                                       user_id BIGINT,
                                       root_folder_id BIGINT
);

-- Добавление внешнего ключа из users в buckets
ALTER TABLE users
    ADD CONSTRAINT fk_users_bucket
        FOREIGN KEY (bucket_id) REFERENCES buckets(id);

-- Добавление внешнего ключа из buckets в users
ALTER TABLE buckets
    ADD CONSTRAINT fk_buckets_user
        FOREIGN KEY (user_id) REFERENCES users(id);

-- Создание таблицы folders
CREATE TABLE IF NOT EXISTS folders (
                                       id BIGSERIAL PRIMARY KEY,
                                       name VARCHAR NOT NULL,
                                       path VARCHAR NOT NULL,
                                       uploaded_at TIMESTAMP NOT NULL,
                                       parent_id BIGINT,
                                       bucket_id BIGINT NOT NULL,

                                       CONSTRAINT fk_folders_parent
                                       FOREIGN KEY (parent_id) REFERENCES folders(id),
    CONSTRAINT fk_folders_bucket
    FOREIGN KEY (bucket_id) REFERENCES buckets(id)
    );

ALTER TABLE buckets
    ADD CONSTRAINT fk_buckets_folder
        FOREIGN KEY (root_folder_id) REFERENCES folders(id);

-- Создание таблицы files
CREATE TABLE IF NOT EXISTS files (
                                     id BIGSERIAL PRIMARY KEY,
                                     file_name VARCHAR NOT NULL,
                                     file_path VARCHAR NOT NULL,
                                     folder_id BIGINT NOT NULL,
                                     uploaded_at TIMESTAMP NOT NULL,
                                     content_type VARCHAR NOT NULL,
                                     file_size BIGINT NOT NULL,

                                     CONSTRAINT fk_files_folder
                                     FOREIGN KEY (folder_id) REFERENCES folders(id)
    );