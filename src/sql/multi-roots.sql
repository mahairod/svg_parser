select * from affix where qty<0; --653, 654, 655
----- root words
with rws as (
select * from bunch_word where inroot
)
--insert into affix_appl 
select 1, length(r.line), r.line, 653, r.id from rws r;

--insert into composed_affix_appl 
	select aa.word, aa.id, null, null, -- aas
		aa.affix, null, null, -- as
		array[numrange(aa.offs,aa.offs+aa.len)],
		array[numrange(aa.offs,aa.offs+aa.len)]
 from affix_appl aa where affix >= 653;

select * from affix_appl where affix >652 order by affix desc; -- 33813, 33814..

select * from composed_affix_appl caa where affix1 >652;

--update composed_affix_appl set parent = 31260 where id in (
select caa.id, parent from composed_affix_appl caa
join bunch_word bw on bw.id = caa.word
where bw.inroot and word>0 and affix1 >652;
--);

select bwp.id, min(bwp.page) page, min(bwp.line) ln, string_agg(bw.line, '; '), count(*) from composed_affix_appl caa
join bunch_word bw on bw.id = caa.word
join bunch_word bwp on bwp.derived_id = bw.bunch
left join composed_affix_appl pcaa on pcaa.word = bwp.id
	where true 
	and caa.parent is null
	and pcaa.word is null
--	and bwp.line like '%/%'
group by bwp.id
;
-----------------------------------------------------------------------------------------------------------

-- not saved whole word parts
with rws as (
select count(*) qty, array_agg(w.id order by w.id) ids, array_agg(pw.id order by w.id) pids,
	array_agg(w.line order by w.id) origins, array_agg(pw.line order by w.id) parents, regexp_replace(w.line, '[´\u001b-\u001f[\]()]', '', 'g') cln
from bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
where w.id in (
	select bwp.id from composed_affix_appl caa
	join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
	left join composed_affix_appl pcaa on pcaa.word = bwp.id
		where true 
		and caa.parent is null
		and pcaa.word is null
		and bwp.line like '%/%'
	group by bwp.id
)
group by cln
)
--delete from affix_appl where affix = 655 and word in (select unnest(r.pids) pid from rws r);
--insert into affix_appl (offs, len, orig, affix, word, parent_word)
select 1, length(unnest(r.parents)) pl, unnest(r.parents) par, 655 aff, unnest(r.ids) id, unnest(r.pids) pid from rws r;

-----------------------------------------------------------------------------------------------------------------------------

with arrs as (
select count(*) qty, array_agg(w.id order by w.id) ids, array_agg(pw.id order by w.id) pids,
	array_agg(a.id order by w.id) aaids, array_agg(a.affix order by w.id) aids,
	array_agg(w.line order by w.id) origins, array_agg(pw.line order by w.id) parents, regexp_replace(w.line, '[´\u001b-\u001f[\]()]', '', 'g') cln
from bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
left join affix_appl a on w.id = a.word
where /*a.affix = 655 and*/ w.id in (
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
group by cln
order by qty desc, cln

), rows1 as (
select unnest(ids) word,
	regexp_split_to_array(unnest(origins), '/') ps, unnest(origins) origin, * from arrs
--) select * from rows1;
), rows2 as (
select array[
	numrange(1, length(ps[1])+1),
	numrange(	length(ps[1])+2, length(ps[1])+length(ps[2])+2),
	numrange(					 length(ps[1])+length(ps[2])+3, length(ps[1])+length(ps[2])+length(ps[3])+3)
] locs_, * from rows1
), rows3 as (
select array((select el from unnest(locs_) l(el) where lower(el)<=length(origin))) as locs, * from rows2
)
--insert into composed_affix_appl
select * from rows3 order by qty desc;
select word, aaids[1],aaids[2],aaids[3], aids[1],aids[2],aids[3], locs, locs  from rows3;
;

select * from bunch_word w
left join composed_affix_appl a on w.id = a.word
where w.line like '%/%' and affappl1 is null;

select * from bunch_word bw where bunch = 45114 or 45114 = derived_id;
select * from affix_appl aa where affix in (655);
select * from composite_affix_application caa where affices[1] in (655);

select bw.page, bw.line, caa.word, caa.id, caa.parent from composed_affix_appl caa
join bunch_word bw on bw.id = caa.word
where parent is null
order by word;
