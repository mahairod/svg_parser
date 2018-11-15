CREATE OR REPLACE FUNCTION try_alters(orig text)
RETURNS text AS $$
declare
	subres text;
	conson text;
begin
	conson = '([^яиеюёйаыэуо?])';
	subres := orig;
	subres := regexp_replace(subres, 'ой', 'о[яиеюёй]', 'g');
	RAISE NOTICE 'Subresult-1--: %', subres;
	subres := regexp_replace(subres, conson||'о'||conson, '\1о?\2');
	RAISE NOTICE 'Subresult-21--: %', subres;
	subres := regexp_replace(subres, conson||'о'||conson, '\1о?\2');
	RAISE NOTICE 'Subresult-22--: %', subres;
	subres := regexp_replace(subres, 'зд', 'з[дж]', 'g');
	subres := regexp_replace(subres, 'ст', '(д|ст)', 'g');
	subres := regexp_replace(subres, 'о', '[оа]', 'g');
	subres := regexp_replace(subres, 'г', '[гж]', 'g');
	subres := regexp_replace(subres, 'ю', '[ею]', 'g');
	subres := regexp_replace(subres, conson||'ь'||conson, '\1[ье]\2', 'g');
	subres := regexp_replace(subres, '^(с|от|об)', '\1о?', 'g');
	RAISE NOTICE 'Subresult-3--: %', subres;
	return subres;
end;
$$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION apply_alters(orig text, alt1 text[], alt2 text[])
RETURNS text AS $$
declare
	mark text;
	subres text;
begin
	RAISE NOTICE 'Params: alt1=%, alt2=%, line=%', alt1, alt2, orig;
	mark := '';
	subres := regexp_replace(orig, mark||'ё', mark||'о', 'g');
	subres := regexp_replace(subres, mark||'([а-ё])'||alt1[1], mark||'\1'||alt1[2], 'g');
	subres := regexp_replace(subres, mark||alt1[1], alt1[2]||mark, 'g');
	if alt2 is not null then
		RAISE NOTICE 'Subresult---: %', subres;
		subres := regexp_replace(subres, mark||alt2[1], alt2[2], 'g');
	end if;
	RAISE NOTICE 'Result: %', subres;
	return subres;
end;
$$ LANGUAGE plpgsql STABLE;

CREATE OR REPLACE FUNCTION apply_alters(orig text, alt11 text, alt12 text, alt21 text, alt22 text)
RETURNS text AS $$
declare
	mark text;
	subres text;
begin
	if alt21 is null then
		subres := apply_alters(orig, array[alt11, alt12], null);
	else
		subres := apply_alters(orig, array[alt11, alt12], array[alt21, alt22]);
	end if;
	return subres;
end;
$$ LANGUAGE plpgsql STABLE;

---------- exact match ------------------------

with arrs as (
select w.id word, pw.id pid,
	a.id aaid, a.affix aid,
	w.line origin, pw.line parent,
	regexp_replace(w.line, '[´\u001b-\u001f[\]()]', '', 'g') cln,
	regexp_replace(pw.line, '[´\u001b-\u001f[\]()]', '', 'g') pcln,
	'[|-]'::text as ext_patt
from bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
left join affix_appl a on w.id = a.word
where /*a.affix = 656 and */w.id in (
	select bwp.id from composed_affix_appl caa
	join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
	left join composed_affix_appl pcaa on pcaa.word = bwp.id
		where true 
		and caa.parent is null
--		and pcaa.word is null
--		and bwp.line like '%/%'
	group by bwp.id
)
order by cln

), rows1 as (
select regexp_replace(pcln, ext_patt, '', 'g') pcline,
	regexp_replace(cln, ext_patt||'|ь$', '', 'g') cline,
	regexp_matches(origin, '(.*[а-ё])((\u001b\u001f)?\(.*\))?') parts,
	*  from arrs
), rows2 as (
select  regexp_matches(pcline, cline||'.*'), parts[1] base, * from rows1
), rows3 as (
select array[numrange(1, 1+length(base))] locs, * from rows2
)
select parts[1] base, * from rows3;
--insert into composed_affix_appl
--select word, aaid,null,null, aid,null,null, locs, locs  from rows3;
--insert into affix_appl (offs, len, orig, affix, word, parent_word)
--select 1, length(base) orl, base orig, 657 aff, word, pid from rows3 r;
;

with arrs as (
select w.id word, pw.id pid,
	a.id aaid, a.affix aid,
	w.line origin, pw.line parent,
	regexp_replace(w.line, '[´\u001b-\u001f[\]()]', '', 'g') cln,
	regexp_replace(pw.line, '[´\u001b-\u001f[\]()]', '', 'g') pcln,
	regexp_matches(w.line, '(.*[а-ё])((\u001b\u001f)?\(.+\))?') parts,
	'[|-]'::text as ext_patt
from bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
left join affix_appl a on w.id = a.word
where w.id in (
	select bw.id from composed_affix_appl caa
	right join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
		where true 
		and caa.id is null
		and bw.line not like '%/%'
		and not bw.line ~ '.*[а-ё´]{2,}-[а-ё]{2,}.*'
--		and not bw.line ~ '.*[а-ё´][а-ё]-[а-ё]{2,}.*'
		and not bw.line ~ '.*[а-ё]-[а-ё].*'
	group by bw.id
)
order by cln

), rows1 as (
select parts[1] base,
	regexp_replace(pcln, ext_patt, '', 'g') pcline,
	regexp_replace(cln, ext_patt||'|ь$', '', 'g') cline,
	*  from arrs
), rows3 as (
select  regexp_matches(pcline, cline||'.*'),
	array[numrange(1, 1+length(base))] locs,
	* from rows1
	where pcline ~ (cline||'.*')
)
--select * from rows3;
, ins as (
  insert into affix_appl (offs, len, orig, affix, word, parent_word)
    select 1, length(base) orl, base orig, 656 aff, word, pid from rows3 r
  returning *
)
insert into composed_affix_appl (word, affappl1, affix1, val_locs, aff_locs)
  select ins.word, ins.id, ins.affix, rows3.locs, rows3.locs from ins join rows3 on ins.word = rows3.word
;


select * from affix where qty < 0;

---------- precise match ------------------------

with const as (select '[´\u001b\u001c-\u001f[\]()]'::text clean),
	arrs as (
select w.id word, pw.id pid,
	a.id aaid, a.affix aid,
	w.line origin, pw.line parent,
	regexp_replace(w.line, clean, '', 'g') cln,
	regexp_replace(pw.line, clean, '', 'g') pcln,
	regexp_matches(w.line, '(.*)((\u001b\u001f)?\(.*\))?') parts,
	'[|-]'::text as ext_patt, clean
from const, bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
left join affix_appl a on w.id = a.word
where /*a.affix = 656 and*/ w.id in (
	select bwp.id from composed_affix_appl caa
	join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
	left join composed_affix_appl pcaa on pcaa.word = bwp.id
		where true 
		and caa.parent is null
		and pcaa.word is null
	group by bwp.id
)
--	and w.alternation is not null
order by cln

), rows1 as (
select 
	parts[1] base,
	regexp_replace(regexp_replace(parts[1], '\u001cё', 'о'), clean || '|ь$|' || ext_patt, '', 'g') cbase,
	regexp_replace(parent, clean || '|' || ext_patt, '', 'g') clalparent,
	regexp_replace(pcln, ext_patt, '', 'g') pcline,
	regexp_replace(cln, ext_patt, '', 'g') cline,
--	regexp_replace(regexp_replace(cln, '\u001cё', 'о'), ext_patt||'|ь$', '', 'g') cline,
	*  from arrs
), rows3 as (
select 
	array[numrange(1, 1+length(base))] locs,
	* from rows1
	where not clalparent ~ (try_alters(cbase)||'.*')
)
select * from rows3;
--insert into composed_affix_appl
--select word, aaid,null,null, aid,null,null, locs, locs  from rows3;
--insert into affix_appl (offs, len, orig, affix, word, parent_word)
--select 1, length(base) orl, base orig, 657 aff, word, pid from rows3 r;
;

	select bw.line, regexp_replace(bw.line, '[´]', '', 'g') as cln, caa.id caa, caa.parent par, bwp.* from composed_affix_appl caa
	right join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
		where true 
		and caa.id is null
		and bw.line not like '%/%'
		and not bw.line ~ '.*[а-ё´]{2,}-[а-ё]{2,}.*'
--		and not bw.line ~ '.*[а-ё´][а-ё]-[а-ё]{2,}.*'
		and not bw.line ~ '.*[а-ё]-[а-ё].*'
--	group by bwp.id
;


with const as (select '[´\u001b\u001c-\u001f[\]()-]'::text clean),
	arrs as (
select w.id word, pw.id pid,
	a.id aaid, a.affix aid,
	w.line origin, pw.line parent,
	regexp_replace(w.line, clean, '', 'g') cln,
	regexp_replace(pw.line, clean, '', 'g') pcln,
	regexp_matches(w.line, '(.*[а-ё])((\u001b\u001f)?\(.*\))?') parts,
	regexp_split_to_array(w.alternation, ':|-') alts,
	'[|-]'::text as ext_patt, clean
from const, bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
left join affix_appl a on w.id = a.word
where w.id in (
	select bw.id from composed_affix_appl caa
	right join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
		where true 
		and caa.id is null
		and bw.line not like '%/%'
		and not bw.line ~ '.*[а-ё´]{2,}-[а-ё]{2,}.*'
--		and not bw.line ~ '.*[а-ё´][а-ё]-[а-ё]{2,}.*'
		and not bw.line ~ '.*[а-ё]-[а-ё].*'
	group by bw.id
)
	and w.alternation is not null
order by cln

), rows1 as (
select 
	parts[1] base,
	regexp_replace(regexp_replace(parts[1], '\u001cё', 'о'), clean || '|ь$', '', 'g') cbase,
--	apply_alters(parent, alts[1], alts[2], alts[3], alts[4]) alparent,
	regexp_replace(apply_alters(parent, alts[1], alts[2], alts[3], alts[4]), clean || '|' || ext_patt, '', 'g') clalparent,
	regexp_replace(pcln, ext_patt, '', 'g') pcline,
	regexp_replace(cln, ext_patt, '', 'g') cline,
--	regexp_replace(regexp_replace(cln, '\u001cё', 'о'), ext_patt||'|ь$', '', 'g') cline,
	*  from arrs
), rows3 as (
select 
 array[numrange(1, 1+length(base))] locs,
	* from rows1
	where clalparent ~ (cbase||'.*')
)
--select * from rows3;
, ins as (
--  insert into affix_appl (offs, len, orig, affix, word, parent_word)
    select
		0 as id,
		1 offs, length(base) len, base orig, 657 affix, word, pid parent_word from rows3 r
--	returning *
)
--insert into composed_affix_appl (word, affappl1, affix1, val_locs, aff_locs)
  select ins.word, ins.id, ins.affix, rows3.locs, rows3.locs from ins join rows3 on ins.word = rows3.word;

with const as (select '[´\u001b\u001c-\u001f[\]()]'::text clean),
    arrs as (
      select w.id word, pw.id pid,
             a.id aaid, a.affix aid,
             w.line origin, pw.line parent,
             regexp_replace(w.line, clean, '', 'g') cln,
             regexp_replace(pw.line, clean, '', 'g') pcln,
             regexp_matches(w.line, '(.*?)((\u001b\u001f)?\(.+\))?$') parts,
             '[/|-]'::text as ext_patt, clean
      from const, bunch_word w
        join bunch_word pw on pw.derived_id = w.bunch
        left join affix_appl a on w.id = a.word
      where w.id in (
        select bw.id from composed_affix_appl caa
          right join bunch_word bw on bw.id = caa.word
          join bunch_word bwp on bwp.derived_id = bw.bunch
        where true
              and caa.id is null
              and bw.line not like '%/%'
              and not bw.line ~ '.*[а-ё´]{2,}-[а-ё]{2,}.*'
              and not bw.line ~ '.*[а-ё]-[а-ё].*'
      )
--	and w.alternation is null
      order by cln

  ), rows1 as (
    select
      parts[1] base,
--      regexp_replace(regexp_replace(regexp_replace(parts[1], '\u001cё', 'о'), '(.)', '\1?'), clean || '|ь$|' || ext_patt, '', 'g') cbase,
      regexp_replace(regexp_replace(parts[1], '\u001cё', 'о'), clean || '|ь$|' || ext_patt, '', 'g') cbase,
      regexp_replace(parent, clean || '|' || ext_patt, '', 'g') clalparent,
      regexp_replace(pcln, ext_patt, '', 'g') pcline,
      regexp_replace(cln, ext_patt, '', 'g') cline,
      --	regexp_replace(regexp_replace(cln, '\u001cё', 'о'), ext_patt||'|ь$', '', 'g') cline,
      *  from arrs
), rows3 as (
    select
      array[numrange(1, 1+length(base))] locs,
	  try_alters(cbase) tryalt,
      * from rows1
    where not clalparent ~ (try_alters(cbase)||'.*')-- and word in (24087)
)
select * from rows3;
, ins as (
--  insert into affix_appl (offs, len, orig, affix, word, parent_word)
    select
		0 as id,
		1 offs, length(base) len, base orig, 657 affix, word, pid parent_word from rows3 r
--	returning *
)
--insert into composed_affix_appl (word, affappl1, affix1, val_locs, aff_locs)
  select ins.word, ins.id, ins.affix, rows3.locs, rows3.locs from ins join rows3 on ins.word = rows3.word;
