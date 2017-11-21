import UIKit
import WebKit
import StoreKit
import UnityAds

class ViewController: UIViewController,
    WKUIDelegate, WKNavigationDelegate, WKScriptMessageHandler,
    SKProductsRequestDelegate,
    UnityAdsDelegate {
    
    var webView: WKWebView!
    
    var products: [SKProduct]!
    
    override func loadView() {
        let webViewConfig = WKWebViewConfiguration()
        let webViewContentController = WKUserContentController()
        webViewConfig.userContentController = webViewContentController
        webViewContentController.add(self, name: "init")
        webViewContentController.add(self, name: "purchase")
        
        webView = WKWebView(frame: .zero, configuration: webViewConfig)
        webView.uiDelegate = self
        webView.navigationDelegate = self
        view = webView
    }
    
    func userContentController(_ userContentController: WKUserContentController, didReceive message: WKScriptMessage) {
        
        if (message.name == "init") {
            let data = message.body as! [String:AnyObject]
            
            let isDevMode = data["isDevMode"] as! Bool
            let registerPushKeyHandlerName = data["registerPushKeyHandlerName"] as! String
            let unityAdsGameId = data["unityAdsGameId"] as! String
            
            let request = SKProductsRequest(productIdentifiers: Set(data["productIds"] as! [String]))
            request.delegate = self
            request.start()
        }
        
        if (message.name == "purchase") {
            let data = message.body as! [String:AnyObject]
            
            let productId = data["productId"] as! String
            let errorHandlerName = data["errorHandlerName"] as! String
            let cancelHandlerName = data["errorHandlerName"] as! String
            let callbackName = data["errorHandlerName"] as! String
            
            if (products == nil) {
                //TOOD:
            } else {
                for product in products {
                    if product.productIdentifier == productId {
                        SKPaymentQueue.default().add(SKPayment(product: product))
                    }
                }
            }
        }
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        // 화면 계속 켜진 상태로 유지
        UIApplication.shared.isIdleTimerDisabled = true
        
        // index.html 로딩
        let baseURL = Bundle.main.url(forResource: "www", withExtension: nil)!
        let indexURL = baseURL.appendingPathComponent("index.html")
        let htmlString = try? String(contentsOf: indexURL, encoding: String.Encoding.utf8)
        webView.loadHTMLString(htmlString!, baseURL: baseURL)
    }
    
    func productsRequest(_ request: SKProductsRequest, didReceive response: SKProductsResponse) {
        products = response.products;
    }
    
    func unityAdsReady(_ placementId: String) { }
    
    func unityAdsDidStart(_ placementId: String) { }
    
    func unityAdsDidError(_ error: UnityAdsError, withMessage message: String) { }
    
    func unityAdsDidFinish(_ placementId: String, with state: UnityAdsFinishState) { }
}
