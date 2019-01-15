package cn.videoworks.gearman.function;

import org.gearman.GearmanFunction;
import org.gearman.GearmanFunctionCallback;

public class Test implements GearmanFunction{

	@Override
	public byte[] work(String arg0, byte[] data, GearmanFunctionCallback callBack) throws Exception {
		
		for (int i = 0; i < 100; i++) {
			Thread.sleep(30);
		}
		String string = new String(data, "UTF-8");
		System.out.println("work接收消息："+string);
		return string.getBytes();
	}

}
