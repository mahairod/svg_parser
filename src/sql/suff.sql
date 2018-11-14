WITH 
--	l as (select '[^\u001d\u001f[|]+'::text as nb, '|\(|\['::text as br),
	v AS (SELECT '\u001b[\u001e\u001c]((?:[^\u001d\u001f[|]+|\(|\[)+)(?:\u001b[\u001d\u001f]|$)'::text AS bl),
--	vars AS (SELECT bl||'-' AS regex from v)
	vars AS (SELECT bl||'-'||'|'||'-'||bl AS regex from v)
select word.id, line, 
(regexp_matches(line, regex, 'g'))[2] pref,
(regexp_matches(line, regex, 'g'))[1] suff,
regexp_replace((regexp_matches(line, regex, 'g'))[1], '[´\u001b-\u001f([]', '', 'g') m,
position((regexp_matches(line, regex, 'g'))[1] in line) offs,
	bunch_id bunch from word, vars, v where deprecated is null 
--and not (line ~ ('-'||regex) or line ~ (regex||'-'))
--and (line ~ ('\|'||regex) or line ~ (regex||'\|'))
and (line ~ (regex))
--and not (line ~ ('-' || regex))
--and not (line ~ ('^' || regex))
order by id, bunch asc;

WITH 
	l as (select '[^\u001d\u001f[|]+'::text as nb, '|\(|\['::text as br),
	vars AS (SELECT '\u001b[\u001e\u001c](('||nb||br||')+)(\u001b[\u001d\u001f]|$)'::text AS regex from l)
select min(line),
regexp_replace((regexp_matches(line, regex))[1], '[´\u001b-\u001f([]', '', 'g') m,
	count(bunch_id) cnt, min (bunch_id) bunch from word, vars where deprecated is null and line ~ regex
group by m
order by cnt desc;

WITH 
	l as (select '[^\u001d\u001f[|]+'::text as nb, '|\(|\['::text as br),
	vars AS (SELECT '\u001b[\u001e\u001c](('||nb||br||')+)(\u001b[\u001d\u001f]|$)'::text AS regex from l),
	r as (select 
		regexp_replace((regexp_matches(line, regex))[1], '[´\u001b-\u001f([]', '', 'g') m,
	count(bunch_id) cnt from word, vars where deprecated is null and line ~ regex
	group by m order by cnt desc
) insert into affix (value, qty) select m, cnt from r
;


WITH 
	l as (select '[^\u001d\u001f[|]+'::text as nb, '|\(|\['::text as br),
	vars AS (SELECT '(^.*?\u001b[\u001e\u001c])(('||nb||br||')+)(\u001b[\u001d\u001f].*?)?$'::text AS regex from l),
	r as (
select line, id,
		regexp_replace((regexp_matches(line, regex))[2], '[´\u001b-\u001f([-]', '', 'g') m,
	  ((regexp_matches(line, regex))[2]) orig,
length((regexp_matches(line, regex))[2]) len,
length((regexp_matches(line, regex))[1]) offs,
	bunch_id bunch from word, vars where deprecated is null and line ~ regex
--) insert into affix_appl select offs, len, orig, a.id as aff, r.id as word from r join affix a on a.value like r.m
) select offs, len, orig, a.id as aff, r.id as word from r join affix a on a.value like r.m;
;

--with r as (
with t as (select 3 as d, '-'::text as tv)
select b.page, b.id bunch, w.line, right(line, -offs+d),
(alternation like '%-'||orig or alternation like orig||'-%') as alters,
--left(right(line, -offs+d), d) = tv as format,
w.alternation,
aa.* from t, affix_appl aa
	join word w on aa.word = w.id
	join bunch b on b.id = w.bunch_id
where not aa.altern
and offs > d
and (alternation like '%-'||regexp_replace(orig, '´', ''))
and left(right(line, -offs+1), 1) = ''
and left(right(line, -offs+d), d) != tv
--) update affix_appl aa set altern = true from r where r.id = aa.id;
;

--with dr as (
select a.id as aff, a."value", a.qty, count (distinct aa.id) l_qty, qty - count (distinct aa.id) as diff from affix_appl aa
join affix a on a.id = aa.affix
join affix_appl aa2 on a.id = aa2.affix
	where aa2.altern -- removed ones
	and aa.altern
group by a.id
having count (distinct aa.id) = qty

--) update affix af set dropped=true from dr where af.id = dr.aff
;

select * from affix_appl where affix in (111);

select "value" from affix where not dropped order by "value";

select * from word where id = 49301;

WITH 
	l as (select '[^\u001d\u001f[|]+'::text as nb, '|\(|\['::text as br),
	vars AS (SELECT '\u001b[\u001e\u001c](('||nb||br||')+)(\u001b[\u001d\u001f]|$)'::text AS regex from l)
select 
	(regexp_matches(line, regex, 'g'))[1] m,
	(bunch_id) cnt, (bunch_id) bunch from word, vars where line ~ regex
and id = 66
order by cnt asc;

----------------------------------------------------------------------------------------------------------
-- 1c B I	// fs
-- 1d I		// gs
-- 1e B		// rs
-- 1f		// us

--32301 // bl
--'\u001b[\u001e\u001c]((?:[^\u001d\u001f[|]+|([(\][]|[(´]\[))+)(?:\u001b[\u001d\u001f]|$)'
--31265 // regex-all
--('-'||bl||'-')  ||'|'||  (bl||'-')  ||'|'|| ('-'||bl)
--31258 // regex-total-pref-lims
--('[-\|]\[?'||bl||'-')  ||'|'||  ('^'||bl||'-')  ||'|'|| ('-'||bl)
--21295 // bl-pref
--6758 // bl-pref at start
--11629 // bl-suff 

WITH 
	v AS (SELECT '\u001b[\u001e\u001c]((?:[^\u001d\u001f[|]+|([(\][]|[(´]\[))+)(?:\u001b[\u001d\u001f]|$)'::text AS bl),
	vars AS (SELECT bl||'-' AS regex from v)
--	vars AS (SELECT ('[-\|]\[?'||bl||'-'||)  '|'  (||'^'||bl||'-'||)  '|' (||'-'||bl) AS regex, bl from v)
,sel as (
select w.id word, line, page, bunch_id, alt_rest,
--(regexp_matches(line, '[-\|]\[?'||regex))[1] fm,
(regexp_matches(line, regex))[1] inf,
	0 as z from vars, word w join bunch b on w.bunch_id=b.id where deprecated is null-- and not b.root
--and not (line ~ ('[-\|]\[?'||regex))
and not (line ~ ('[-\|][[(]?'||regex))
and not (line ~ ('^'||regex))
and (line ~ (regex))
--and (line ~ (bl))
--) select count(word) tot, count(inf) infs from sel
) select *, length(inf) from sel where length(inf)>1
;

WITH 
	v AS (SELECT '\u001b[\u001e\u001c]((?:[^\u001d\u001f[|]+|(?:[(\][]|[(´]\[))+)(?:\u001b[\u001d\u001f]|$)'::text AS bl),
	vars AS (SELECT bl, ('[-\|][[(]?'||bl||'-')  ||'|'||  ('^'||bl||'-')  ||'|'|| ('-'||bl) AS regex from v)
,sel as (
--	vars AS (SELECT (bl||'-')  AS regex, bl from v)
select w.id word, line, page, bunch_id,
--unnest(regexp_matches(line, regex)) m,
regexp_matches(line, regex) m,
(regexp_matches(line, regex))[1] inf,
(regexp_matches(line, regex))[2] pref,
(regexp_matches(line, regex))[3] suff,
	0 as z from vars, word w join bunch b on w.bunch_id=b.id where deprecated is null-- and not b.root
and (line ~ (regex))
) select array_search(false, ARRAY[m[1] is null, m[2] is null, m[3] is null]), m from sel
;

WITH 
	v AS (SELECT '\u001b[\u001e\u001c]((?:[^\u001d\u001f[|]+|(?:[(\][]|[(´]\[))+)(?:\u001b[\u001d\u001f]|\||$)'::text AS bl),
	vars AS (SELECT bl, '(' || ('[-\|][[(]?'||bl||'-(?!\u001b[\u001e\u001c])')  ||'|'||  ('^'||bl||'-')  ||'|'|| ('-'||bl) || ')' AS regex from v),
	mr as (select regexp_matches(line, regex, 'g') as m, w.* from vars, word w
		left join affix_appl aa on aa.word=w.id  where deprecated is null and aa.id is null
		and line ~ regex),
	msel as ( select
			position(m[1] in line) goffs,
			array_search(false, array_nulls(m), 2) typeind, mr.*
		from mr
	)
--	select * from msel/* group by id/**/;
--	insert into affix_appl (offs, len, orig, affix, word, parent_word)
select 
	position(m[typeind] in substr(line, goffs)) + goffs-1 offs,
	length(m[typeind]), m[typeind],
	a.id as aff, msel.id as word, 0
 from msel join affix a on
		a.value like regexp_replace(m[typeind], '[´\u001b-\u001f([\]]', '', 'g')
		and a.kind = (ARRAY['infix','prefix','suffix'])[typeind-1]
;

WITH 
	v AS (SELECT '\u001b[\u001e\u001c]((?:[^\u001d\u001f[|]+|(?:[(\][]|[(´]\[))+)(?:\u001b[\u001d\u001f]|$)'::text AS bl),
	vars AS (SELECT bl, '(' || ('[-\|][[(]?'||bl||'-(?!\u001b[\u001e\u001c])')  ||'|'||  ('^'||bl||'-')  ||'|'|| ('-'||bl) || ')' AS regex from v),
	mr as (select regexp_matches(line, regex, 'g') as m, word.* from word, vars where deprecated is null and line ~ regex),
	msel as ( select
			array_search(false, array_nulls(m), 2) typeind, mr.*
		from mr
		left join affix_appl aa on aa.word=mr.id  where aa.id is null
	),
	sel as (
		select
			regexp_replace(m[typeind], '[´\u001b-\u001f([\]]', '', 'g') mc,-- m[typeind] orig,
			msel.*
		--	position(m[typeind] in substr(line, goffs)) + goffs-1 offs,
		--	length(m[typeind]), line
		from msel
	)
--	select * from msel;
--	insert into affix a (value, qty, kind) 
select mc, count(id) qty, (ARRAY['infix','prefix','suffix'])[typeind-1] kind from sel
where not exists(select from affix a where a.value=mc and a.kind=kind)
group by typeind, mc order by qty desc
;

select kind, sum(a.qty) from affix a group by a.kind;


CREATE FUNCTION array_search(needle ANYELEMENT, haystack ANYARRAY)
RETURNS INT AS $$
    SELECT i
      FROM generate_subscripts($2, 1) AS i
     WHERE $2[i] = $1
  ORDER BY i
$$ LANGUAGE sql STABLE;

CREATE FUNCTION array_search(needle ANYELEMENT, haystack ANYARRAY, startpos integer)
RETURNS INT AS $$
    SELECT i
      FROM generate_subscripts($2, 1) AS i
     WHERE $2[i] = $1 and i >= startpos
  ORDER BY i
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION array_nulls(haystack ANYARRAY)
RETURNS boolean[] AS $$
    SELECT array (select x is null FROM unnest($1) x);
$$ LANGUAGE sql STABLE;
