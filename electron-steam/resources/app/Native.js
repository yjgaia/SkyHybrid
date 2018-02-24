global.Native = OBJECT({

	init : (inner, self) => {

		let greenworks;

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
			
			greenworks = require('./greenworks');
			
			try {
				greenworks.init();
				
				if (callback !== undefined) {
					callback();
				}
			}
			
			catch(e) {
				
				greenworks = undefined;
				
				if (errorHandler !== undefined) {
					errorHandler(e.toString());
				} else {
					SHOW_ERROR('HybridApp', e.toString());
				}
			}
		};
		
		let showAchievements = self.showAchievements = (errorHandler) => {
			//REQUIRED: errorHandler
			
			if (greenworks !== undefined) {
				greenworks.activateGameOverlay("Achievements");
			} else {
				errorHandler();
			}
		};
		
		let unlockAchievement = self.unlockAchievement = (achievementId) => {
			//REQUIRED: achievementId
			
			if (greenworks !== undefined) {
				greenworks.activateAchievement(achievementId, () => {});
			}
		};
	}
});