import UIKit
import WebKit
import StoreKit

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
        
        webView = WKWebView(frame: .zero, configuration: webViewConfig)
        webView.isOpaque = false
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
    }
    
    // 상단 상태 바 여백
    override func viewWillAppear(_ animated: Bool) {
        var bounds:CGRect = webView.bounds
        bounds.origin.y = UIApplication.shared.statusBarFrame.height;
        bounds.size.height = bounds.size.height - UIApplication.shared.statusBarFrame.height;
        webView.frame = bounds;
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
            UIApplication.shared.open(URL(string : message.body as! String)!, options: [:], completionHandler: { (status) in })
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
