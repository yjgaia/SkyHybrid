RUN(() => {
	INIT_OBJECTS();
	
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
	
	A({
		style : buttonStyle,
		c : '유니티 광고 보기',
		on : {
			tap : () => {
				
				RUN((retry) => {
					
					Native.showUnityAd({
						error : () => {
							alert('유니티 광고를 볼 수 없습니다. 인터넷 연결을 확인해 주시기 바랍니다.');
							
							retry();
						},
						success : () => {
							alert('유니티 광고 시청 완료');
						}
					});
				});
			}
		}
	}).appendTo(BODY);
});