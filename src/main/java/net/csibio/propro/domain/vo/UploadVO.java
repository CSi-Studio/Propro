package net.csibio.propro.domain.vo;

import lombok.Data;

import java.util.Date;

/**
 * 文件上传VO
 *
 */
@Data
public class UploadVO {

	/***
	 * 文件id WU_FILE_0
	 * 
	 */
	private String id;
	/**
	 * 文件名称 Beyond Compare.rar
	 */
	private String name;
	/**
	 * 类型 application/octet-stream
	 */
	private String type;
	/**
	 * 文件大小
	 */
	private Long size;

	/**
	 * 最后修改时间
	 */
	private Date lastModifiedDate;

	/**
	 * 分片片数
	 */
	private Long chunks;

	/**
	 * 当前分片标识
	 */
	private Long chunk;

	/**
	 * 分片设置大小
	 */
	private Long chunkSize;
}
