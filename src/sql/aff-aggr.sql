
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

WITH RECURSIVE t(id, word, parent, affs, root) AS (
    select id, word, parent, array[array[affix1,affix2,affix3]] affs, word from composed_affix_appl caa where parent=31260 and word between 1 and 90000
  UNION ALL
    SELECT c.id, c.word, c.parent, t.affs || array[affix1,affix2,affix3] affs, root from composed_affix_appl c, t WHERE t.id = c.parent
)
SELECT root, *
FROM t;


select id, word, parent, array[affix1,affix2,affix3] affs, 0 from composed_affix_appl caa where id in (1,2,3, 31781);

select id, word, parent, array[affix1,affix2,affix3] affs, 0 from composed_affix_appl caa where word in (1,2,3);

select * from word w
join bunch b on w.bunch_id = b.id
left join composed_affix_appl caa on caa.word = w.id
where w.derived_id is null and deprecated is null and b.root
and caa.id is null;