require(process.env.UPPERCASE_PATH + '/LOAD.js');

BOOT({
	CONFIG : {
		defaultBoxName : 'PushTestServer',
		isDevMode : true,
		webServerPort : 8112
	},
	NODE_CONFIG : {
		// 테스트 목적이기 때문에 CPU 클러스터링 기능을 사용하지 않습니다.
		isNotUsingCPUClustering : true,
		
		dbName : 'PushTestServer',
		
		UPUSH : {
			Android : {
				serverKey : 'AAAAeSh5kOE:APA91bEYMqkN4W3GQ7Wed4bs7VW6LH6p-l9VOkN7PWnWjklMCXYW_JZ4wTELHJFEEcz1GgaIxnzg1iQ1iZBxfQ1YHPlv1wDUYqa-QRGc9vkiTBXz3JasXaQ6c_xXuVp5G4a2jZSMxs4Y'
			},
			IOS : {
				certFilePath : 'cert.pem',
				keyFilePath : 'key.pem',
				password : '{{비밀번호}}'
			}
		}
	}
});
