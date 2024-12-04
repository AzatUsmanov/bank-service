CREATE TABLE IF NOT EXISTS public.authorities
(
    id SERIAL,
    user_id INTEGER NOT NULL,
    authority SMALLINT NOT NULL,
    CONSTRAINT authorities_pkey PRIMARY KEY (id),
    CONSTRAINT users_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);