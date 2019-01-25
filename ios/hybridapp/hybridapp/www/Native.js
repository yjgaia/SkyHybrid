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

			window.webkit.messageHandlers.purchase.postMessage({
				productId : productId,
				errorHandlerName : registerCallback(errorHandler),
				cancelHandlerName : registerCallback(cancelHandler),
				callbackName : registerCallback(callback)
			});
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

			window.webkit.messageHandlers.consumePurchase.postMessage({
				productId : productId,
				errorHandlerName : registerCallback(errorHandler),
				callbackName : registerCallback(callback)
			});
		};
		
		let openURL = self.openURL = (url) => {
			//REQUIRED: url
			
			window.webkit.messageHandlers.openURL.postMessage(url);
		};
		
		let restorePurchase = self.restorePurchase = (productId, callbackOrHandlers) => {
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

			window.webkit.messageHandlers.restorePurchase.postMessage({
				productId : productId,
				errorHandlerName : registerCallback(errorHandler),
				callbackName : registerCallback(callback)
			});
		};
		
		let playBGM = self.playBGM = (path) => {
			//REQUIRED: path
			
			if (path.indexOf('.') !== -1) {
				path = path.substring(0, path.indexOf('.'));
			}

			window.webkit.messageHandlers.playBGM.postMessage(path);
		};
		
		let pauseBGM = self.pauseBGM = (path) => {
			//REQUIRED: path
			
			if (path.indexOf('.') !== -1) {
				path = path.substring(0, path.indexOf('.'));
			}
			
			window.webkit.messageHandlers.pauseBGM.postMessage(path);
		};
		
		let stopBGM = self.stopBGM = (path) => {
			//REQUIRED: path
			
			if (path.indexOf('.') !== -1) {
				path = path.substring(0, path.indexOf('.'));
			}
			
			window.webkit.messageHandlers.stopBGM.postMessage(path);
		};
		
		let setBGMVolume = self.setBGMVolume = (params) => {
			//REQUIRED: params
			//REQUIRED: params.path
			//REQUIRED: params.volume

			let path = params.path;
			let volume = params.volume;
			
			if (path.indexOf('.') !== -1) {
				path = path.substring(0, path.indexOf('.'));
			}
			
			window.webkit.messageHandlers.setBGMVolume.postMessage({
				path : path,
				volume : volume
			});
		};
	}
});
