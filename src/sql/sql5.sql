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

--(ср. )
WITH 
	vars AS (SELECT '(.+)?(?:| )\((ср(?:)?\.(?: |)[^)(]+(?:\(а\))?[^)(]*)\)$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and notes is null and line ~ vars.regex )
--update word w set line = '===с', notes=m[2] from r where w.id=r.id and m[1] is null;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

WITH 
	vars AS (SELECT '(.+)?(?:| )\((ср(?:)?\.(?: |)[^)(]+(?:\(а\))?[^)(]*)\)$'::text AS regex)
select id, line, notes, alt_rest, bunch_id, regexp_matches(line, vars.regex) as m from vars, word
	where deprecated is null
	and not line like '(ср%'
--	and not line ~ '(.+)?(?:| )\((ср(?:)?\.(?:)?[^)]{4,35})\)$'
	and line ~ vars.regex;

--[()()]
WITH 
	vars AS (SELECT '^(.+[а-ё])(\[.*\(.+\).+\(.+\).*\])$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and notes is null and line ~ vars.regex )
--update word w set line = m[1], notes=m[2] from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

--*(‘’)
WITH 
	vars AS (SELECT '^(.+[а-ёI¹]?) (\(‘.*’\))$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and notes is null and line ~ vars.regex )
--update word w set line = m[1], notes=m[2] from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

--*[()]
WITH clw AS (
select 
		'([а-ё´]+.*)/([а-ё´]+).*\[.*\1.*\([а-ё´]+\).*\2.*\]'::text AS cregex,
		'(.*/.*).*(\[.*\([а-ё´]+\).*\])'::text AS regex,
		id, line, notes, bunch_id,
		regexp_matches(line, '(.*/.*).*(\[.*\([а-ё´]+\).*\])') m,
		(regexp_replace(line, '([а-ё])[´]+', '\1', 'g')) as cline
				from word where deprecated is null)
--update word w set line = m[1], notes=m[2] from clw where w.id=clw.id;
select id, line, notes, bunch_id, m[1] as line_, m[2] as notes_ from clw;

-- версия
WITH 
	vars AS (SELECT '(^.*\S)(?: |)([⁰¹²³⁴⁵⁶⁷⁸⁹])$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and version is null and line ~ vars.regex )
--update word w set line = m[1], version=m[2] from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

--нареч
WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]])([]|),((?:| )нареч(?:)?.)$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and version is null and line ~ vars.regex )
--update word w set line = m[1], pos='наречие' from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as split, m[3] as pos from r order by id;

WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]])([]|),((?:| )нареч(?:)?.)$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and version is null and line ~ vars.regex )
--update word w set line = m[1], pos='наречие' from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as split, m[3] as pos from r order by id;


;WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]\)])(()?,сущ()?\.)$'::text AS regex)
select id, line, bunch_id,
(regexp_matches(line, vars.regex))[1] as m1,
(regexp_matches(line, vars.regex))[2] as m2,
(regexp_matches(line, vars.regex))[3] as m3

from vars, word
	where deprecated is null
--	and not line ~  '(^.+[а-ё´I¹²\]\)])(()?,сущ()?\.)$'
	and line ~ vars.regex
order by id
;
