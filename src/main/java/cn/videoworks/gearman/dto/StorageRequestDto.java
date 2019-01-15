package cn.videoworks.gearman.dto;

public class StorageRequestDto {

	private Long id;
	private String url;
	private String check_sum;
	private int type;
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public String getUrl() {
		return url;
	}
	public void setUrl(String url) {
		this.url = url;
	}
	public String getCheck_sum() {
		return check_sum;
	}
	public void setCheck_sum(String check_sum) {
		this.check_sum = check_sum;
	}
	public int getType() {
		return type;
	}
	public void setType(int type) {
		this.type = type;
	}
	
}
