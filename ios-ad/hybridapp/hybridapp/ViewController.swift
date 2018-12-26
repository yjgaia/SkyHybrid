import UIKit
import WebKit
import StoreKit
import UnityAds
import GoogleMobileAds

class ViewController: UIViewController,
    WKUIDelegate, WKNavigationDelegate, WKScriptMessageHandler,
    SKProductsRequestDelegate, SKPaymentTransactionObserver,
    UnityAdsDelegate,
    GADInterstitialDelegate,
    GADRewardBasedVideoAdDelegate {
    
    var webView: WKWebView!
    
    var products: [SKProduct]!
    
    var isDevMode: Bool!
    
    var showUnityAdsCallbackName: String!
    
    var adMobTestDeviceId: String!
    var adMobInterstitialAdId: String!
    var adMobInterstitial: GADInterstitial!
    var adMobRewardedVideoAdId: String!
    var showAdMobRewardedVideoCallbackName:String!
    
    var purchasedTransactions: [String:SKPaymentTransaction] = [:]
    var restorePurchaseProductId: String!
    
    var loadPurchasedHandlerName: String!
    var purchaseCancelHandlerName: String!
    var purchaseCallbackName: String!
    var restorePurchaseCallbackName: String!
    
    var registeredPushKey: String!
    var registerPushKeyHandlerName: String!
    
    override func loadView() {
        let webViewConfig = WKWebViewConfiguration()
        let webViewContentController = WKUserContentController()
        webViewConfig.userContentController = webViewContentController
        webViewConfig.mediaTypesRequiringUserActionForPlayback = []
        webViewConfig.preferences.setValue(true, forKey: "allowFileAccessFromFileURLs")
        
        webViewContentController.add(self, name: "init")
        
        // 푸시 관련
        webViewContentController.add(self, name: "removePushKey")
        webViewContentController.add(self, name: "generateNewPushKey")
        
        // 결제 관련
        webViewContentController.add(self, name: "initPurchaseService")
        webViewContentController.add(self, name: "purchase")
        webViewContentController.add(self, name: "consumePurchase")
        
        webViewContentController.add(self, name: "openURL")
        
        // 유니티 광고 관련
        webViewContentController.add(self, name: "showUnityAd")
        
        // 애드몹 광고 관련
        webViewContentController.add(self, name: "initAdMobInterstitialAd")
        webViewContentController.add(self, name: "showAdMobInterstitialAd")
        webViewContentController.add(self, name: "initAdMobRewardedVideoAd")
        webViewContentController.add(self, name: "showAdMobRewardedVideoAd")
        
        webView = WKWebView(frame: .zero, configuration: webViewConfig)
        webView.isOpaque = false
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
    }
    
    func webView(_ webView: WKWebView, runJavaScriptAlertPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping () -> Void) {
        
        let alertController = UIAlertController(title: message, message: nil, preferredStyle: .alert)
        
        alertController.addAction(UIAlertAction(title: "OK", style: .default) { _ in
            completionHandler()
        })
        
        DispatchQueue.main.async {
            self.present(alertController, animated: true, completion: nil)
        }
    }
    
    func webView(_ webView: WKWebView, runJavaScriptConfirmPanelWithMessage message: String, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (Bool) -> Void) {
        
        let alertController = UIAlertController(title: message, message: nil, preferredStyle: .alert)
        
        alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel) { _ in
            completionHandler(false)
        })
        alertController.addAction(UIAlertAction(title: "OK", style: .default) {  _ in
            completionHandler(true)
        })
        
        DispatchQueue.main.async {
            self.present(alertController, animated: true, completion: nil)
        }
    }
    
    func webView(_ webView: WKWebView, runJavaScriptTextInputPanelWithPrompt prompt: String, defaultText: String?, initiatedByFrame frame: WKFrameInfo, completionHandler: @escaping (String?) -> Void) {
        
        let alertController = UIAlertController(title: prompt, message: nil, preferredStyle: .alert)
        
        alertController.addTextField { (textField) in
            textField.text = defaultText
        }
        
        alertController.addAction(UIAlertAction(title: "Cancel", style: .cancel) { _ in
            completionHandler(nil)
        })
        alertController.addAction(UIAlertAction(title: "OK", style: .default) { _ in
            if let text = alertController.textFields?.first?.text {
                completionHandler(text)
            } else {
                completionHandler(defaultText)
            }
        })
        
        DispatchQueue.main.async {
            self.present(alertController, animated: true, completion: nil)
        }
    }
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        
        if (message.name == "init") {
            let data = message.body as! [String:AnyObject]
            
            if (data["isDevMode"] != nil) {
                isDevMode = data["isDevMode"] as! Bool
            }
            
            if (registeredPushKey == nil) {
                registerPushKeyHandlerName = data["registerPushKeyHandlerName"] as! String
            } else {
                webView.evaluateJavaScript(data["registerPushKeyHandlerName"] as! String + "('" + registeredPushKey + "')")
            }
            
            // 유니티 광고 초기화
            if (data["unityAdsGameId"] != nil) {
                UnityAds.initialize(data["unityAdsGameId"] as! String, delegate: self, testMode: isDevMode)
            }
            
            // 애드몹 광고 초기화
            if (data["adMobAppId"] != nil) {
                GADMobileAds.configure(withApplicationID: data["adMobAppId"] as! String)
                
                if (data["adMobTestDeviceId"] != nil) {
                    adMobTestDeviceId = data["adMobTestDeviceId"] as! String
                }
            }
            
            // 결제 초기화
            let request = SKProductsRequest(productIdentifiers: Set(data["productIds"] as! [String]))
            request.delegate = self
            request.start()
        }
        
        if (message.name == "initPurchaseService") {
            
            loadPurchasedHandlerName = message.body as! String
            
            SKPaymentQueue.default().add(self)
        }
        
        if (message.name == "purchase") {
            let data = message.body as! [String:AnyObject]
            
            let productId = data["productId"] as! String
            let errorHandlerName = data["errorHandlerName"] as! String
            
            purchaseCancelHandlerName = data["cancelHandlerName"] as! String
            purchaseCallbackName = data["callbackName"] as! String
            
            // 초기화가 아니므로 삭제
            loadPurchasedHandlerName = nil
            
            if (products == nil) {
                webView.evaluateJavaScript(errorHandlerName + "()")
            } else {
                for product in products {
                    if product.productIdentifier == productId {
                        SKPaymentQueue.default().add(SKPayment(product: product))
                    }
                }
            }
        }
        
        if (message.name == "consumePurchase") {
            let data = message.body as! [String:AnyObject]
            
            let productId = data["productId"] as? String
            let errorHandlerName = data["errorHandlerName"] as! String
            let callbackName = data["callbackName"] as! String
            
            if (productId == nil) {
                webView.evaluateJavaScript(errorHandlerName + "()")
            }
                
            else {
                let purchasedTransaction = purchasedTransactions[productId!]
                if (purchasedTransaction == nil) {
                    webView.evaluateJavaScript(errorHandlerName + "()")
                } else {
                    SKPaymentQueue.default().finishTransaction(purchasedTransaction!)
                    purchasedTransactions.removeValue(forKey: productId!)
                    webView.evaluateJavaScript(callbackName + "()")
                }
            }
        }
        
        if (message.name == "restorePurchase") {
            let data = message.body as! [String:AnyObject]
            
            let errorHandlerName = data["errorHandlerName"] as! String
            
            restorePurchaseProductId = data["productId"] as? String
            restorePurchaseCallbackName = data["callbackName"] as! String
            
            // 초기화가 아니므로 삭제
            loadPurchasedHandlerName = nil
            
            if (products == nil) {
                webView.evaluateJavaScript(errorHandlerName + "()")
            } else {
                SKPaymentQueue.default().restoreCompletedTransactions()
            }
        }
        
        if (message.name == "openURL") {
            UIApplication.shared.open(URL(string : message.body as! String)!, options: [:], completionHandler: { (status) in })
        }
        
        if (message.name == "showUnityAd") {
            let data = message.body as! [String:AnyObject]
            
            let errorHandlerName = data["errorHandlerName"] as! String
            showUnityAdsCallbackName = data["callbackName"] as! String
            
            if (UnityAds.isReady() == true) {
                UnityAds.show(self, placementId: "rewardedVideo")
            } else {
                webView.evaluateJavaScript(errorHandlerName + "()")
            }
        }
        
        if (message.name == "initAdMobInterstitialAd") {
            adMobInterstitialAdId = message.body as! String
            
            adMobInterstitial = GADInterstitial(adUnitID: adMobInterstitialAdId)
            adMobInterstitial.delegate = self
            
            let request = GADRequest()
            if (isDevMode == true) {
                request.testDevices = [adMobTestDeviceId]
            }
            adMobInterstitial.load(request)
        }
        
        if (message.name == "showAdMobInterstitialAd") {
            if (adMobInterstitial.isReady == true) {
                adMobInterstitial.present(fromRootViewController: self)
            }
        }
        
        if (message.name == "initAdMobRewardedVideoAd") {
            let data = message.body as! [String:AnyObject]
            
            adMobRewardedVideoAdId = data["adId"] as! String
            showAdMobRewardedVideoCallbackName = data["callbackName"] as! String
            
            GADRewardBasedVideoAd.sharedInstance().delegate = self
            
            let request = GADRequest()
            if (isDevMode == true) {
                request.testDevices = [adMobTestDeviceId]
            }
            GADRewardBasedVideoAd.sharedInstance().load(request, withAdUnitID: adMobRewardedVideoAdId)
        }
        
        if (message.name == "showAdMobRewardedVideoAd") {
            if (GADRewardBasedVideoAd.sharedInstance().isReady == true) {
                GADRewardBasedVideoAd.sharedInstance().present(fromRootViewController: self)
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        (UIApplication.shared.delegate as! AppDelegate).viewController = self
        
        // 화면 계속 켜진 상태로 유지
        UIApplication.shared.isIdleTimerDisabled = true
        
        // index.html 로딩
        if let path = Bundle.main.path(forResource: "index", ofType: "html", inDirectory: "www") {
            webView.load(URLRequest(url: URL(fileURLWithPath: path)))
        }
    }
    
    func registerPushKey(_ pushKey: String) {
        if (registerPushKeyHandlerName == nil) {
            registeredPushKey = pushKey
        } else {
            webView.evaluateJavaScript(registerPushKeyHandlerName + "('" + pushKey + "')")
        }
    }
    
    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        products = response.products;
    }
    
    func paymentQueue(_ queue: SKPaymentQueue, updatedTransactions transactions: [SKPaymentTransaction]) {
        
        var purchaseReceipt: String = "undefined"
        do {
            purchaseReceipt = "'" + (try Data(contentsOf: Bundle.main.appStoreReceiptURL!).base64EncodedString()) + "'"
        } catch {
            // ignore.
        }
        
        var purchasedInfoStr = ""
        
        for transaction in transactions {
            switch transaction.transactionState {
                
            case .purchased:
                
                let productId = transaction.payment.productIdentifier
                
                purchasedTransactions[productId] = transaction
                
                if (purchaseCallbackName != nil) {
                    webView.evaluateJavaScript(purchaseCallbackName + "({productId:'" + productId + "',purchaseReceipt:" + purchaseReceipt + "})")
                    purchaseCallbackName = nil
                }
                    
                else {
                    if (purchasedInfoStr != "") {
                        purchasedInfoStr += ","
                    }
                    purchasedInfoStr += "{productId:'" + productId + "',purchaseReceipt:" + purchaseReceipt + "}"
                }
                
                break
                
            case .failed:
                
                if (purchaseCancelHandlerName != nil) {
                    webView.evaluateJavaScript(purchaseCancelHandlerName + "()")
                    purchaseCancelHandlerName = nil
                }
                
                SKPaymentQueue.default().finishTransaction(transaction)
                
                break
                
            case .restored:
                
                let productId = transaction.payment.productIdentifier
                
                purchasedTransactions[productId] = transaction
                
                if (restorePurchaseCallbackName != nil) {
                    
                    if (restorePurchaseProductId == productId) {
                        webView.evaluateJavaScript(restorePurchaseCallbackName + "({productId:'" + productId + "',purchaseReceipt:" + purchaseReceipt + "})")
                    }
                    
                    restorePurchaseProductId = nil
                    restorePurchaseCallbackName = nil
                }
                
                break
                
            default:
                break
            }
        }
        
        if (loadPurchasedHandlerName != nil) {
            if (purchasedInfoStr != "") {
                webView.evaluateJavaScript(loadPurchasedHandlerName + "([" + purchasedInfoStr + "])")
            }
            loadPurchasedHandlerName = nil
        }
    }
    
    func unityAdsReady(_ placementId: String) { }
    
    func unityAdsDidStart(_ placementId: String) { }
    
    func unityAdsDidError(_ error: UnityAdsError, withMessage message: String) { }
    
    func unityAdsDidFinish(_ placementId: String, with state: UnityAdsFinishState) {
        webView.evaluateJavaScript(showUnityAdsCallbackName + "()")
    }
    
    func interstitialDidDismissScreen(_ ad: GADInterstitial) {
        adMobInterstitial = GADInterstitial(adUnitID: adMobInterstitialAdId)
        adMobInterstitial.delegate = self
        
        let request = GADRequest()
        if (isDevMode == true) {
            request.testDevices = [adMobTestDeviceId]
        }
        adMobInterstitial.load(request)
    }
    
    func rewardBasedVideoAd(_ rewardBasedVideoAd: GADRewardBasedVideoAd, didRewardUserWith reward: GADAdReward) {
        DispatchQueue.main.asyncAfter(deadline: .now() + 1) {
            self.webView.evaluateJavaScript(self.showAdMobRewardedVideoCallbackName + "()")
        }
    }
    
    func rewardBasedVideoAdDidClose(_ rewardBasedVideoAd: GADRewardBasedVideoAd) {
        let request = GADRequest()
        if (isDevMode == true) {
            request.testDevices = [adMobTestDeviceId]
        }
        GADRewardBasedVideoAd.sharedInstance().load(request, withAdUnitID: adMobRewardedVideoAdId)
    }
    
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        if navigationAction.targetFrame == nil, let url = navigationAction.request.url {
            if url.description.lowercased().range(of: "http://") != nil ||
                url.description.lowercased().range(of: "https://") != nil ||
                url.description.lowercased().range(of: "mailto:") != nil ||
                url.description.lowercased().range(of: "tel:") != nil {
                UIApplication.shared.open(url, options: [:], completionHandler: nil);
            }
        }
        return nil
    }
}
