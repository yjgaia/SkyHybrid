PushTestServer.MAIN = METHOD({

	run : (addRequestListener) => {
		
		let pushKeyDB = PushTestServer.DB('PushKey');
		
		addRequestListener((requestInfo, response, replaceRootPath, next) => {

			let uri = requestInfo.uri;
			let params = requestInfo.params;
			
			if (uri === 'save-push-key') {
				
				pushKeyDB.create({
					pushKey : params.pushKey
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
		});
	}
});
