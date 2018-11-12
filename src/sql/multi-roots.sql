select * from affix where qty<0; --653, 654

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

select * from affix_appl where affix >652; -- 33813, 33814..

select * from composed_affix_appl caa where affix1 >652;

--update composed_affix_appl set parent = 31260 where id in (
select caa.id, parent from composed_affix_appl caa
join bunch_word bw on bw.id = caa.word
where bw.inroot and word>0 and affix1 >652;
--);

select bwp.id, min(bwp.page) page, min(bwp.line) ln, string_agg(bw.line, '; '), count(*) from composed_affix_appl caa
join bunch_word bw on bw.id = caa.word
join bunch_word bwp on bwp.derived_id = bw.bunch
where parent is null and bwp.line like '%/%'
group by bwp.id
;

with arrs as (
select count(*) qty, array_agg(w.id order by w.id) ids, array_agg(pw.id order by w.id) pids, array_agg(a.id order by w.id) aaids,
	array_agg(w.line order by w.id) origins, regexp_replace(w.line, '[´\u001b-\u001f[\]()]', '', 'g') cln
from bunch_word w
join bunch_word pw on pw.derived_id = w.bunch
join affix_appl a on pw.id = a.word
where w.id in (
	select bwp.id from composed_affix_appl caa
	join bunch_word bw on bw.id = caa.word
	join bunch_word bwp on bwp.derived_id = bw.bunch
	where parent is null and bwp.line like '%/%'
	group by bwp.id
)
group by cln
order by qty desc, cln

), rows1 as (
select unnest(ids) word, array_fill(653, array[array_length(pids, 1)]) aids,
	regexp_split_to_array(unnest(origins), '/') ps, unnest(origins) origin, * from arrs
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
select word, aaids[1],aaids[2],aaids[3], aids[1],aids[2],aids[3], locs, locs  from rows3
;

select * from bunch_word w
left join composed_affix_appl a on w.id = a.word
where w.line like '%/%';

select * from bunch_word bw where bunch = 45114 or 45114 = derived_id;
select * from affix_appl aa where word in (94888,94889);
select * from composed_affix_appl caa where word in (94888,94889);

select bw.page, bw.line, caa.word, caa.id, caa.parent from composed_affix_appl caa
join bunch_word bw on bw.id = caa.word
where parent is null
order by word;
