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
		
		__Native.init(CONFIG.isDevMode, registerCallback((data) => {
			
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
	}
});