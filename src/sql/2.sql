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
	vars AS (SELECT '(^.*\S)(?: |)([⁰¹²³⁴⁵⁶⁷⁸⁹])()?$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and version is null and line ~ vars.regex )
--update word w set line = m[1], version=m[2] from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

WITH 
	vars AS (SELECT '(^.*\S).(I+)$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and version is null and line ~ vars.regex )
--update word w set line = m[1], version='Ⅰ' from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

--нареч
WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]])([]|),((?:| )нареч(?:)?.)$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and version is null and line ~ vars.regex )
--update word w set line = m[1], pos='наречие' from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as split, m[3] as pos from r order by id;

--сущ
WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]\)])(()?,сущ()?\.)$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and line ~ vars.regex )
--update word w set line = m[1], pos='существительное' from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as split from r order by id;

--прил
WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]\)])(.*,прил()?\.)$'::text AS regex),
	r AS (select id, line, pos, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and pos is null and line ~ vars.regex )
--update word w set line = m[1], pos='прилагательное' from r where w.id=r.id;
select r.id, r.line, r.pos, r.bunch_id, m[1] as line_, m[2] as split from r order by id;

--междометие
WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]\)])(.*,)(()?междом()?\.)$'::text AS regex),
	r AS (select id, line, pos, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and pos is null and line ~ vars.regex )
--update word w set line = m[1], pos='междометие' from r where w.id=r.id;
select r.id, r.line, r.pos, r.bunch_id, m[1] as line_, m[2] as split from r order by id;

-- ), notes
WITH 
	vars AS (SELECT '(^.*\(.+\)),(.*[^\)])$'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and notes is null and line ~ vars.regex )
--update word w set line = m[1], notes=m[2] from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[2] as notes_ from r;

WITH 
	vars AS (SELECT '(.*(.{4,}).*.*)(\[.+\])'::text AS regex),
	r AS (select id, line, notes, bunch_id, regexp_matches(line, vars.regex) as m 
				from vars, word where deprecated is null and notes is null and line ~ vars.regex )
--update word w set line = m[1], notes=m[2] from r where w.id=r.id;
select r.id, r.line, r.notes, r.bunch_id, m[1] as line_, m[3] as notes_ from r;


WITH clw AS (
select 
		(regexp_replace(line, '[´\(\)]+', '\1', 'g')) as cline,
		(regexp_matches(line, '(.*(.{4,}).*.*)(\[.+\])')) m,
		w.id, bunch_id, line, w.derived_id, notes
				from word w where deprecated is null)
--update word w set line = m[1], notes=m[3] from clw where w.id=clw.id
select clw.id id, derived_id, line, m[1] m1, m[3] m3 from clw where true
 and clw.line ~ '(.*(.{4,}).*).*(\[.*\2.*\]).*'
 and cline ~ '.*(.{4,}.*).*\[.*\1.*\].*'
;

WITH clw AS (
select 
		(regexp_matches(line, '.+(\(.+\)).*')) m,
		w.id, bunch_id, line, w.derived_id, notes
				from word w where deprecated is null)
select clw.id id, derived_id, line, m[1] m1, length(m[1]) from clw where true
 and clw.line ~ '.+(\[.+\]).*'
and length(m[1])>13
;


;WITH 
	vars AS (SELECT '(^.+[а-ё´I¹²\]\)])(.*,)(()?междом()?\.)$'::text AS regex)
select id, line, bunch_id,
(regexp_matches(line, vars.regex))[1] as m1,
(regexp_matches(line, vars.regex))[2] as m2,
(regexp_matches(line, vars.regex))[3] as m3

from vars, word
	where deprecated is null and pos is null
--	and not line ~  '(^.+[а-ё´I¹²\]\)])(.*,()?прил()?\.)$'
	and line ~ vars.regex
order by id
;


select page, w.* from word w
join bunch b on b.id = w.bunch_id
where bunch_id in (8353, 8351, 8359, 8301, 17799, 63403) order by bunch_id, y desc;

--select * from bunch where abs(id - 8351) < 10;

select page, min(id) mnid, max(id) mxid,  max(id/page), min(id/page), (max(id/page)) / min(id/page+1) as koeff, max(id/page) - min(id/page) as diff from bunch b
group by page 
having max(id) < 1001001
order by min(id/page) - max(id/page)
;


select * from word where bunch_id = 52471;

--select id, alt_rest from word where line like '===' and alt_rest like '%черед%';
select id, bunch_id, alt_rest
	,(regexp_matches(alt_rest, '(?:черед.|; |, |,|)(?:|)([а-ё’]+)(?:(|)|)(?:–|-)(?:|)([а-ё]+)'))[1] as m1
	,(regexp_matches(alt_rest, '(?:черед.|; |, |,|)(?:|)([а-ё]+)(?:(|)|)(?:–|-)(?:|)([а-ё]+)'))[3] as m2
from word where line like '===' and alt_rest like '%черед%'
and alt_rest ~ '(?:черед.|; |, |,|)(?:|)([а-ё’]+((|)|)(–|-)(|)[а-ё]+)'
--and not alt_rest ~ '^((?:черед.|; |, |,|)(?:|)([а-ё’]+)(?:(|)|)(?:–|-)(?:|)([а-ё]+))+$'
;
