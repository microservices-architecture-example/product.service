CREATE TABLE product
(
    id varchar(36) NOT NULL,
    name varchar(255) NOT NULL,
    price decimal(10,2) NOT NULL,
    unit varchar(50) NOT NULL,
    CONSTRAINT product_pkey PRIMARY KEY (id)
)