package cn.nsoc.nlp.client;

import junit.framework.TestCase;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class NClientTest extends TestCase{
    private NClient client;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        client = new NClient("127.0.0.1", 50051);
    }

    public void testExtract() {
        String text = "【观察者网综合报道】日前遭“前友邦”多米尼加共和国“闪电分手”，台湾当局依旧毫无悔改之意。“行政院长”赖清德近日继续叫嚣“中国吞并台湾，盼朝野团结”。不过此话立即被马英九怼回，后者告诫赖清德“别一天到晚强调自己是‘台独’，台湾要先解决九二共识问题，只出一张嘴，一点用都没有。”";
        client.extractKeyWord(text, 3).forEach(System.out::println);
        client.extract(text).forEach((key, value) -> System.out.println(key + "  " + value));
    }

    public void testFeedBack() {
        String text = "东北东北松花江";
        Map<String, Double> ret = client.extract(text);
        ret.forEach((key, value) -> System.out.println(key + "  " + value));
        Map<String, Double> feedMap = new HashMap<>();
        ret.entrySet().stream().limit(2).forEach(v -> feedMap.put(v.getKey(), v.getValue()));
        client.feedBack(feedMap).forEach((k, v) -> System.out.println(k + " " + v));

    }

    public void testSort() {
        Map<String, Double> map = new HashMap<>();
        map.put("qaa", 1.002);
        map.put("qab", 1.004);
        map.put("qac", 1.001);
        map.put("qad", 1.008);

        Map<String, Double> sortMap = new LinkedHashMap<>();
        map.entrySet().stream()
                .sorted((v1, v2) -> Double.compare(v2.getValue(), v1.getValue()))
                .forEach(v -> sortMap.put(v.getKey(), v.getValue()));
        sortMap.forEach((k, v) -> System.out.println(k + "  " + v));
    }

    public void testLog() {
        System.out.println(Math.log(0.99999));
    }

    public void testEquals() {
        String s1 = "addsb";
        String s2 = "sasdd";

        System.out.println(equals(s1, s2));

    }

    private boolean equals(String s1, String s2) {
        if(s1.length() != s2.length()) {
            return false;
        }

        for(int i = 0; i < s1.length(); i ++) {
            String s1V = s1.charAt(i) + "";
            if(!s2.contains(s1V)) {
                return false;
            } else {
                s2 = s2.replaceFirst(s1V, "");
            }
        }

        return true;
    }
}
