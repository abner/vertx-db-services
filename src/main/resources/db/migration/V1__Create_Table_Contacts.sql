-- Table: public.contacts

-- DROP TABLE public.contacts;

CREATE TABLE IF NOT EXISTS public.contacts
(
  name character varying(255),
  telephone character varying(20),
  email character varying(255),
  uuid uuid NOT NULL,
  CONSTRAINT "PK_contacts" PRIMARY KEY (uuid)
)
WITH (
  OIDS=FALSE
);
ALTER TABLE public.contacts
  OWNER TO contacts;
