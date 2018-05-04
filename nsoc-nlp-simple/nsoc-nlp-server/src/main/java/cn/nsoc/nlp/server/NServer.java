package cn.nsoc.nlp.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.management.ManagementFactory;

public class NServer {
    private static final Logger LOGGER = LoggerFactory.getLogger("nsoc nlp server");
    private volatile boolean isRunning = false;
    private int port = 50051;
    private Server server;

    public void start(String... args) throws IOException, InterruptedException {
        if(args.length == 1) {
            port = Integer.parseInt(args[0]);
        }

        Runtime.getRuntime().addShutdownHook(new Thread(this::shutdown));

        String name = ManagementFactory.getRuntimeMXBean().getName();
        String pid = name.split("@")[0];
        LOGGER.debug("Name: %s Pid: %s", name, pid);

        server = ServerBuilder.forPort(port).addService(new Greeter()).build().start();
        isRunning = true;

        server.awaitTermination();
        LOGGER.info("final event");
    }

    public void shutdown() {
        LOGGER.info("shutdown event begin");
        if (server != null) {
            server.shutdown();
        }
        isRunning = false;
        try {
            Thread.sleep(1000);
        } catch (InterruptedException exp) {
            exp.printStackTrace();
        }
        LOGGER.info("shutdown event close");
    }

    public static void main(String... args) {
        NServer server = new NServer();
        try {
            server.start("50051");
        } catch (IOException | InterruptedException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }
}
