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

		let initPurchaseService = self.initPurchaseService = (handlers) => {
			//REQUIRED: handlers
			//OPTIONAL: handlers.purchaseError
			//OPTIONAL: handlers.purchaseCancel
			//REQUIRED: handlers.purchaseSuccess

			let purchaseErrorHandler = handlers.purchaseError;
			let purchaseCancelHandler = handlers.purchaseCancel;
			let purchaseSuccessHandler = handlers.purchaseSuccess;

			__Native.initPurchaseService(registerCallback(purchaseErrorHandler), registerCallback(purchaseCancelHandler), registerCallback(purchaseSuccessHandler));
		};

		let loadPurchased = self.loadPurchased = (handlers) => {
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;

			__Native.loadPurchased(registerCallback(errorHandler), registerCallback(callback));
		};

		let requestPurchase = self.requestPurchase = (skuId) => {
			//REQUIRED: skuId

			__Native.requestPurchase(skuId);
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
		
		let logoutGameService = self.logoutGameService = (callback) => {
			//REQUIRED: callback
			
			__Native.logoutGameService(registerCallback(callback));
		};
		
		let showAchievements = self.showAchievements = (errorHandler) => {
			//REQUIRED: errorHandler
			
			__Native.showAchievements(registerCallback(errorHandler));
		};
		
		let unlockAchievement = self.unlockAchievement = (achievementId) => {
			//REQUIRED: achievementId
			
			__Native.unlockAchievement(achievementId);
		};
		
		let incrementAchievement = self.incrementAchievement = (achievementId) => {
			//REQUIRED: achievementId
			
			__Native.incrementAchievement(achievementId);
		};
		
		let showLeaderboards = self.showLeaderboards = (leaderboardId, errorHandler) => {
			//REQUIRED: leaderboardId
			//REQUIRED: errorHandler
			
			__Native.showLeaderboards(leaderboardId, registerCallback(errorHandler));
		};
		
		let updateLeaderboardScore = self.updateLeaderboardScore = (leaderboardId, score) => {
			//REQUIRED: leaderboardId
			//REQUIRED: score
			
			__Native.updateLeaderboardScore(leaderboardId, score);
		};
	}
});