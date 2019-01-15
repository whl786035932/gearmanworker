package cn.videoworks.gearman.dto;

public class MovieDto {
	private Long id;
	private String url;// http://xxx.jpg";//实际地址 //必填
	private String type;// 1, //0:未知,1:正片,2:预告片 //必填
	private String size;// 23， //大小 kb //必填
	private String check_sum;// :"D78-DDSF-9D-8F" //校验码 例如md5等 //必填
	private String filename;// 文件名称
	private String duration;// 60, //时长 秒 //必填
	private Integer width;// 宽
	private Integer height;// 高
	private Long bitrate;// 码率
	private String ftpUlr;// ftpUrl

	public String getFtpUlr() {
		return ftpUlr;
	}

	public void setFtpUlr(String ftpUlr) {
		this.ftpUlr = ftpUlr;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getSize() {
		return size;
	}

	public void setSize(String size) {
		this.size = size;
	}

	public String getCheck_sum() {
		return check_sum;
	}

	public void setCheck_sum(String check_sum) {
		this.check_sum = check_sum;
	}

	public String getFilename() {
		return filename;
	}

	public void setFilename(String filename) {
		this.filename = filename;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getDuration() {
		return duration;
	}

	public void setDuration(String duration) {
		this.duration = duration;
	}

	public Integer getWidth() {
		return width;
	}

	public void setWidth(Integer width) {
		this.width = width;
	}

	public Integer getHeight() {
		return height;
	}

	public void setHeight(Integer height) {
		this.height = height;
	}

	public Long getBitrate() {
		return bitrate;
	}

	public void setBitrate(Long bitrate) {
		this.bitrate = bitrate;
	}
}
