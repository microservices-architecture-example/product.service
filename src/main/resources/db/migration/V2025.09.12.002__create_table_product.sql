CREATE TABLE product (
    id VARCHAR(36) NOT NULL,
    name VARCHAR(256) NOT NULL,
    price FLOAT NOT NULL,
    unit VARCHAR(10) NOT NULL,
    CONSTRAINT pk_product PRIMARY KEY (id)
);