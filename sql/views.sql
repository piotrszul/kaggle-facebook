

create table bid_duration(bid_id BIGINT NOT NULL PRIMARY KEY, duration BIGINT, from_start BIGINT, from_end BIGINT, au_duration BIGINT);
INSERT INTO bid_duration SELECT bid_id,time-lag(time) OVER (PARTITION BY auction ORDER BY time, bid_id),time-first_value(time) OVER (PARTITION BY auction ORDER BY time, bid_id),last_value(time) OVER (PARTITION BY auction ORDER BY time, bid_id ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING)-time , last_value(time) OVER (PARTITION BY auction ORDER BY time, bid_id ROWS BETWEEN UNBOUNDED PRECEDING AND UNBOUNDED FOLLOWING) - first_value(time) OVER (PARTITION BY auction ORDER BY time, bid_id) FROM bid;

create materialized view bid_full AS select bid.*,duration,from_start,from_end,au_duration,outcome FROM bid LEFT OUTER JOIN bidder USING(bidder_id) JOIN bid_duration USING(bid_id)