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

select id, * from affix_appl where word in (22359, 146969);

select * from affix where id in (10, 21);
