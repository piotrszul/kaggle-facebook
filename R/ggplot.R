library(ggplot2)


#9631916842105263
#9645558894736842
#9695580000000000
#9709222052631578
#9759243157894736
#9772885210526315

#52631578

x = ggplot(result,aes(x=(start_time - 9631916842105263)/17684210526,y=bids)) + 
    geom_point(aes(color=outcome)) +
    scale_y_log10()
    #    scale_colour_gradient(limits=c(0, 13), low="yellow", high="blue") +
    
    
x