SELECT bidder_id,AVG(cnt),MIN(cnt),MAX(cnt),stddev_samp(cnt) FROM (SELECT bidder_id,auction,COUNT(*) AS cnt FROM bid_out GROUP BY bidder_id,auction) as tmp  GROUP BY bidder_id;

