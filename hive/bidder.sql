create EXTERNAL table bidder_tmp (bidder_id STRING,payment_account STRING,address STRING ,outcome FLOAT) ROW FORMAT DELIMITED FIELDS TERMINATED BY ',' STORED AS TEXTFILE LOCATION '/var/hive/staging/szu004/fbk/bidder' tblproperties ("skip.header.line.count"="1");


create table bidder(bidder_id STRING,payment_account STRING,address STRING ,outcome FLOAT) STORED AS PARQUET; 

INSERT INTO TABLE bidder select * from bidder_tmp where outcome IS NOT NULL;


