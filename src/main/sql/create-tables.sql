-- Table: public.contacts

-- DROP TABLE public.contacts;

CREATE TABLE public.contacts
(
  name character varying(255),
  telephone character varying(20),
  email character varying(255),
  uuid uuid NOT NULL DEFAULT uuid_generate_v4(),
  CONSTRAINT "PK_contacts" PRIMARY KEY (uuid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.contacts
  OWNER TO postgres;
