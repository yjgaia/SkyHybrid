PushTestServer.MAIN = METHOD({

	run : (addRequestListener) => {
		
		let pushKeyDB = PushTestServer.DB('PushKey');
		
		addRequestListener((requestInfo, response, replaceRootPath, next) => {

			let uri = requestInfo.uri;
			let params = requestInfo.params;
			
			if (uri === 'save-android-push-key') {
				
				pushKeyDB.create({
					androidKey : params.pushKey
				}, (savedData) => {
					
					response({
						content : JSON.stringify(savedData),
						contentType : 'application/json',
						headers : {
							'Access-Control-Allow-Origin' : '*'
						}
					});
				});
				
				return false;
			}

			if (uri === 'save-ios-push-key') {
				
				pushKeyDB.create({
					iosKey : params.pushKey
				}, (savedData) => {
					
					response({
						content : JSON.stringify(savedData),
						contentType : 'application/json',
						headers : {
							'Access-Control-Allow-Origin' : '*'
						}
					});
				});
				
				return false;
			}
			
			if (uri === 'send-push') {
				
				pushKeyDB.find({
					isFindAll : true
				}, EACH((savedData) => {
					
					if (savedData.androidKey !== undefined) {
						
						UPUSH.ANDROID_PUSH({
							regId : savedData.androidKey,
							data : {
								message : params.message
							}
						});
					}

					if (savedData.iosKey !== undefined) {
						
						UPUSH.IOS_PUSH({
							badge : 1,
							token : savedData.iosKey,
							sound : 'ping.aiff',
							message : params.message
						});
					}
				}));
				
				return false;
			}
		});
	}
});
