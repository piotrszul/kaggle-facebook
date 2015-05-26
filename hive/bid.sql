CREATE EXTERNAL TABLE bid_tmp (bid_id INT,bidder_id VARCHAR(64) ,auction VARCHAR(64),merchandise VARCHAR(64),device VARCHAR(64),time BIGINT,country VARCHAR(10),ip VARCHAR(64),url VARCHAR(64)) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE LOCATION '/var/hive/staging/szu004/fbk/bid' tblproperties ("skip.header.line.count"="1");


CREATE TABLE bid(bid_id INT,bidder_id STRING ,auction STRING,merchandise STRING,device STRING,time BIGINT,country STRING,ip STRING,url STRING) STORED AS PARQUET;

CREATE TABLE bid_out(bid_id INT,bidder_id STRING ,auction STRING,merchandise STRING,device STRING,time BIGINT,country STRING,ip STRING,url STRING, outcome FLOAT) STORED AS PARQUET;

INSERT OVERWRITE TABLE bid select * from bid_tmp where bid_id IS NOT NULL;
