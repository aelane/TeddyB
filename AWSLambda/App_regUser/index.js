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

function sendVerificationEmail(email, token, fn) {
    
	var verificationLink = "meh" + '?email=' + encodeURIComponent(email) + '&verify=' + token;
	ses.sendEmail({
		Source: "teddyec463@gmail.com",
		Destination: {
			ToAddresses: [
				email
			]
		},
		Message: {
			Subject: {
				Data: "Verification Email for TED"
			},
			Body: {
				Html: {
					Data: '<html><head>'
					+ '<meta http-equiv="Content-Type" content="text/html; charset=UTF-8" />'
					+ '<title>' + "TED Verification Email" + '</title>'
					+ '</head><body>'
					+ 'Please <a href="' + verificationLink + '">click here to verify your email address</a> or copy & paste the following link in a browser:'
					+ '<br><br>'
					+ '<a href="' + verificationLink + '">' + verificationLink + '</a>'
					+ '</body></html>'
				}
			}
		}
	}, fn);
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