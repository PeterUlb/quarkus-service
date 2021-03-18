SET timezone TO 'UTC';

-- Set update timestamp
CREATE OR REPLACE FUNCTION set_update_timestamp()
    RETURNS TRIGGER AS
$$
BEGIN
    NEW.updatedAt = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;