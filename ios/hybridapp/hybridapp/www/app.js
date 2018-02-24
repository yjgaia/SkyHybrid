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
	
	let productId;
	
	Native.initPurchaseService((dataSet) => {
		
		if (dataSet.length > 0) {
			productId = dataSet[0].productId;
			
			consumeButton.empty();
			consumeButton.append('결제 Consume (' + productId + ')');
		}
		
		alert('Consume 되지 않은 결제 기록: ' + JSON.stringify(dataSet));
	});
	
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
						productId = data.productId;
						
						consumeButton.empty();
						consumeButton.append('결제 Consume (' + productId + ')');
						
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
				
				Native.consumePurchase(productId, {
					error : () => {
						alert('Consume할 수 없습니다. 인터넷 연결을 확인해 주시기 바랍니다.');
					},
					success : (dataSet) => {
						
						consumeButton.empty();
						consumeButton.append('결제 Consume');
						
						alert('Consume 완료');

						productId = undefined;
					}
				});
			}
		}
	}).appendTo(BODY);
});
