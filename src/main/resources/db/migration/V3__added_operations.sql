CREATE TABLE IF NOT EXISTS public.replenishment_operations
(
    id SERIAL,
    user_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL,
    date_of_creation DATE NOT NULL,
    funds DECIMAL NOT NULL,
    currency SMALLINT NOT NULL,
    CONSTRAINT replenishment_operations_pkey PRIMARY KEY (id),
    CONSTRAINT users_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT accounts_fkey FOREIGN KEY (account_id)
        REFERENCES public.accounts (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.withdrawal_operations
(
    id SERIAL,
    user_id INTEGER NOT NULL,
    account_id INTEGER NOT NULL,
    date_of_creation DATE NOT NULL,
    funds DECIMAL NOT NULL,
    currency SMALLINT NOT NULL,
    CONSTRAINT withdrawal_operations_pkey PRIMARY KEY (id),
    CONSTRAINT users_fkey FOREIGN KEY (user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT accounts_fkey FOREIGN KEY (account_id)
        REFERENCES public.accounts (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS public.transfer_operations
(
    id SERIAL,
    from_user_id INTEGER NOT NULL,
    to_user_id INTEGER NOT NULL,
    from_account_id INTEGER NOT NULL,
    to_account_id INTEGER NOT NULL,
    date_of_creation DATE NOT NULL,
    funds DECIMAL NOT NULL,
    from_account_currency SMALLINT NOT NULL,
    CONSTRAINT transfer_operations_pkey PRIMARY KEY (id),
    CONSTRAINT from_users_fkey FOREIGN KEY (from_user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT to_users_fkey FOREIGN KEY (to_user_id)
        REFERENCES public.users (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT from_accounts_fkey FOREIGN KEY (from_account_id)
        REFERENCES public.accounts (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE,
    CONSTRAINT to_accounts_fkey FOREIGN KEY (to_account_id)
        REFERENCES public.accounts (id) MATCH SIMPLE
        ON UPDATE CASCADE
        ON DELETE CASCADE
);

