global.Native = OBJECT({

	init : (inner, self) => {

		let callbackCount = 0;
		let registerCallback = (callback) => {
			let callbackId = '__CALLBACK_' + callbackCount;
			global[callbackId] = callback;
			callbackCount += 1;
			return callbackId;
		};
		
		let pushKey;
		let registerPushKeyHandler;

		__Native.init(CONFIG.isDevMode, registerCallback((data) => {
			
			pushKey = data.pushKey;
			
			if (registerPushKeyHandler !== undefined) {
				registerPushKeyHandler(pushKey);
			}
			
		}), CONFIG.unityAdsGameId);

		let setRegisterPushKeyHandler = self.setRegisterPushKeyHandler = (handler) => {
			//REQUIRED: handler

			if (pushKey !== undefined) {
				handler(pushKey);
			} else {
				registerPushKeyHandler = handler;
			}
		};

		let removePushKey = self.removePushKey = () => {
			__Native.removePushKey();
		};

		let generateNewPushKey = self.generateNewPushKey = () => {
			__Native.generateNewPushKey();
		};

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
			//OPTIONAL: handlers.cancel
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let cancelHandler = handlers.cancel;
			let callback = handlers.success;

			__Native.purchase(skuId, registerCallback(errorHandler), registerCallback(cancelHandler), registerCallback(callback));
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

		let loginGameService = self.loginGameService = (handlers) => {
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;
			
			__Native.loginGameService(registerCallback(errorHandler), registerCallback(callback));
		};
		
		let logoutGameService = self.logoutGameService = () => {
			
		};
		
		let showAchievements = self.showAchievements = () => {
			__Native.showAchievements();
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