select b.page as page, b.id as bunch, w.id, w.line, w.y, sw.id, w.notes, (w.y-sw.y) as diff
	from word sw
	join bunch b on sw.bunch_id=b.id
	join word w on w.bunch_id=b.id and -0.1<(w.y-sw.y) and (w.y-sw.y)<10
where sw.line like '%(%черед%'
--group by w.id, b.id
order by bunch, w.id;

select page, w.id, bunch_id, w.derived_id, line, notes, alt_rest, pos
from word w join bunch b on w.bunch_id=b.id where deprecated is null
		and line like '%(%)%'
--		and line like '%(%[%е%]%)%'
		and not line like '%(%_й%)%'
		and not line like '%(_)'
		and not line like '%(_´)'
		and not line like '%(%ец)'
		and not line like '%(ть)%'
		and not line like '%([j-_]%)' -- one symb at end
		and not line like '%(%и_[j-_]%)' -- one symb at end
		and not line like '%([-иj-а])'
		and not line like '%(%и´_[j-_])'
		and not line like '%(и_)%'
		and not line like '%(и´я)%'
;

WITH clw AS (
select 
		(regexp_replace(line, '[´]+', '\1', 'g')) as line,
		w.id, bunch_id, w.derived_id, notes, alt_rest, pos
				from word w where deprecated is null)
select b.page, clw.* from clw join bunch b on clw.bunch_id=b.id
where true
		and line like '%(%)%'
		and not line like '%(%ый)%'
		and not line like '%(%ий)%'
		and not line like '%(%ой)%'
		and not line like '%(_)%'
		and not line like '%(_´)'
		and not line like '%(-_)'
		and not line like '%(-ец)'
		and not line like '%(%к-а)'
		and not line like '%(ка)'
		and not line like '%(ёк)'
		and not line like '%(д-а)'
		and not line like '%(ец)'
		and not line like '%(ение)'
		and not line like '%(ат)'
		and not line like '%(ум)'
		and not line like '%(уум)'
		and not line like '%(чик)'
		and not line like '%(_ин-а)'
		and not line like '%(ино)'
		and not line like '%(%ник)'
		and not line like '%(ус)'
		and not line like '%(-ств-о)'
		and not line like '%(ть)'
		and not line like '%[%j(_)]'
		and not line like '%[j(-_)]'
		and not line like '%[j(-э]ц)'
		and not line like '%([j-_]%)' -- one symb at end
		and not line like '%(%и[j-_]%)' -- one symb at end
		and not line like '%(%и´[j-_])'
		and not line like '%([-иj-а])'
		and not line like '%(и_)%'
		and not line like '%(ое)'
		and not line like '%(ая)'
		and not line like '%(и´я)%'
;


select id, bunch_id, line, notes, version from word where deprecated is null and version is not null;
select id, bunch_id, line, notes, alt_rest, version from word where deprecated is null and line like '%II%';
select id, bunch_id, line, notes, version, pos from word where deprecated is null and line like '%чик%';

select id, line, notes, flags from word where deprecated is null and line like '==%' order by id;
--update word set flags[0] = 1::bit(16) where notes like '%[%(%)%(%)%]';

select id, line, substring(line from '(.*\)) $') as target from word where line like '%) ';
--update word set line = substring(line from '(.*\)) $') where line like '%) ';

select id, line, notes, bunch_id b, regexp_replace(w.line, '[^а-ё]+', '', 'g') as clean from word w
	where deprecated is null and line not like '==%' and line like '%-э%' and length(regexp_replace(w.line, '[^а-ё]+', '', 'g'))<5
order by length(regexp_replace(w.line, '[^а-ё]+', '', 'g'));
--47363, 57828, 45383, 71193, 48035, 15170, 49458
--10250 15170

select * from bunch where abs(id - 8403) < 50;
select * from word where bunch_id in (29633) order by bunch_id, y desc;

select max(id) from bunch where true;

select w.line, b.page as page, pw.line as xlabel, b.id xgrp, pb.id pgr, pw.bunch_id lgrp,
to_char(w.x, '999')||'-'||to_char(w.x+w.len, '999')||','||to_char(w.y, '999') as point,
to_char(b.x, '999')||','||to_char(b.y, '999')||'-'||to_char(b.y+b.height, '999') as coord
from bunch b
left join word pw on b.id = pw.derived_id
,word w
join bunch pb on pb.id = w.bunch_id
where pb.page = b.page and
b.id = pb.id and
b.y < w.y and w.y < b.y + b.height and
w.x < b.x-6 and b.x+6 < w.x + w.len
;

select * from bunch where page=562 and root;
select * from bunch where id=69742;

select * from word where bunch_id in (47363, 57828, 45383, 71193, 48035, 15170, 49458) order by bunch_id, y desc;
select * from word where bunch_id in (69742,69742/*,62485*/) order by bunch_id, y desc;

select * from word where deprecated =false;

select * from word where deprecated is null;

select * from word where deprecated is null and line like '__' and not line like '===%';
select * from word where deprecated is null and line like '===' and len > 120;

select * from word where derived_id = 4399 order by bunch_id, y desc;

select page, w.id, bunch_id, w.derived_id, line, version, w.x, w.y
from word w join bunch b on w.bunch_id=b.id
where deprecated is null and w.bunch_id= derived_id order by page, w.y desc;

select page, w.id, bunch_id, w.derived_id, line, w.x, w.y
from word w join bunch b on w.bunch_id=b.id
where deprecated is null and page=304 order by page, bunch_id, w.y desc;

--count group words from roots
select page, b.id as bunch, count(mw.line) gr_size, max(mw.line)
from bunch b
left join word mw on mw.bunch_id = b.id
where mw.deprecated is null and b.root
group by b.id
order by gr_size desc, b.id asc;

--count group-words from group with parent word
select distinct page, b.id as bunch
from word w join bunch b on w.derived_id=b.id
where w.deprecated is null
--group by b.id
order by b.id asc;

--show groups with no parent word (except roots), incl empty groups
select page, b.id as bunch, w.id, bunch_id, w.derived_id, line
from word w
right join bunch b on w.derived_id=b.id
where w.derived_id is null and not b.root
order by b.id asc;

--same with word count per (non empty) group
select page, b.id as bunch, count(mw.id) gr_size, max(mw.line)
from word w
right join bunch b on w.derived_id=b.id
join word mw on mw.bunch_id = b.id
where w.derived_id is null and mw.deprecated is null and not b.root
group by b.id
having count(mw.id)>0
order by gr_size desc, b.id asc;

--same per page
select page, count(distinct b.id) as bunches, count(mw.id) gr_sum_size, string_agg(mw.line, ', '), count(mw.line) / count(distinct b.id) as "W/G"
from word w
right join bunch b on w.derived_id=b.id
left join word mw on mw.bunch_id = b.id
where w.derived_id is null and mw.deprecated is null and not b.root
group by page
having count(mw.id)>0
order by page, gr_sum_size desc, bunches desc;

--same with total word count
select count(distinct page) pages, count(distinct b.id) as bunches, count(mw.line) words
from word w
right join bunch b on w.derived_id=b.id
join word mw on mw.bunch_id = b.id
where w.derived_id is null and mw.deprecated is null and not b.root;

select * from word where bunch_id = 3561 order by bunch_id, y desc;
select * from word where line like 'диверс%';

select * from word where id = 1002003 order by bunch_id, y desc;
insert into word select (w #= hstore('id', '1002004')).* from word w where id=37984;


select page, w.* from word w join bunch on bunch_id=bunch.id where bunch_id = 3561 order by bunch_id, y desc;

with v as (select 3 as span)
select b.page, b.id as bunch, pw.line pline, w.line line
from word w
right join bunch b on w.derived_id=b.id
join bunch pb on pb.page = b.page
join v on true
join word pw on pw.bunch_id = pb.id
	and b.x-100 < pw.x + pw.len and pw.x + pw.len < b.x and b.y < pw.y-v.span and pw.y+v.span < b.y + b.height
where w.derived_id is null
and pw.deprecated is null and pw.line not like '===%'
and pw.derived_id is null
--group by b.id
order by page, pline desc, b.id asc;


with v as (select 0 as span)
--, lnk as (
select b.page, b.id as bunch, min(pw.id) word, count(distinct pw.id) par_count,
	array_agg(pw.x::int) x,
	max(pw.line) line
	,string_agg(mw.line, ', ')
from word w
right join bunch b on w.derived_id=b.id
join word mw on mw.bunch_id = b.id and mw.deprecated is null
join bunch pb on pb.page = b.page
join v on true
join word pw on pw.bunch_id = pb.id
	and b.x-20 < pw.x + pw.len and pw.x + pw.len < b.x and b.y < pw.y-v.span and pw.y+v.span < b.y + b.height
where w.derived_id is null
and pw.deprecated is null and pw.line not like '===%'
and pw.derived_id is null
group by b.id
having count(pw.line)<=10
order by page, par_count desc, b.id asc

--)update word w set derived_id = lnk.bunch from lnk where w.id = lnk.word;

;--show groups with no member words
select distinct page, b.id as bunch, count(w.id) as num
from word w right join bunch b on w.bunch_id=b.id and w.deprecated is null 
where w.bunch_id is null
group by b.id
order by b.id asc;

