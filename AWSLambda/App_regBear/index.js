console.log('Loading function');

var AWS = require('aws-sdk');
var doc = require('dynamodb-doc');
var crypto = require('crypto');

var dynamo = new AWS.DynamoDB();
var docClient = new AWS.DynamoDB.DocumentClient();


function checkBearID(BearID, fn){
    docClient.query({
                TableName: "BearData",
                KeyConditionExpression: "#ID = :num",

                ExpressionAttributeNames:{
                    "#ID": "BearID",
                },
                ExpressionAttributeValues:{
                    ":num": BearID,
                }
            }, 
            function(err, data) {
                if (err) {
                    return fn(err);
                    
                }
                else {

                         if(data.ScannedCount >= 1){
                             return fn(null, true);
                         }
                         else{
                             return fn(null, false);
                         }

                }
            });
    
    
}


exports.handler = function(event, context) {
    //console.log('Received event:', JSON.stringify(event, null, 2));

checkBearID(event.BearID, function(err, result){
        if(err){
	        context.fail('Error in getUserData: ' + err);
	        context.succeed({Success: false});
	    }
	    else{
	        if(result){
    	        docClient.update({
                    "TableName": "userData",
                    "Key": {
                        "Username": event.Username
                     },
                    "UpdateExpression": "set BearID = :r",
                    "ExpressionAttributeValues":{
                        ":r":event.BearID
                    },
                }, 
                function(err, data) {
                    if (err) {
                        //Tell the app registration was a failure
                        console.log(err);
                        context.succeed({
                            Success: false
                        });
                    }
                    else {
                        //Tell the app registration was a success
                        console.log("BearID updated");
                        context.succeed({
                            Success: true
                        });
                    }
                });
	        }
	        else{
	            context.succeed({
                    Success: false
                });
	        }
	   }
});
    
};