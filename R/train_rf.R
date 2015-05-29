library(caret)
library(pROC)

all_data <- read.csv('target/data.csv')

test_data <- all_data[is.na(all_data$outcome),]
train_data <- all_data[!is.na(all_data$outcome),]

train_data[,"class" ] = as.factor(paste("c", train_data$outcome, sep='_'))
train_set <- subset(train_data, select=-c(bidder_id,outcome))

set.seed(37)

multiLogLoss <-function (data, lev = NULL, model = NULL) {
    ro <-roc(data$obs, data$c_1, levels(c('c_0',c_1)))
    c(ROC=ro$auc)
}

trControl <- trainControl(method="cv",
                          number=10,
                          verboseIter=FALSE,
                          classProbs = TRUE, 
                          summaryFunction = multiLogLoss,
                          allowParallel = FALSE)

rfModel <- train(class ~ ., data = train_set, 
                 method="rf",
                 ntree=500,
                 trControl = trControl,
                 metric = "ROC",
                 maximize = TRUE
)

print("Best:")
rfModel$results[as.numeric(row.names(rfModel$bestTune)),]

pred <- predict(rfModel, newdata = test_data,type="prob")
result <- data.frame(bidder_id=test_data$bidder_id,prediction=pred$c_1)
write.csv(result,'target/result.csv', quote=FALSE,row.names=FALSE)