package dependency.ranktext;
/**
 * 类说明   
 *
 * @author 郑元浩
 * @date 2017年10月18日 下午7:43:11 
 */

import dependency.bean.Dependency;
import org.slf4j.LoggerFactory;
import utils.Log;

import java.util.*;

public class RankText {

    private final org.slf4j.Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * 计算主题间的认知关系
     * @param termList 输入的主题列表（每个主题包含内容信息用于计算主题间的认知关系）
     * @param ClassName 领域名
     * @param MAX 认知关系数目设置上限
     * @return 主题间的认知关系
     */
    public List<Dependency> rankText(List<Term> termList, String ClassName, int MAX) {
        List<Dependency> dependencies = new ArrayList<>();

        logger.info("Finish Hash...");
        logger.info("Start computing the hammingDistance...");
        HashMap<TwoTuple<Term, Term>, Double> disMap = new HashMap<>();
        for (int i = 0; i < termList.size() - 1; i++) {
            for (int j = i + 1; j < termList.size(); j++) {
                Term term1 = termList.get(i);
                Term term2 = termList.get(j);
                if (term1.getTermText().length() == 0 || term2.getTermText().length() == 0 || term1.equals("") || term2.equals("")) {
                    Log.log("内容为空");
                } else {
                    double dis = CosineSimilarAlgorithm.getSimilarity(term1.getTermText(), term2.getTermText());
//				    logger.info(dis+"");
                    TwoTuple<Term, Term> twoTuple = new TwoTuple<>(term1, term2);
                    disMap.put(twoTuple, dis);
                }
            }
        }
        logger.info("Finish computing the hammingDistance...");
        logger.info("Start ranking...");

        List<Map.Entry<TwoTuple<Term, Term>, Double>> infoIds = new ArrayList<>(disMap.entrySet());
        Collections.sort(infoIds, (o1, o2) -> o2.getValue().compareTo(o1.getValue()));
        logger.info("Finish ranking!");
        logger.info("Start printing...");


        int end = MAX;
        if (infoIds.size() < end) end = infoIds.size();
        logger.info("end:" + end);
        for (int k = 0; k < end; k++) {
            TwoTuple<Term, Term> twoTuple = infoIds.get(k).getKey();
            String term1_term2 = twoTuple.first.getTermName() + "_" + twoTuple.second.getTermName();
            float dis = Float.parseFloat(infoIds.get(k).getValue().toString());
            System.out.println(term1_term2 + ": " + dis);
            if (new Float(dis).isNaN()) {
                dis = 0.5f;
            }
            System.out.println(dis);
            Dependency dependency = new Dependency(ClassName,
                    twoTuple.first.getTermName(),
                    twoTuple.first.getTermID(),
                    twoTuple.second.getTermName(),
                    twoTuple.second.getTermID(),
                    dis + "");
            dependencies.add(dependency);
        }
        logger.info("Finish printing...");
        return dependencies;

    }
}
