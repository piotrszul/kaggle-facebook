SELECT bidder_id,COUNT(*) FROM (SELECT DISTINCT bidder_id,country FROM bid_out) as dist GROUP BY bidder_id;


