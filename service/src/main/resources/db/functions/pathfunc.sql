DROP FUNCTION IF EXISTS pathRecurse;
CREATE FUNCTION pathRecurse(jsonb, text[], jsonb) RETURNS jsonb as $do$
DECLARE
    node text;
    child jsonb;
BEGIN
    IF array_length($2, 1) IS NULL THEN
        RETURN $3;
    END IF;
    IF jsonb_typeof($1) != 'object' THEN
        RAISE EXCEPTION 'Attempting to index into non-object: %', $1 USING ERRCODE = 'invalid_sql_json_subscript';
    END IF;
    node = $2[1];
    IF ($1 -> node) IS NULL THEN
        child = '{}'::jsonb;
    ELSE
        child = $1 -> node;
    END IF;
    RETURN jsonb_set($1, ARRAY[node], pathRecurse(child, $2[2:], $3));
END;
$do$ LANGUAGE plpgsql;
