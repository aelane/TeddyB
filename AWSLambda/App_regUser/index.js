console.log('Loading function');

var AWS = require('aws-sdk');
var doc = require('dynamodb-doc');
var crypto = require('crypto');

var dynamo = new AWS.DynamoDB();
var ses = new AWS.SES();

function createHash(password, salt, fn) {

	var length = 64;
	var iterations = 500;

	fn = salt
	//create a random salt and then use it to hash the password
	crypto.randomBytes(length, function(err, salt) {
		if (err) {
		    return fn(err);
		}
		salt = salt.toString('base64');
		crypto.pbkdf2(password, salt, iterations, length, function(err, derivedKey) {
			if (err) {
			    return fn(err);
			}
			fn(null, salt, derivedKey.toString('base64'));
		});
	});
	
}

exports.handler = function(event, context) {
    //console.log('Received event:', JSON.stringify(event, null, 2));

    //Create the hashed password
    createHash(event.Password, function(err, salt, hash) {
		if (err) {
			context.fail('Error in hash: ' + err);
		} 
		else {
		    //save the Username, hashed password, and the salt in our database
            dynamo.putItem({
                "TableName": "userData",
                "ConditionExpression" : "attribute_not_exists (Username)",
                "Item" : {
                    "Username": {"S": event.Username },
                    "Password": {"S": hash},
                    "Salt": {"S":salt},
                    "BearID": {"S":"Blank"}
                }
            }, 
            function(err, data) {
                if (err) {
                    //Tell the app registration was a failure
                    context.succeed({
                        Success: false
                        });
                }
                else {
                    //Tell the app registration was a success
                    console.log("New User Registered");
                    context.succeed({
                        Success: true
                    });
                }
            });
		}
    });
};