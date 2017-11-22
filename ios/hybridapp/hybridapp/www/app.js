RUN(() => {
	INIT_OBJECTS();
	
	Native.setRegisterPushKeyHandler((pushKey) => {
		alert('푸시 키 : ' + pushKey);
		
		POST({
			url : 'http://192.168.0.9:8112/save-ios-push-key',
			params : {
				pushKey : pushKey
			}
		}, {
			error : () => {
				alert('푸시 키를 서버에 저장할 수 없습니다.');
			},
			success : (result) => {
				alert('푸시 키 서버 저장 완료: ' + result);
			}
		});
	});
	
	let buttonStyle = {
		display : 'block',
		backgroundColor : '#333',
		color : '#fff',
		padding : 20,
		borderRadius : 10,
		textAlign : 'center',
		fontSize : 20,
		marginBottom : 10
	};
	
	let purchaseToken;
	
	/*Native.initPurchaseService((dataSet) => {
		
		if (dataSet.length > 0) {
			purchaseToken = dataSet[0].purchaseToken;
			
			consumeButton.empty();
			consumeButton.append('결제 Consume (' + purchaseToken + ')');
		}
		
		alert('Consume 되지 않은 결제 기록: ' + JSON.stringify(dataSet));
	});*/
	
	A({
		style : buttonStyle,
		c : '결제 테스트 (iap_test_item)',
		on : {
			tap : () => {
				
				Native.purchase('iap_test_item', {
					error : () => {
						alert(MSG({
							ko : '결제를 진행할 수 없습니다. 인터넷 연결을 확인해 주시기 바랍니다.'
						}));
					},
					cancel : () => {
						alert(MSG({
							ko : '결제를 취소하였습니다.'
						}));
					},
					success : (data) => {
						purchaseToken = data.purchaseToken;
						
						consumeButton.empty();
						consumeButton.append('결제 Consume (' + purchaseToken + ')');
						
						alert('결제 완료: ' + JSON.stringify(data));
					}
				});
			}
		}
	}).appendTo(BODY);
	
	let consumeButton = A({
		style : buttonStyle,
		c : '결제 Consume',
		on : {
			tap : () => {
				
				Native.consumePurchase(purchaseToken, {
					error : () => {
						alert('Consume할 수 없습니다. 인터넷 연결을 확인해 주시기 바랍니다.');
					},
					success : (dataSet) => {
						
						consumeButton.empty();
						consumeButton.append('결제 Consume');
						
						alert('Consume 완료');
					}
				});
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '유니티 광고 보기',
		on : {
			tap : () => {
				
				RUN((retry) => {
					
					Native.showUnityAd({
						error : () => {
							alert(MSG({
								ko : '유니티 광고를 재생할 수 없습니다. 인터넷 연결을 확인해 주시기 바랍니다.'
							}));
							
							retry();
						},
						success : () => {
							alert(MSG({
								ko : '유니티 광고 시청 완료'
							}));
						}
					});
				});
			}
		}
	}).appendTo(BODY);
});
