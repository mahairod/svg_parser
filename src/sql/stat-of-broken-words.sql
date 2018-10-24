with stats as (
select page, count(w.id) number, bunch_id, count(w.derived_id) as derives, avg(length(line)), avg(w.len) avglen,
max(w.y) top, min(w.y) bottom
from word w join bunch b on w.bunch_id=b.id
where true
	and b.x>180 --and len<75
	group by bunch_id, page
having avg(w.len)<35
	and count(w.id)>3
--and max(len)-avg(len) < stddev_pop(len)
	order by page, bunch_id, top desc
)
select b.page, w.id, s.bunch_id, w.derived_id, line, w.y, w.hyphen
from stats s join word w on s.bunch_id = w.bunch_id
join bunch b on w.bunch_id=b.id
where deprecated is null order by page, w.y desc;

with ids as (
select b.page, b.id as bunch, count(distinct w.line) num,
		avg(length(regexp_replace(w.line, '[^а-ё]+', '', 'g'))) avglen,
		max(length(regexp_replace(p.line, '[^а-ё]+', '', 'g'))) plen,
	regexp_replace(min(p.line), '[^а-ё]+', '', 'g') as cleanline
from word w
join bunch b on w.bunch_id=b.id
join word p on p.derived_id=b.id
where true and w.deprecated is null
and length(regexp_replace(w.line, '[^а-ё]+', '', 'g')) >0
group by b.id, p.derived_id
--having avg(length(w.line))*1.2 < max(length(p.line))
having count(distinct w.line)>1 and 
avg(length(regexp_replace(w.line, '[^а-ё]+', '', 'g'))) > 0.5 and 
avg(length(regexp_replace(w.line, '[^а-ё]+', '', 'g')))*1.4 < max(length(regexp_replace(regexp_replace(p.line, '\(‘[а-ё .-]+’\)|[а-ё.]+.*', '', 'g'), '[^а-ё]+', '', 'g')))
--and count (distinct p.line) >1
order by page, bunch
)
select bunch, page, cleanline, num from ids;
