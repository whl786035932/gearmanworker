package cn.videoworks.gearman.dto;

public class ImageDto {
	private Long id;
	private String width;// :50， //宽
	private String height;// :30， //高
	private String url;// http://xxx.jpg";//实际地址 //必填
	private String type;// 0:未知,1:海报,2:xxx //必填
	private String size;// 大小 byte //必填
	private String check_sum;// :"D78-DDSF-9D-8F" //校验码 例如md5等 //必填
	private String filename;// 文件名称
	private String ftpUrl;// ftpurl

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getWidth() {
		return width;
	}

	public void setWidth(String width) {
		this.width = width;
	}

	public String getHeight() {
		return height;
	}

	public void setHeight(String height) {
		this.height = height;
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

	public String getFtpUrl() {
		return ftpUrl;
	}

	public void setFtpUrl(String ftpUrl) {
		this.ftpUrl = ftpUrl;
	}
}
