select * from bunch;
select * from word where deprecated is null;
select * from word where deprecated is null and line like '___' and not line like '===%';


--count group-words from group with parent word
select distinct page, b.id as bunch
from word w join bunch b on w.derived_id=b.id
where w.deprecated is null
--group by b.id
order by b.id asc;

--count group words from roots
select page, b.id as bunch, count(mw.line) gr_size, max(mw.line)
from bunch b
left join word mw on mw.bunch_id = b.id
where mw.deprecated is null and b.root
group by b.id
order by gr_size desc, b.id asc;

--count groups with no parent word (except roots)
select page, b.id as bunch, w.id, bunch_id, w.derived_id, line, w.y
from word w right join bunch b on w.derived_id=b.id
where w.derived_id is null and not b.root order by b.id asc;

--same with word count
select page, b.id as bunch, count(mw.line) gr_size, max(mw.line)
from word w
right join bunch b on w.derived_id=b.id
left join word mw on mw.bunch_id = b.id
where w.derived_id is null and mw.deprecated is null and not b.root
group by b.id
order by gr_size desc, b.id asc;

--same per page
select page, count(b.id) as bunches, count(mw.line) gr_sum_size, max(mw.line)
from word w
right join bunch b on w.derived_id=b.id
left join word mw on mw.bunch_id = b.id
where w.derived_id is null and mw.deprecated is null and not b.root
group by page
order by bunches desc, gr_sum_size desc;

