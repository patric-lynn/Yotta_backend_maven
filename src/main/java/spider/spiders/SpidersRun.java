package spider.spiders;

import app.Config;
import domain.bean.Domain;
import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;
import spider.spiders.baiduzhidao.BaiduZhidaoProcessor;
import spider.spiders.csdn.CSDNProcessor;
import spider.spiders.wikicn.FragmentCrawler;
import spider.spiders.wikicn.MysqlReadWriteDAO;
import spider.spiders.wikicn.TopicCrawler;
import spider.spiders.zhihu.ZhihuProcessor;
import utils.DatabaseUtils;
import utils.Log;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * 一、中文维基爬虫
 * 1. 爬取领域术语
 * 2. 爬取领域术语下的知识碎片（等第一步完成才可以第二步）
 *
 * 二、百度知道、csdn、知乎爬虫（必须在中文维基把课程下的主题和分面爬取之后才能运行）
 * 1. 根据“主题+分面”进行碎片爬取
 *
 * @author yuanhao
 * @date 2017/12/19 22:02
 */
public class SpidersRun {

    public static void main(String[] args) throws Exception {
        // 如果数据库中表格不存在，先新建数据库表格
        DatabaseUtils.createTable();
        // 爬取多门课程
        String excelPath = SpidersRun.class.getClassLoader().getResource("").getPath() + "domains.xls";
        List<Domain> domainList = getDomainFromExcel(excelPath);
        for (int i = 0; i < domainList.size(); i++) {
            Domain domain = domainList.get(i);
            boolean hasSpidered = MysqlReadWriteDAO.judgeByClass(Config.DOMAIN_TABLE, domain.getClassName());
            // 如果domain表已经有这门课程，就不爬取这门课程的数据，没有就爬取
            if (!hasSpidered) {
                Log.log("domain表格没有这门课程，开始爬取课程：" + domain);
                constructKGByDomainName(domain);
                spiderFragment(domain);
            } else {
                Log.log("domain表格有这门课程，不需要爬取课程：" + domain);
            }
        }
    }

    /**
     * 爬取一门课程：主题、主题上下位关系、分面、分面关系、主题认知关系(gephi文件)、中文维基碎片
     * @param domain 课程
     */
    public static void constructKGByDomainName(Domain domain) throws Exception {
        String domainName = domain.getClassName();
        // 存储领域
        TopicCrawler.storeDomain(domain);
        // 存储主题
        TopicCrawler.storeTopic(domain);
        // 存储分面和碎片
        FragmentCrawler.storeKGByDomainName(domainName);
    }

    /**
     * 爬取一门课程：碎片（数据源为：百度知道、知乎、csdn）
     * @param domain 课程
     */
    public static void spiderFragment(Domain domain) {
        String domainName = domain.getClassName();

        //爬取百度知道
        if (!MysqlReadWriteDAO.judgeByClassAndSourceName(Config.ASSEMBLE_FRAGMENT_TABLE, domainName, "百度知道")) {
            BaiduZhidaoProcessor baiduZhidaoProcessor = new BaiduZhidaoProcessor();
            baiduZhidaoProcessor.baiduAnswerCrawl(domainName);
        } else {
            Log.log("数据已经爬取：" + domainName + "，百度知道");
        }

        //爬取知乎
        if (!MysqlReadWriteDAO.judgeByClassAndSourceName(Config.ASSEMBLE_FRAGMENT_TABLE, domainName, "知乎")) {
            ZhihuProcessor zhihuProcessor = new ZhihuProcessor();
            zhihuProcessor.zhihuAnswerCrawl(domainName);
        } else {
            Log.log("数据已经爬取：" + domainName + "，知乎");
        }

        //爬取CSDN
        if (!MysqlReadWriteDAO.judgeByClassAndSourceName(Config.ASSEMBLE_FRAGMENT_TABLE, domainName, "csdn")) {
            CSDNProcessor csdnProcessor = new CSDNProcessor();
            csdnProcessor.CSDNAnswerCrawl(domainName);
        } else {
            Log.log("数据已经爬取：" + domainName + "，csdn");
        }

    }

    /**
     * 读取本地excel文件，获取课程和对应的学科信息
     * @param excelPath 课程excel文件路径
     * @return 课程信息集合
     */
    public static List<Domain> getDomainFromExcel(String excelPath) {
        List<Domain> domains = new ArrayList<>();
        try {
            Workbook wb = Workbook.getWorkbook(new File(excelPath));
            Sheet st = wb.getSheet(0);
            int rows = st.getRows();
            for (int i = 1; i < rows; i++) {
                String subjectName = st.getCell(0, i).getContents();
                String domainName = st.getCell(1, i).getContents();
                Domain domain = new Domain();
                domain.setClassName(domainName);
                domain.setSubjectName(subjectName);
                domains.add(domain);
            }
        } catch (BiffException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return domains;
    }

}
