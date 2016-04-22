console.log('Loading function');

var AWS = require('aws-sdk');
var doc = require('dynamodb-doc');
var crypto = require('crypto');

var dynamo = new AWS.DynamoDB();
var cognito = new AWS.CognitoIdentity();
var ses = new AWS.SES();



function getCognitoInfo(Username, fn){
    //"login.TED.TEDapp" (ProviderID)
    //Create some user data to later get a token
    var cognitoInfo = {
        IdentityPoolId : "us-east-1:b0b7a95e-1afe-41d6-9465-1f40d1494014",
        Logins : {
            "login.TED.TEDapp" : Username
        }
    };
    
    //Get the token and id which will be send back to the app
    cognito.getOpenIdTokenForDeveloperIdentity(cognitoInfo,
		function(err, data) {
			if (err) {
			    console.log(err);
			    return fn(err);
			}
			else {
			    fn(null, data.IdentityId, data.Token);
			}
	});
    
}

function createHash(password, salt, fn) {

	var length = 64;
	var iterations = 500;

    //Hash the password that we recieved using the saved salt
	crypto.pbkdf2(password, salt, iterations, length, function(err, derivedKey) {
		if (err) {
		    return fn(err);
		}
		fn(err, derivedKey.toString('base64'));
	});

	
}

function getUserData(Username, fn){

    //Get the User's hashed password and salt
    dynamo.getItem({
                "TableName": "userData",
                "Key" : {
                    "Username": {"S": Username }
                }
            }, 
            function(err, data) {
                if (err) {
                    return fn(err);
                }
                else {
                    console.log("Data Found: " + data.Item);
                    fn(err, data.Item.Password.S, data.Item.Salt.S, data.Item.BearID.S);
                }
            });

}

exports.handler = function(event, context) {
    console.log(event);
	getUserData(event.Username, function(err, savedHash, savedSalt, BearID){
	    if(err){
	        context.fail('Error in getUserData: ' + err);
	        context.succeed({Success: false});
	    }
	    else{
	        console.log("okily dokily");
	        createHash(event.Password, savedSalt, function(err, newHash){
	            if(err){
	                console.log("Error in createHash: " + err);
	                context.succeed({Success: false});
	                context.fail(err);
	            }
	            else{
	                console.log("Neighborino");
	                 //Check the new has to the saved hash to see if the passwords match
	                if(savedHash == newHash){
	                    //If they do match get the token and ID and send it back to the app
	                    getCognitoInfo(event.Username, function(err, ID, Token){
	                        if(err){
	                            console.log("Error in getCognitoInfo: " + err);
	                            context.fail(err);
	                        }
	                        else{
	                            console.log("Login was succesful");
	                            context.succeed({
                                    Success: true,
                                    ID: ID,
                                    Token: Token,
                                    BearID: BearID
                                });
	                        }
	                    });
	                }
	                //If they do not match tell the app that the login was  failure
	                else{
	                    console.log("Gosh darn it! Am I that pre-diddly-ictable? ");
	                    context.succeed({
                            Success: false,
                            ID: "",
                            Token: "",
                            BearID: ""
                        });
	                }
	            }
	        });
	    }
	});
};