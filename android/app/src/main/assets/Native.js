global.Native = OBJECT({

	init : (inner, self) => {

		let callbackCount = 0;
		let registerCallback = (callback) => {
			let callbackId = '__CALLBACK_' + callbackCount;
			global[callbackId] = callback;
			callbackCount += 1;
			return callbackId;
		};

		__Native.init(CONFIG.isDevMode, CONFIG.unityAdsGameId);

		let loadPurchased = self.loadPurchased = (handlers) => {
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;

			__Native.loadPurchased(registerCallback(errorHandler), registerCallback(callback));
		};

		let purchase = self.purchase = (skuId, handlers) => {
			//REQUIRED: skuId
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;

			__Native.purchase(skuId, registerCallback(errorHandler), registerCallback(callback));
		};
		
		let consumePurchase = self.consumePurchase = (purchaseToken, handlers) => {
			//REQUIRED: purchaseToken
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;

			__Native.consumePurchase(purchaseToken, registerCallback(errorHandler), registerCallback(callback));
		};

		let showUnityAd = self.showUnityAd = (handlers) => {
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;
			
			__Native.showUnityAd(registerCallback(errorHandler), registerCallback(callback));
		};

		let loginGameService = self.loginGameService = () => {
			
		};
		
		let logoutGameService = self.logoutGameService = () => {
			
		};
		
		let showAchievements = self.showAchievements = () => {
			
		};
		
		let unlockAchievement = self.unlockAchievement = () => {
			
		};
		
		let incrementAchievement = self.incrementAchievement = () => {
			
		};
		
		let showLeaderboards = self.showLeaderboards = () => {
			
		};
		
		let updateLeaderboardScore = self.updateLeaderboardScore = () => {
			
		};
	}
});