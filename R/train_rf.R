library(caret)
library(pROC)

printf <- function(...) invisible(print(sprintf(...)))
commit <- function() {
    modelId <-paste(model$method, format(model$timestamp, "%y%m%d_%H%M%S"), sprintf('%0.5f', estAUC), sep='-' )
    printf("Committing model: %s", modelId)
    write.csv(resultTest,sprintf('models/%s.csv', modelId), quote=FALSE,row.names=FALSE)
    write.csv(resultTrain,sprintf('models/meta/train_%s.csv', modelId), quote=FALSE,row.names=FALSE)    
    save(model,file=sprintf('models/meta/%s.Rdata', modelId)) 
    system("git add .")
    system(sprintf('git commit -m "MODEL: %s"', modelId))
}

all_data <- read.csv('target/data.csv')

test_data <- all_data[is.na(all_data$outcome),]
train_data <- all_data[!is.na(all_data$outcome),]

train_data[,"class" ] = as.factor(paste("c", train_data$outcome, sep='_'))
train_set <- subset(train_data, select=-c(bidder_id,outcome))

set.seed(37)

multiLogLoss <-function (data, lev = NULL, model = NULL) {
    ro <-roc(data$obs, data$c_1, levels(c('c_0','c_1')))
    c(ROC=ro$auc)
}

trControl <- trainControl(method="cv",
                          number=10,
                          verboseIter=TRUE,
                          classProbs = TRUE, 
                          summaryFunction = multiLogLoss,
                          allowParallel = FALSE)

model <- train(class ~ ., data = train_set, 
                 method="rf",
                 ntree=500,
                 importance = TRUE,
                 trControl = trControl,
                 metric = "ROC",
                 maximize = TRUE, 
                 tuneLength = 5
)

model$timestamp <-Sys.time()
bestTune <- model$results[order(as.numeric(row.names(model$result)))[as.numeric(row.names(model$bestTune))],]
print("Best:")
bestTune
estAUC<-bestTune[length(bestTune-2)]
printf("Estimated AUD %f" , estAUC)
predTrain <- predict(model, newdata=train_data,type="prob")
resultTrain <- data.frame(bidder_id=train_data$bidder_id,prediction=predTrain$c_1)
trainROC <-roc(train_data$class, predTrain$c_1, levels(c('c_0','c_1')))
trainAUC <-trainROC$auc 
printf("Train AUC: %f", trainAUC)
predTest <- predict(model, newdata = test_data,type="prob")
resultTest <- data.frame(bidder_id=test_data$bidder_id,prediction=predTest$c_1)


