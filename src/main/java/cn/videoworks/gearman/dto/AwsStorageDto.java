package cn.videoworks.gearman.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class AwsStorageDto {

	private Long id;
	private String url;
//	@JsonIgnore
	private String check_sum;
	private Integer type;
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
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
	
	
}
