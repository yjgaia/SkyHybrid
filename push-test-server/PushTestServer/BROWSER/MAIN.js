PushTestServer.MAIN = METHOD({
	
	run : () => {
		
		A({
			style : {
				display : 'block',
				backgroundColor : '#333',
				color : '#fff',
				padding : 20,
				borderRadius : 10,
				textAlign : 'center',
				fontSize : 20,
				marginBottom : 10
			},
			c : '푸시 메시지 보내기',
			on : {
				tap : () => {
					
					let message = prompt('보낼 메시지를 입력해주세요.');
					
					if (message.trim() !== '') {
						
						POST({
							uri : 'send-push',
							params : {
								message : message
							}
						});
					}
				}
			}
		}).appendTo(BODY);
	}
});
