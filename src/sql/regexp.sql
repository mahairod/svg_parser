--[][]
WITH 
	vars AS (SELECT '(.*([а-я]{1,}).*\[.+\].*)(\[.*\2.*)'::text AS regex),
	parts AS (select id, line, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where notes is null)
--update word w set notes = parts.m[3], line=parts.m[1] from parts where w.id=parts.id;
select parts.id, parts.line, parts.bunch_id, parts.m[1] as line_, parts.m[3] as notes_ from parts;

--[]()
WITH 
	vars AS (SELECT '(?e)(.*\[.*\].*)( |)(\(.*черед\.[а-ё–]{4,6}.*\))'::text AS regex),
	parts AS (select id, line, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and line ~ vars.regex )
select parts.id, parts.line, parts.bunch_id, parts.m[1] as line_, parts.m[3] as alters_ from parts;

--()()
WITH 
	vars AS (SELECT '(?e)(.*\(.*\).*)( ?|)\((.*черед\.[а-ё–]{3,6}.*)\)'::text AS regex),
	parts AS (select id, line, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and line ~ vars.regex )
--update word w set alt_rest = parts.m[3], line=parts.m[1] from parts where w.id=parts.id;
select parts.id, parts.line, parts.bunch_id, parts.m[1] as line_, parts.m[3] as alters_ from parts;

--*()
WITH 
	vars AS (SELECT '(?e)(.*)?( |)\((черед\.[а-ё–’]{3,6}.*)\)'::text AS regex),
	parts AS (select id, line, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and alt_rest is null and line ~ vars.regex )
--update word w set alt_rest = m[3], line='===' from parts where w.id=parts.id and m[1] is null;
select parts.id, parts.line, parts.bunch_id, parts.m[1] as line_, parts.m[3] as alters_ from parts;

WITH 
	vars AS (SELECT '(?: ?|)\((черед\.(?:	|)?[а-ё–’	]{3,6}.*)\)'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(notes, vars.regex) as m 
				from vars, word where deprecated is null and alt_rest is not null and notes ~ vars.regex )
--update word w set alt_rest = m[1], notes=null from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as notes_ from r;

WITH 
	vars AS (SELECT '(?: ?|)\((черед\.(?:	|)?[а-ё–’	]{3,6}.*)\)'::text AS regex)
select id, line, notes, alt_rest, bunch_id, regexp_matches(notes, vars.regex) as m from vars, word
	where deprecated is null
	and notes ~ vars.regex;
