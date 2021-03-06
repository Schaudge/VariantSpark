library(rpart)
library(randomForest)

wideData <- read.csv('../../../tmp/gendata.csv', header=TRUE, row.names = 1)
data = t(wideData)
label <- read.csv('../../../tmp/labels.csv', header=TRUE)
# rf is not used, is this used elsewhere?
rf <- randomForest(as.matrix(data), as.factor(label$label), importance= TRUE)
