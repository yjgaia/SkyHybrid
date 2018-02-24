RUN(() => {
	INIT_OBJECTS();
	
	Native.setRegisterPushKeyHandler((pushKey) => {
		alert('푸시 키 : ' + pushKey);
		
		POST({
			url : 'http://192.168.0.7:8112/save-android-push-key',
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
					}
				});
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 게임 서비스 로그인',
		on : {
			tap : () => {
				
				Native.loginGameService({
					error : () => {
						alert('구글 게임 서비스 로그인에 실패하였습니다.');
					},
					success : () => {
						alert('로그인 성공!');
					}
				});
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 게임 서비스 로그아웃',
		on : {
			tap : () => {
				
				Native.logoutGameService(() => {
					alert('로그아웃 성공!');
				});
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 게임 업적 보기',
		on : {
			tap : () => {
				
				Native.showAchievements(() => {
					alert('구글 게임 업적을 불러올 수 없습니다.');
				});
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 게임 업적 등록',
		on : {
			tap : () => {
				
				Native.unlockAchievement('CgkIrs3f_ogNEAIQAg');
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 게임 업적 증가',
		on : {
			tap : () => {
				
				Native.incrementAchievement('CgkIrs3f_ogNEAIQAg');
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 리더보드 보기',
		on : {
			tap : () => {
				
				Native.showLeaderboards('CgkIrs3f_ogNEAIQAQ', () => {
					alert('구글 리더보드를 불러올 수 없습니다.');
				});
			}
		}
	}).appendTo(BODY);
	
	A({
		style : buttonStyle,
		c : '구글 리더보드 등록',
		on : {
			tap : () => {
				
				Native.updateLeaderboardScore('CgkIrs3f_ogNEAIQAQ', prompt('점수를 입력해 주십시오.'));
			}
		}
	}).appendTo(BODY);
});