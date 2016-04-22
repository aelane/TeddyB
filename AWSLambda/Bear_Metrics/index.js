console.log('Loading function');

var AWS = require('aws-sdk');

var dynamo = new AWS.DynamoDB();
var dynamoClient = new AWS.DynamoDB.DocumentClient();


exports.handler = function(event, context) {
    console.log(event);

    var time = new Date();
    
    var dd = time.getDate();
    var MM = time.getMonth()+1; //January is 0!
    var yyyy = time.getFullYear();
    
    var hh = time.getHours();
    var mm = time.getMinutes();
    var ss = time.getMinutes();
    
    if(dd<10) {
        dd='0'+dd
    }
    if(MM<10) {
        MM='0'+MM
    } 
    if(hh<10) {
        hh='0'+hh
    } 
 
   if(mm<10) {
        mm='0'+mm
    } 
    if(ss<10) {
        ss='0'+ss
    } 
    time = MM+'/'+dd+'/'+yyyy+" "+hh+":"+mm+":"+ss;
    console.log(event);
   dynamo.putItem({
                "TableName": "Metrics",
                "Item" : {
                    "BearID": {"S": event.BearID },
                    "Date" : {"S": time},
                    "CorrectWord": {"S": event.CorrectWord},
                    "WordSaid": {"S": event.WordSaid},
                    "TeachingMode": {"S": event.TeachingMode},
                    "Language": {"S": event.Language},
                    "Topic": {"S": event.Topic},
                    "Correct": {"BOOL": JSON.parse(event.Correct)},
                    "Attempt": {"N": String(event.Attempt)}
                }
            }, 
            function(err, data) {
                if (err) {
                    console.log(err);
                    context.succeed({Success: false});
                }
                else {
                    context.succeed({
                        Success: true
                    });
                }
            });
   
};