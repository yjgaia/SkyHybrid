global.Native = OBJECT({

	init : (inner, self) => {

		let callbackCount = 0;
		let registerCallback = (callback) => {
			
			if (callback === undefined) {
				callback = () => {};
			}
			
			let callbackId = '__CALLBACK_' + callbackCount;
			
			global[callbackId] = callback;
			
			callbackCount += 1;
			
			return callbackId;
		};
		
		let pushKey;
		let registerPushKeyHandler;
		
		__Native.init(CONFIG.isDevMode, CONFIG.androidPushChannelId, CONFIG.androidPushChannelTitle, registerCallback((data) => {
			
			pushKey = data.pushKey;
			
			if (registerPushKeyHandler !== undefined) {
				registerPushKeyHandler(pushKey);
			}
			
		}));

		let setRegisterPushKeyHandler = self.setRegisterPushKeyHandler = (handler) => {
			//OPTIONAL: handler
			
			if (handler === undefined) {
				handler = () => {};
			}

			if (pushKey !== undefined) {
				handler(pushKey);
			} else {
				registerPushKeyHandler = handler;
			}
		};

		let initPurchaseService = self.initPurchaseService = (loadPurchasedHandler) => {
			//REQUIRED: loadPurchasedHandler
			
			__Native.initPurchaseService(registerCallback(loadPurchasedHandler));
		};

		let purchase = self.purchase = (productId, callbackOrHandlers) => {
			//REQUIRED: productId
			//REQUIRED: callbackOrHandlers
			//OPTIONAL: callbackOrHandlers.error
			//OPTIONAL: callbackOrHandlers.cancel
			//REQUIRED: callbackOrHandlers.success

			let errorHandler;
			let cancelHandler;
			let callback;
			
			if (CHECK_IS_DATA(callbackOrHandlers) !== true) {
				callback = callbackOrHandlers;
			} else {
				errorHandler = callbackOrHandlers.error;
				cancelHandler = callbackOrHandlers.cancel;
				callback = callbackOrHandlers.success;
			}

			__Native.purchase(productId, registerCallback(errorHandler), registerCallback(cancelHandler), registerCallback(callback));
		};
		
		let consumePurchase = self.consumePurchase = (productId, callbackOrHandlers) => {
			//REQUIRED: productId
			//REQUIRED: callbackOrHandlers
			//OPTIONAL: callbackOrHandlers.error
			//REQUIRED: callbackOrHandlers.success

			let errorHandler;
			let callback;
			
			if (CHECK_IS_DATA(callbackOrHandlers) !== true) {
				callback = callbackOrHandlers;
			} else {
				errorHandler = callbackOrHandlers.error;
				callback = callbackOrHandlers.success;
			}

			__Native.consumePurchase(productId, registerCallback(errorHandler), registerCallback(callback));
		};

		let loginGameService = self.loginGameService = (callbackOrHandlers) => {
			//OPTIONAL: callbackOrHandlers
			//OPTIONAL: callbackOrHandlers.error
			//OPTIONAL: callbackOrHandlers.success

			let errorHandler;
			let callback;
			
			if (CHECK_IS_DATA(callbackOrHandlers) !== true) {
				callback = callbackOrHandlers;
			} else {
				errorHandler = callbackOrHandlers.error;
				callback = callbackOrHandlers.success;
			}
			
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