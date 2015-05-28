library(RPostgreSQL)

drv <- dbDriver("PostgreSQL")
conn <-dbConnect(drv, host="postgis", user="ubuntu", password="qwerty", db="fbk")
#result <-dbGetQuery(conn,"select bid.*,bidder.outcome FROM bid LEFT OUTER JOIN bidder USING(bidder_id) where auction='kd8yk' order by bid_id")
#result <-dbGetQuery(conn,"select * from bid_full where from_start=0 order by time limit 100000");
result <-dbGetQuery(conn,"select auction.*,outcome from auction JOIN bid_full ON auction.end_bid_id = bid_full.bid_id order by start_time");

dbDisconnect(conn)