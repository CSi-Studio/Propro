package com.csi.propro.domain.vo;

import lombok.Data;

/**
 * 
 * 文件MD5校验VO
 */
@Data
public class FileBlockVO {

	/**
	 * 文件名
	 */
	private String fileName;

	/**
	 * 当前分片下标
	 */
	private Long chunk;

	/**
	 * 文件大小（如果分片了，则是分片文件大小）
	 */
	private Long chunkSize;
}
