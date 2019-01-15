package cn.videoworks.gearman.enumeration;

public enum ContentType {

	VIDEO(1,"视频"),
	POSTER(2,"海报"),
	AD(3,"广告"),
	ICON(4,"图标");
	private int value;
	private String name;
	public int getValue() {
		return value;
	}
	public void setValue(int value) {
		this.value = value;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	private ContentType(int value, String name){
		this.value=value;
		this.name=name;
	}
}
