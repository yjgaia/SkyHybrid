import UIKit
import WebKit
import StoreKit
import AVFoundation

class ViewController: UIViewController,
    WKUIDelegate, WKNavigationDelegate, WKScriptMessageHandler,
    SKProductsRequestDelegate, SKPaymentTransactionObserver {
    
    var webView: WKWebView!
    
    var products: [SKProduct]!
    
    var purchasedTransactions: [String:SKPaymentTransaction] = [:]
    var restorePurchaseProductId: String!
    
    var loadPurchasedHandlerName: String!
    var purchaseCancelHandlerName: String!
    var purchaseCallbackName: String!
    var restorePurchaseCallbackName: String!
    
    var registeredPushKey: String!
    var registerPushKeyHandlerName: String!
    
    var audioPlayers: [String:AVAudioPlayer] = [:]
    var audioVolumes: [String:NSNumber] = [:]
    
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
        
        // BGM 관련
        webViewContentController.add(self, name: "playBGM")
        webViewContentController.add(self, name: "pauseBGM")
        webViewContentController.add(self, name: "stopBGM")
        webViewContentController.add(self, name: "setBGMVolume")
        
        webView = WKWebView(frame: .zero, configuration: webViewConfig)
        webView.isOpaque = false
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
    }
    
    // 상단 상태 바 여백
    override func viewWillAppear(_ animated: Bool) {
        var bounds:CGRect = webView.bounds
        bounds.origin.y = UIApplication.shared.statusBarFrame.height
        bounds.size.height = bounds.size.height - UIApplication.shared.statusBarFrame.height
        webView.frame = bounds
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
            
            if (registeredPushKey == nil) {
                registerPushKeyHandlerName = data["registerPushKeyHandlerName"] as! String
            } else {
                webView.evaluateJavaScript(data["registerPushKeyHandlerName"] as! String + "('" + registeredPushKey + "')")
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
            UIApplication.shared.open(URL(string : message.body as! String)!, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: { (status) in })
        }
        
        if (message.name == "playBGM") {
            
            let path = message.body as! String
            
            do {
                // 플레이어 생성
                if (audioPlayers[path] == nil) {
                    
                    if let fileURL = Bundle.main.path(forResource: "www/" + path, ofType: "mp3") {
                        
                        let player = try AVAudioPlayer(contentsOf: URL(fileURLWithPath: fileURL))
                        player.numberOfLoops = -1
                        player.play()
                        
                        // 볼륨 설정
                        if (audioVolumes[path] != nil) {
                            player.volume = audioVolumes[path]!.floatValue
                        } else {
                            player.volume = 0.8
                        }
                        
                        audioPlayers[path] = player
                    }
                }
                    
                else {
                    audioPlayers[path]?.play()
                }
                
            } catch {
                // ignore.
            }
        }
        
        if (message.name == "pauseBGM") {
            
            let path = message.body as! String
            
            if (audioPlayers[path] != nil) {
                audioPlayers[path]?.pause()
            }
        }
        
        if (message.name == "stopBGM") {
            
            let path = message.body as! String
            
            if (audioPlayers[path] != nil) {
                audioPlayers[path]?.stop()
                audioPlayers[path]?.currentTime = 0
            }
        }
        
        if (message.name == "setBGMVolume") {
            
            let data = message.body as! [String:AnyObject]
            
            let path = data["path"] as! String
            let volume = data["volume"] as! NSNumber
            
            if (audioPlayers[path] != nil) {
                audioPlayers[path]?.volume = volume.floatValue
            }
            
            audioVolumes[path] = volume
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        (UIApplication.shared.delegate as! AppDelegate).viewController = self
        
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
        products = response.products
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
    
    func webView(_ webView: WKWebView, createWebViewWith configuration: WKWebViewConfiguration, for navigationAction: WKNavigationAction, windowFeatures: WKWindowFeatures) -> WKWebView? {
        if navigationAction.targetFrame == nil, let url = navigationAction.request.url {
            if url.description.lowercased().range(of: "http://") != nil ||
                url.description.lowercased().range(of: "https://") != nil ||
                url.description.lowercased().range(of: "mailto:") != nil ||
                url.description.lowercased().range(of: "tel:") != nil {
                UIApplication.shared.open(url, options: convertToUIApplicationOpenExternalURLOptionsKeyDictionary([:]), completionHandler: nil)
            }
        }
        return nil
    }
    
    func webViewWebContentProcessDidTerminate(_ webView: WKWebView) {
        webView.reload()
    }
}

// Helper function inserted by Swift 4.2 migrator.
fileprivate func convertToUIApplicationOpenExternalURLOptionsKeyDictionary(_ input: [String: Any]) -> [UIApplication.OpenExternalURLOptionsKey: Any] {
	return Dictionary(uniqueKeysWithValues: input.map { key, value in (UIApplication.OpenExternalURLOptionsKey(rawValue: key), value)})
}
