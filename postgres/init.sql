CREATE TABLE texts (
    id SERIAL PRIMARY KEY,
    body TEXT NOT NULL
);

INSERT INTO texts 
    (body) 
VALUES 
    ('Hello'),
    ('World'),
    ('Foo'),
    ('Bar');

begin;

create or replace function tg_notify_texts()
    returns trigger
    language plpgsql
as $$
declare
    channel text := TG_ARGV[0];
begin
    PERFORM (
                WITH payload(id, body) as (SELECT NEW.*)
                select pg_notify(channel, row_to_json(payload)::text)
                from payload
            );
    RETURN NULL;
end;
$$;

CREATE TRIGGER notify_texts
    AFTER INSERT
    ON texts
    FOR EACH ROW
EXECUTE PROCEDURE tg_notify_texts('texts');

commit;;