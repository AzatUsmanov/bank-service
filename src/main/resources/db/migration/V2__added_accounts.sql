CREATE TABLE IF NOT EXISTS public.accounts
(
    id SERIAL,
    user_id INTEGER NOT NULL,
    date_of_creation DATE NOT NULL,
    funds DECIMAL NOT NULL,
    currency SMALLINT NOT NULL,
    CONSTRAINT accounts_pkey PRIMARY KEY (id),
    CONSTRAINT users_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

