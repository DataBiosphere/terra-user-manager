-- pathRecurse(target, path, new_value) is a wrapper over jsonb_set which essentially performs:

-- ({"a" : {"b": {"c": null}}}, [a, b, c], "d") ->
--  jsonb_set({"a": ...}, [a], 
--      jsonb_set({"b": ...}, [b],
--          jsonb_set({"c", null}, [c], 
--              "d")))

-- The jsonb_set spec states:
-- "Returns target with the item designated by path replaced by new_value, or with new_value added ...
--  if the item designated by path does not exist. **All earlier steps in the path must exist, 
--  or the target is returned unchanged.**"

-- Thus, the advantage of splitting the path into individual calls is twofold:

-- a) steps in the path *do not* have to exist -- they will be created with each call.
-- b) jsonb_set quietly fails if a path element is not indexable (object or array) -- this
--     function will raise an exception instead.
-- 
-- Because of its recursive nature, it must be a named function, so we define it once on startup
-- rather than inline with the query.
--
CREATE OR REPLACE FUNCTION pathRecurse(jsonb, text[], jsonb) RETURNS jsonb as $do$
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
