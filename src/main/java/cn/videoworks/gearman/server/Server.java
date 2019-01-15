package cn.videoworks.gearman.server;

import java.io.IOException;

import org.gearman.Gearman;
import org.gearman.GearmanServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
	
	private static Logger logger = LoggerFactory.getLogger(Server.class);

	public static final int ECHO_PORT = 4730;
	public static void main(String[] args){
		Gearman gearman = Gearman.createGearman();
        try {
            GearmanServer server = gearman.startGearmanServer(ECHO_PORT);
            logger.info("启动gearman server 服务器成功，端口【"+server.getPort()+"】");
        } catch (IOException ioe) {
            gearman.shutdown();
            logger.error("启动gearman server 服务器失败【"+ioe.getMessage()+"】");
            System.exit(-1);
        }
	}

}
