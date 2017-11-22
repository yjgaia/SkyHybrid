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

		window.webkit.messageHandlers.init.postMessage({

			isDevMode : CONFIG.isDevMode,

			registerPushKeyHandlerName : registerCallback((_pushKey) => {
				
				pushKey = _pushKey;
				
				if (registerPushKeyHandler !== undefined) {
					registerPushKeyHandler(pushKey);
				}
			}),

			unityAdsGameId : CONFIG.unityAdsGameId,

			productIds : CONFIG.productIds
		});

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
			
			window.webkit.messageHandlers.initPurchaseService.postMessage(registerCallback(loadPurchasedHandler));
		};

		let purchase = self.purchase = (productId, handlers) => {
			//REQUIRED: productId
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//OPTIONAL: handlers.cancel
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let cancelHandler = handlers.cancel;
			let callback = handlers.success;

			window.webkit.messageHandlers.purchase.postMessage({
				productId : productId,
				errorHandlerName : registerCallback(errorHandler),
				cancelHandlerName : registerCallback(cancelHandler),
				callbackName : registerCallback(callback)
			});
		};
		
		let consumePurchase = self.consumePurchase = (purchaseToken, handlers) => {
			//REQUIRED: purchaseToken
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;

			window.webkit.messageHandlers.consumePurchase.postMessage({
				purchaseToken : purchaseToken,
				errorHandlerName : registerCallback(errorHandler),
				callbackName : registerCallback(callback)
			});
		};

		let showUnityAd = self.showUnityAd = (handlers) => {
			//REQUIRED: handlers
			//OPTIONAL: handlers.error
			//REQUIRED: handlers.success

			let errorHandler = handlers.error;
			let callback = handlers.success;
			
			window.webkit.messageHandlers.showUnityAd.postMessage({
				errorHandlerName : registerCallback(errorHandler),
				callbackName : registerCallback(callback)
			});
		};
	}
});
