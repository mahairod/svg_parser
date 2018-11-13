select * from composite_affix_application
where (affices[1] in (655) or affices[2] in (655))
and line like '%/%';


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
where a.affix = 656 and w.id in (
	select bwp.id from composed_affix_appl caa
	join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
	left join composed_affix_appl pcaa on pcaa.word = bwp.id
		where true 
		and caa.parent is null
		and pcaa.word is null
--		and bwp.line like '%/%'
	group by bwp.id
)
order by cln

), rows1 as (
select regexp_replace(pcln, ext_patt, '', 'g') pcline,
	regexp_replace(cln, ext_patt, '', 'g') cline,
	regexp_matches(origin, '(.*[а-ё])((\u001b\u001f)?\(.*\))?') parts,
	*  from arrs
), rows2 as (
select  regexp_matches(pcline, cline||'.*'), parts[1] base, * from rows1
), rows3 as (
select array[numrange(1, 1+length(base))] locs, * from rows2
)
--insert into composed_affix_appl
select word, aaid,null,null, aid,null,null, locs, locs  from rows3;
--select parts[1] base, * from rows3;
--insert into affix_appl (offs, len, orig, affix, word, parent_word)
--select 1, length(base) orl, base orig, 656 aff, word, pid from rows3 r;
;

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
where /*a.affix = 656 and*/ w.id in (
	select bwp.id from composed_affix_appl caa
	join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
	left join composed_affix_appl pcaa on pcaa.word = bwp.id
		where true 
		and caa.parent is null
		and pcaa.word is null
--		and bwp.line like '%/%'
	group by bwp.id
)
order by cln

), rows1 as (
select regexp_replace(pcln, ext_patt, '', 'g') pcline,
	regexp_replace(cln, ext_patt, '', 'g') cline,
	regexp_matches(origin, '(.*[а-ё])((\u001b\u001f)?\(.*\))?') parts,
	*  from arrs
), rows2 as (
select  regexp_matches(pcline, cline||'.*'), parts[1] base, * from rows1
), rows3 as (
select array[numrange(1, 1+length(base))] locs, * from rows2
)
select * from rows3;
--insert into composed_affix_appl
--select word, aaid,null,null, aid,null,null, locs, locs  from rows3;
--insert into affix_appl (offs, len, orig, affix, word, parent_word)
--select 1, length(base) orl, base orig, 656 aff, word, pid from rows3 r;
;
