console.log('Loading function');

var AWS = require('aws-sdk');

var dynamo = new AWS.DynamoDB();
var docClient = new AWS.DynamoDB.DocumentClient();
var iotdata = new AWS.IotData({endpoint: 'A2TGLQBSJH6LW0.iot.us-east-1.amazonaws.com'});

function getTranslation(Topic,Language, fn){

    dynamo.getItem({
                "TableName": "topicList",
                "Key" : {
                    "Topic": {"S": Topic }
                }
            }, 
            function(err, data) {
                if (err) {
                    console.log("Err in getting words");
                    return fn(err);
                }
                else {
                    switch(Language) {
                        case "English":
                            fn(err, data.Item.English.SS, data.Item.English.SS);
                            break;
                        case "French":
                            fn(err, data.Item.English.SS, data.Item.French.SS);
                            break;
                        case "Spanish":
                            fn(err, data.Item.English.SS,data.Item.Spanish.SS);
                            break;
                        case "Greek":
                            fn(err, data.Item.English.SS,data.Item.Greek.SS);
                            break;
                        case "Persian":
                            fn(err, data.Item.English.SS,data.Item.Persian.SS);
                            break;
                        default:
                           fn(err);
                    } 
                }
            });

}

function getTopic(BearID, fn){

    dynamo.getItem({
                "TableName": "BearData",
                "Key" : {
                    "BearID": {"S": BearID}
                }
            }, 
            function(err, data) {
                if (err) {
                    console.log("Err in getting Topic");
                    return fn(err);
                }
                else {
                    fn(err, data.Item.Language.S, data.Item.Topic.S,  data.Item.TeachingMode.S);
                }
            });

}

function updateTopic(BearID, currTopic, fn){
    var Topics = ["Items", "Numbers", "People", "Body", "Food", "Animals"];
    var topicPosition = Topics.indexOf(currTopic); 
    topicPosition += 1;
    console.log(topicPosition);
    if(topicPosition == 6){
        topicPosition = 0;
    }

    docClient.update({
        "TableName": "BearData",
        "Key": {
            "BearID": BearID
         },
        "UpdateExpression": "set Topic = :r",
        "ExpressionAttributeValues":{
            ":r": Topics[topicPosition]
        },
    }, 
    function(err, data) {
        if (err) {
            return fn(err);
        }
        else {
            return fn(null);
        }
    });
}

exports.handler = function(event, context) {
    console.log(event);
    getTopic(event.BearID,function(err, Language, Topic, TeachingMode){
        if(err){
            console.log("Err in getTopic");
            console.log(err);
        }
        else{
            getTranslation(Topic,Language,function(err, English, Translation){
                if(err){
                    console.log("Err in getTranslation");
                    console.log(err);
                }
                else{
                    if(Language == "English"){
                        for(i = 0; i < English.length;i++){
                            English[i] = English[i].slice(2);
                        }
                    }
                    else{
                        for(i = 0; i < English.length;i++){
                            English[i] = English[i].slice(2);
                            Translation[i] = Translation[i].slice(2);
                        }
                    }
                    console.log("done cutting");
                    var Response = {
                        "Language": Language,
                        "Setting": TeachingMode,
                        "Topic": Topic,
                        "TopicLen": English.length.toString(),
                        "English": English,
                        "Translation": Translation,
                        "End": true
                    };
                    console.log(Response);
                    var params = {
                        topic: 'Bear/Curriculum/Response',
                        payload: JSON.stringify(Response),
                        qos: 0
                    };
                   iotdata.publish(params, function(err, data){
                        if(err){
                            console.log(err);
                        }
                        else{
                            updateTopic(event.BearID, Topic, function(err){
                                if(err){
                                    console.log(err);
                                }
                                else{
                                    console.log("success");
                                    console.log(event);
                                    context.succeed(event);
                                }
                            })
                            
                        }
                    }); 
                }
            }); 
        }
    }); 
};