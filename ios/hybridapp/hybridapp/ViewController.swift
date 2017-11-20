import UIKit
import WebKit

class ViewController: UIViewController, WKUIDelegate {
    
    @IBOutlet weak var webView: WKWebView!
    
    override func loadView() {
        let webConfiguration = WKWebViewConfiguration()
        webView = WKWebView(frame: .zero, configuration: webConfiguration)
        webView.uiDelegate = self
        view = webView
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
}
