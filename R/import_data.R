library(RPostgreSQL)

drv <- dbDriver("PostgreSQL")
conn <-dbConnect(drv, host="postgis", user="ubuntu", password="qwerty", db="fbk")
result <-dbGetQuery(conn,"select * from data");
dbDisconnect(conn)
write.csv(result,"target/data.csv",quote=FALSE,row.names=FALSE)