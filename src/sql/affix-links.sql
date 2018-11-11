with
  aa as (
	select count(*) qty, word,
		array_agg(id order by offs) aa, array_agg(affix order by offs) a,
		array_agg(offs order by offs) starts, array_agg(offs+len order by offs) ends
	from affix_appl aa group by word
)
--	insert into composed_affix_appl
select word, aa[1], aa[2], aa[3], a[1], a[2], a[3],
		ARRAY(select numrange((1::smallint||ends)[i], starts[i]) from generate_series(1, 1+array_length(starts,1)) g(i)) val_locs,
		ARRAY(select numrange(starts[i], ends[i]) from generate_subscripts(starts,1) g(i)) aff_locs
from aa
;

select count(*) qty, word, array_agg(aa.id) aa, array_agg(aa.affix) a, a.kind, count (a.kind) kqty
from affix_appl aa
join affix a on a.id = aa.affix
group by word, kind
having count (a.kind) > 2-- and kind like 'infix '
;

select id, * from affix_appl where word in (4658);

select * from affix where id in (11, 3);

CREATE SEQUENCE composed_affix_id_seq;
create type affices as (affix1 integer, affix2 integer, affix3 integer);

create table composed_affix as 
	select nextval('composed_affix_id_seq'::regclass)::integer id,
		(affs).affix1,
		(affs).affix2,
		(affs).affix3,
		qty
		from (select (affix1, affix2, affix3)::affices as affs, count(word) as qty from composed_affix_appl group by affs) as caas
	order by qty desc
;

select word word, line, array[affix1, affix2, affix3] affices, array[affappl1, affappl2, affappl3] affix_applications, val_locs
	,word_region(w.line, val_locs[1]) as val1
	,word_region(w.line, val_locs[2]) as val2
	,word_region(w.line, val_locs[3]) as val3
	,word_region(w.line, val_locs[4]) as val4
from composed_affix_appl caa
join word w on w.id = caa.word
;
/**/
CREATE OR REPLACE FUNCTION word_region(line varchar, region numrange)
RETURNS text AS $$
    select case 
		when upper_inf(region) then substr(line, lower(region)::integer)
		else substr(line, lower(region)::integer, (upper(region) - lower(region))::integer)
	end
$$ LANGUAGE sql STABLE;

CREATE OR REPLACE FUNCTION array_map(inarr ANYARRAY, lambda regprocedure, line varchar)
RETURNS text[] AS $$
declare
	callstr text;
	subres text[];
begin
--	RAISE NOTICE 'Params: lambda=%, inarr=%, line=%', get_funcname(lambda), inarr, quote_nullable(line);
	callstr := 'select array_agg((subsel).lit) from (select ' || get_funcname(lambda) || '(x, $1) as lit FROM unnest($2) x) as subsel';
	execute callstr into subres using line, inarr;
--	RAISE NOTICE 'Result: %', subres;
	return subres;
end;
$$ LANGUAGE plpgsql STABLE;

select word word, line, array[affix1, affix2, affix3] affices, array[affappl1, affappl2, affappl3] affix_applications, val_locs,
	array_map(val_locs, 'word_range1(numrange, varchar)'::regprocedure, w.line) vals
from composed_affix_appl caa
join word w on w.id = caa.word
;
