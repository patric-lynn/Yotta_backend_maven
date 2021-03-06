package utils;

import app.Config;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;

import java.util.concurrent.TimeUnit;

/**  
 * Selenium模拟浏览器爬取中文维基
 *  
 * @author 郑元浩 
 * @date 2016年11月26日
 */
@SuppressWarnings("deprecation")
public class SpiderUtils {
	
	/**
	 * 返回中文维基页面加载结果的HTML源码
	 * 拖动页面一次
	 * @param url 网页链接
	 * @return 网页源码
	 * @throws Exception
	 */
	public static String seleniumWikiCN(String url) throws Exception {
//		System.setProperty("webdriver.chrome.driver", Config.CHROME_PATH);
		System.setProperty("webdriver.ie.driver", Config.IE_PATH);
//		System.setProperty("phantomjs.binary.path", Config.PHANTOMJS_PATH);

//		WebDriver driver = new ChromeDriver();
		WebDriver driver = new InternetExplorerDriver();
//		WebDriver driver = new PhantomJSDriver();

		int m = 1;
		driver.manage().timeouts().pageLoadTimeout(1000, TimeUnit.SECONDS);
		while (m < 4) {
			try{
				driver.get(url);
			} catch (Exception e) {
				Log.log("第" + m + "次重载页面...");
				m++;
				driver.quit();

//				driver = new ChromeDriver();
				driver = new InternetExplorerDriver();
//				driver = new PhantomJSDriver();

				driver.manage().timeouts().pageLoadTimeout(1000, TimeUnit.SECONDS);
				continue;
			}
			break;
		}
		Log.log("Page title is: " + driver.getTitle());
		String html = driver.getPageSource();
		Thread.sleep(1000);
		driver.quit();
		return html;
	}
	
}
