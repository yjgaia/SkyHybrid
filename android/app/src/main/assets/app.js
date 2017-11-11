RUN(() => {

	INIT_OBJECTS();

	//Native.purchase('iap_test_item');

	DELAY(10, () => {
	    Native.showUnityAd((result) => {
	    	console.log(result);
	    });
	});
});