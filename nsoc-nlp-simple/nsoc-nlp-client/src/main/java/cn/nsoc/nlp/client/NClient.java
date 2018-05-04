package cn.nsoc.nlp.client;

import cn.nsoc.nlp.message.*;
import io.grpc.ManagedChannel;
import io.grpc.netty.shaded.io.grpc.netty.NettyChannelBuilder;

import java.util.*;

public class NClient {
    private final ManagedChannel channel;
    private final GreeterGrpc.GreeterBlockingStub blockingStub;

    public NClient(String host, int port) {
        channel = NettyChannelBuilder.forAddress(host, port)
                .usePlaintext(true)
                .build();

        blockingStub = GreeterGrpc.newBlockingStub(channel);
    }

    public Map<String, Double> extract(String text) {
        ExtractRequest request = ExtractRequest.newBuilder().setText(text).build();
        ExtractReply reply = blockingStub.extract(request);
        Map<String, Double> sortMap = new LinkedHashMap<>();
        reply.getScoreMap().entrySet()
                .stream()
                .sorted((v1, v2) -> Double.compare(v2.getValue(), v1.getValue()))
                .forEach(v -> sortMap.put(v.getKey(), v.getValue()));
        return sortMap;
    }

    public Map<String, Double> extract(String text, int top) {
        Map<String, Double> topMap = new LinkedHashMap<>();
        extract(text).entrySet().stream().limit(top).forEach(v -> topMap.put(v.getKey(), v.getValue()));
        return topMap;
    }

    public Set<String> extractKeyWord(String text, int top) {
        return extract(text, top).keySet();
    }

    public Map<String, Double> feedBack(Map<String, Double> words) {
        FeedBackRequest request = FeedBackRequest.newBuilder().putAllWords(words).build();
        FeedBackReply reply = blockingStub.feedback(request);
        Map<String, Double> sortMap = new LinkedHashMap<>();
        reply.getScoreMap().entrySet()
                .stream()
                .sorted((v1, v2) -> Double.compare(v2.getValue(), v1.getValue()))
                .forEach(v -> sortMap.put(v.getKey(), v.getValue()));
        return sortMap;
    }

    public void shutdown() {
        channel.shutdown();
    }
}
