package user.normal.dao;

public class Constant {

	private static String DBHost="192.168.23.1:8088";
	
	public static String getDBHost() {
		return DBHost;
	}
	public static void setDBHost(String dBHost) {
		DBHost = dBHost;
	}
	private static String lucenePath="G:\\eclipse_work\\";
	public static void setLucenePath(String lucenePath) {
		Constant.lucenePath = lucenePath;
	}
	public static String getLucenePath(){
		return lucenePath;
	}	
}
