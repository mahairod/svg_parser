
select id, word, parent, array[affix1,affix2,affix3] from composed_affix_appl caa;
/*
WITH RECURSIVE t(id, word, parent, affs, mark) AS (
    select id, word, parent, array[affix1,affix2,affix3] affs, id from composed_affix_appl caa where id < 50 and id <= 3
  UNION ALL
    SELECT c.id, c.word, c.parent, array[affix1,affix2,affix3] affs, mark from composed_affix_appl c, t WHERE c.id = t.parent
)
SELECT * FROM t order by mark;
*/

WITH RECURSIVE t(id, word, parent, affs, mark) AS (
    select id, word, parent, array[array[affix1,affix2,affix3]] affs, word from composed_affix_appl caa where parent=31260 and word between 1 and 1000
  UNION ALL
    SELECT c.id, c.word, c.parent, t.affs || array[affix1,affix2,affix3] affs, mark from composed_affix_appl c, t WHERE t.id = c.parent
)
SELECT * FROM t order by mark;

with affs_combs as (
WITH RECURSIVE t(id, word, parent, affs, root) AS (
    select id, word, parent, array[array[affix1,affix2,affix3]] affs, word from composed_affix_appl caa where parent=31260
  UNION ALL
    SELECT c.id, c.word, c.parent, t.affs || array[affix1,affix2,affix3] affs, root from composed_affix_appl c, t WHERE t.id = c.parent
)
SELECT affs, row_number() over() ind, row_number() over(order by affs) intind, count(*) qty FROM t group by affs order by qty desc

) 
select array_agg(aff_val), ind, count(*) size, max(qty) qty from (
	select a.value aff_val, intind, ind, aff_ind.qty qty from (
		select unnest(affs) affix, intind, ind, qty from affs_combs
	) as aff_ind
	join affix a on a.id = aff_ind.affix
	where affix is not null order by ind, intind
)as affs group by ind order by qty desc;

select id, word, parent, array[affix1,affix2,affix3] affs, 0 from composed_affix_appl caa where id in (1,2,3, 31781);

select id, word, parent, array[affix1,affix2,affix3] affs, 0 from composed_affix_appl caa where word in (1,2,3);

select * from word w
join bunch b on w.bunch_id = b.id
left join composed_affix_appl caa on caa.word = w.id
where deprecated is null
and caa.id is null;



with affs_combs as (
WITH RECURSIVE t(id, word, parent, affs, root) AS (
    select id, word, parent, array[array[affix1,affix2,affix3]] affs, word from composed_affix_appl caa where parent=31260
  UNION ALL
    SELECT c.id, c.word, c.parent, t.affs || array[affix1,affix2,affix3] affs, root from composed_affix_appl c, t WHERE t.id = c.parent
)
SELECT affs, row_number() over() ind, row_number() over(PARTITION by affs) intind, count(*) qty FROM t group by affs order by qty desc

) 
--select array_agg(aff_val), ind, count(*) size, max(qty) qty from (
	select a.value aff_val, intind, ind, aff_ind.qty qty from (
		select unnest(affs) affix, intind, ind, qty from affs_combs
	) as aff_ind
	join affix a on a.id = aff_ind.affix
	where affix is not null order by ind, intind
--)as affs group by ind order by qty desc;
