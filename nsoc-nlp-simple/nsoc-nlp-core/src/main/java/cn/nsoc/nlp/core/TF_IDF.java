package cn.nsoc.nlp.core;

import cn.nsoc.nlp.core.utils.Cache4MapDB;
import org.apdplat.word.WordSegmenter;
import org.apdplat.word.segmentation.SegmentationAlgorithm;
import org.apdplat.word.segmentation.Word;
import org.mapdb.BTreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.math.BigDecimal;
import java.util.*;

public class TF_IDF {
    private final static Logger LOGGER = LoggerFactory.getLogger("TF_IDF");
    private Map<String, BigDecimal> wordFrequency;
    private double threshold = 0.00049;

    public TF_IDF() throws IOException {
        BufferedReader fBr = null;
        try {

            fBr = new BufferedReader(new InputStreamReader(
                    this.getClass().getClassLoader().getResourceAsStream("CorpusWordlist.csv")));
            Map<String, BigDecimal> wordF = new HashMap<>();
            fBr.readLine();
            String line;
            while ((line = fBr.readLine()) != null) {
                String[] em = line.split(",");
                wordF.put(em[1], new BigDecimal(em[3]));
            }

            fBr.close();
            LOGGER.info("Load common word bank. Words size: " + wordF.size());
            this.wordFrequency = wordF;
        } finally {
            if (fBr != null) {
                fBr.close();
            }
        }
    }

    public TF_IDF(Map<String, BigDecimal> wordFrequency) {
        this.wordFrequency = wordFrequency;

    }

    public TF_IDF(Map<String, BigDecimal> wordFrequency, double threshold) {
        this(wordFrequency);
        this.threshold = threshold;
    }

    public Map<String, BigDecimal> getTopic(String text) {
        Map<String, BigDecimal> topic = new TreeMap<>();
        Map<String, BigDecimal> m = calculateWordFrequency(text);
        for (String k : m.keySet()) {
            BigDecimal v = wordFrequency.get(k);
            if (v == null) {
                Double weight = Math.log(BigDecimal.ONE.divide(new BigDecimal(threshold), 10, BigDecimal.ROUND_HALF_UP).doubleValue());
                topic.put(k, m.get(k).multiply(new BigDecimal(weight)));
            } else {
                Double weight = Math.log(BigDecimal.ONE.divide(v, 10, BigDecimal.ROUND_HALF_UP).doubleValue());
                topic.put(k, m.get(k).multiply(new BigDecimal(weight)));
            }

        }

        return topic;
    }

    public Map<String, BigDecimal> getTopicWithDic(String text, String dicName) {
        Cache4MapDB db = Cache4MapDB.getDB(Cache4MapDB.USER_DIR, dicName);
        BTreeMap<String, BigDecimal> cacheMap = db.getTreeMap(dicName, 100000);
        Map<String, BigDecimal> m = getTopic(text);
        Map<String, BigDecimal> updateM = new HashMap<>();
        m.forEach((k, v) -> {
            BigDecimal cacheV = cacheMap.get(k);
            if(cacheV == null) {
                updateM.put(k, v);
            } else {
                updateM.put(k, v.multiply(new BigDecimal(Math.log(cacheV.doubleValue()))));
            }
        });

        return updateM;
    }

    public Map<String, BigDecimal> feedBackWithDic(Map<String, Double> words, String dicName) {
        Cache4MapDB db = Cache4MapDB.getDB(Cache4MapDB.USER_DIR, dicName);
        BTreeMap<String, BigDecimal> cacheMap = db.getTreeMap(dicName, 100000);
        Map<String, BigDecimal> newM = new HashMap<>();
        words.forEach((k, v) -> {
            BigDecimal cacheV = cacheMap.get(k);
            BigDecimal nv = new BigDecimal(v);
            if(cacheV == null) {
                BigDecimal wight = new BigDecimal(Math.E).add(nv);
                newM.put(k, new BigDecimal(Math.log(wight.doubleValue())).multiply(nv));
                cacheMap.put(k, wight);
            } else {
                BigDecimal wight = cacheV.add(nv);
                newM.put(k, new BigDecimal(Math.log(wight.doubleValue())).multiply(nv));
                cacheMap.put(k, wight);
            }
        });

        return newM;
    }

    private Map<String, BigDecimal> calculateWordFrequency(String text) {
        Map<String, BigDecimal> wf = new HashMap<>();
        List<Word> wordList = WordSegmenter.seg(text, SegmentationAlgorithm.MaximumMatching);

        for (Word word : wordList) {
            String k = word.getText();
            BigDecimal v = wf.get(k);

            if (v == null) {
                wf.put(k, BigDecimal.ONE);
            } else {
                wf.put(k, v.add(BigDecimal.ONE));
            }

        }

        BigDecimal wordCount = new BigDecimal(wordList.size());
        for (String key : wf.keySet()) {
            wf.put(key, wf.get(key).divide(wordCount, 10, BigDecimal.ROUND_HALF_UP));
        }

        return wf;
    }

    public Map<String, BigDecimal> sortMapByValue(Map<String, BigDecimal> oriMap, boolean asc) {
        if (oriMap == null || oriMap.isEmpty()) {
            return null;
        }
        Map<String, BigDecimal> sortedMap = new TreeMap<>();
        List<Map.Entry<String, BigDecimal>> entryList = new ArrayList<>(
                oriMap.entrySet());

        entryList.sort((o1, o2) -> {
            if (o1.getValue().doubleValue() - o2.getValue().doubleValue() > 0) {
                if(asc) return 1;
                return -1;
            } else if (o1.getValue().doubleValue() - o2.getValue().doubleValue() < 0) {
                if (asc) return -1;
                return 1;
            }
            return 0;
        });


        Iterator<Map.Entry<String, BigDecimal>> iter = entryList.iterator();
        Map.Entry<String, BigDecimal> tmpEntry;
        while (iter.hasNext()) {
            tmpEntry = iter.next();
            sortedMap.put(tmpEntry.getKey(), tmpEntry.getValue());
        }
        return sortedMap;
    }

    public static void main(String... args) {
        BufferedReader fBr;
        BufferedReader iBr;
        try {
            fBr = new BufferedReader(new InputStreamReader(new FileInputStream("/home/kyle/Work/pro/java/nsoc-nlp/src/main/resources/CorpusWordlist.csv")));
            Map<String, BigDecimal> wordF = new HashMap<>();
            String line = fBr.readLine();
            System.out.println(String.format("head: %s", line));
            while ((line = fBr.readLine()) != null) {
                String[] em = line.split(",");
                wordF.put(em[1], new BigDecimal(em[3]));
            }

            fBr.close();

            TF_IDF tfidf = new TF_IDF(wordF);
            iBr = new BufferedReader(new InputStreamReader(System.in));
            while (true) {
                System.out.println("输入要进行分词的句子:");
                try {
                    String inStr = iBr.readLine();
                    if ("exit".equals(inStr)) {
                        break;
                    }
                    Map<String, BigDecimal> ret = tfidf.getTopic(inStr);

                    ret.entrySet()
                            .stream()
                            .sorted((o1, o2) -> {
                                if (o1.getValue().doubleValue() - o2.getValue().doubleValue() > 0) {
                                    return -1;
                                } else if (o1.getValue().doubleValue() - o2.getValue().doubleValue() < 0) {
                                    return 1;
                                }
                                return 0;
                            })
                            .forEach(v -> System.out.println(String.format("%s | %s", v.getKey(), v.getValue().doubleValue())));

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }

            iBr.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
