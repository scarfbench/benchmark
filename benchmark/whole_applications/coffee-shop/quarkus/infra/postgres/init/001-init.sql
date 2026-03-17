DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_roles WHERE rolname = 'coffeeshopuser') THEN
    CREATE ROLE coffeeshopuser LOGIN PASSWORD 'redhat-21';
  ELSE
    ALTER ROLE coffeeshopuser WITH LOGIN PASSWORD 'redhat-21';
  END IF;
END$$;

GRANT CONNECT, TEMP, CREATE ON DATABASE coffeeshopdb TO coffeeshopuser;

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'coffeeshop') THEN
    EXECUTE 'CREATE SCHEMA coffeeshop AUTHORIZATION coffeeshopuser';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'barista') THEN
    EXECUTE 'CREATE SCHEMA barista AUTHORIZATION coffeeshopuser';
  END IF;
  IF NOT EXISTS (SELECT 1 FROM pg_namespace WHERE nspname = 'kitchen') THEN
    EXECUTE 'CREATE SCHEMA kitchen AUTHORIZATION coffeeshopuser';
  END IF;
END$$;

ALTER SCHEMA coffeeshop OWNER TO coffeeshopuser;
ALTER SCHEMA barista    OWNER TO coffeeshopuser;
ALTER SCHEMA kitchen    OWNER TO coffeeshopuser;
