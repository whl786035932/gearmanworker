package cn.videoworks.gearman.dto;

import java.util.ArrayList;
import java.util.List;

public class StorageRemoveDto {

	private Long contentId;
	
	private Boolean flag;
	
	private List<StorageRequestDto> storageRequestDto = new ArrayList<StorageRequestDto>();

	public Long getContentId() {
		return contentId;
	}

	public void setContentId(Long contentId) {
		this.contentId = contentId;
	}

	public List<StorageRequestDto> getStorageRequestDto() {
		return storageRequestDto;
	}

	public void setStorageRequestDto(List<StorageRequestDto> storageRequestDto) {
		this.storageRequestDto = storageRequestDto;
	}

	public Boolean getFlag() {
		return flag;
	}

	public void setFlag(Boolean flag) {
		this.flag = flag;
	}

	
}
