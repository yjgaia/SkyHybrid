global.Native = OBJECT({

	init : (inner, self) => {

		let callbackCount = 0;
		let registerCallback = (callback) => {
			let callbackId = '__CALLBACK_' + callbackCount;
			global[callbackId] = callback;
			callbackCount += 1;
			return callbackId;
		};

		let purchase = self.purchase = () => {

		};
		
		let consumePurchase = self.consumePurchase = () => {

		};

		let showUnityAd = self.showUnityAd = (callback) => {
			__Native.showUnityAd(registerCallback(callback));
		};
	}
});