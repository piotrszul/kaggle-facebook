SELECT bidder_id,COUNT(*) FROM (SELECT DISTINCT bidder_id,device FROM bid_out) as dist GROUP BY bidder_id;


