package cn.nsoc.nlp.server;

import cn.nsoc.nlp.core.TF_IDF;
import cn.nsoc.nlp.message.*;
import io.grpc.stub.StreamObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class Greeter extends GreeterGrpc.GreeterImplBase {
    private static final Logger LOGGER = LoggerFactory.getLogger("nsoc nlp server");
    private TF_IDF tf_idf;

    public Greeter() {
        try {
            tf_idf = new TF_IDF();
            tf_idf.getTopic("Warm up");
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    @Override
    public void extract(ExtractRequest request, StreamObserver<ExtractReply> responseObserver) {
        String text = request.getText();
        LOGGER.info(String.format("Request text: %s", text));
        Map<String, Double> ret = new TreeMap<>();
        tf_idf.getTopicWithDic(text, "test").forEach((k, v) -> ret.put(k, v.doubleValue()));
        responseObserver.onNext(ExtractReply.newBuilder().putAllScore(ret).build());
        responseObserver.onCompleted();
    }

    @Override
    public void feedback(FeedBackRequest request, StreamObserver<FeedBackReply> responseObserver) {
        Map<String, Double> words = request.getWordsMap();
        words.forEach((k, v) -> {
            LOGGER.info(String.format("Request word: %s %s", k, v));
        });
        Map<String, Double> ret = new TreeMap<>();
        tf_idf.feedBackWithDic(words, "test").forEach((k, v) -> ret.put(k, v.doubleValue()));
        responseObserver.onNext(FeedBackReply.newBuilder().putAllScore(ret).build());
        responseObserver.onCompleted();
    }
}
