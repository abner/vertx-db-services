CREATE OR REPLACE FUNCTION public.remove_from_jsonb_array(
    pJsonbArray jsonb,
    pSelector jsonb)
  RETURNS jsonb AS
$BODY$

SELECT COALESCE(jsonb_agg(el), '[]'::jsonb) FROM (
  SELECT jsonb_array_elements(pJsonbArray) el
) elements
WHERE not el @> pSelector;

$BODY$
LANGUAGE sql VOLATILE
COST 100;

COMMENT ON FUNCTION public.remove_from_jsonb_array(jsonb, jsonb) IS 'This function allows to remove elements from jsonb array
USAGE:
SELECT remove_from_json_array(''[{"name":"A"}, {"name":"B"}]''::jsonb, ''{"name":"B"}''::jsonb)
';


CREATE OR REPLACE FUNCTION public.add_to_jsonb_array(
    pJsonbArray jsonb,
    pItem jsonb)
  RETURNS jsonb AS
$BODY$

SELECT COALESCE(pJsonbArray, '[]'::jsonb) || pItem
$BODY$
LANGUAGE sql VOLATILE
COST 100;

COMMENT ON FUNCTION public.add_to_jsonb_array(jsonb, jsonb) IS 'This function allows to add elements to jsonb array
USAGE:
SELECT add_to_jsonb_array(''[{"name":"A"}, {"name":"B"}]''::jsonb, ''{"name":"C"}''::jsonb)
';



CREATE OR REPLACE FUNCTION public.update_in_jsonb_array(
    pJsonbArray jsonb,
    pItem jsonb,
    pSelector jsonb) 
  RETURNS jsonb AS
$BODY$

SELECT CASE
	 WHEN (SELECT COUNT(1) FROM (SELECT jsonb_array_elements(pJsonbArray) as el) ELEMENTS where ELEMENTS.el @> pSelector) > 0
	    THEN remove_from_jsonb_array(pJsonbArray, pSelector) || pItem
	   ELSE pJsonbArray
END	

$BODY$
LANGUAGE sql VOLATILE
COST 100;

COMMENT ON FUNCTION public.update_in_jsonb_array(jsonb, jsonb, jsonb) IS 'This function allows to update an element inside a jsonb array
USAGE:
SELECT update_in_jsonb_array(''[{"name":"A"}, {"name":"B"}]''::jsonb, ''{"name":"D"}''::jsonb, ''{"name":"B"}''::jsonb)
';  
