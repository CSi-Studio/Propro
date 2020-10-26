package com.csi.propro.component;

import com.csi.propro.constants.SymbolConst;
import com.csi.propro.constants.enums.ResultCode;
import com.csi.propro.domain.ResultDO;
import com.csi.propro.domain.vo.FileBlockVO;
import com.csi.propro.domain.vo.UploadVO;
import com.csi.propro.utils.RepositoryUtil;
import com.csi.propro.utils.FileUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Component("chunkUploader")
public class ChunkUploader {

    public final Logger logger = LoggerFactory.getLogger(ChunkUploader.class);

    /**
     * 上传之前校验(整个文件、分片)
     *
     * @param block
     * @return
     */
    public ResultDO check(FileBlockVO block, String projectName) {

        Long chunk = block.getChunk();
        String fileName = block.getFileName();
        Long chunkSize = block.getChunkSize();
        ResultDO result = new ResultDO(true);
        if (chunk != null) {
            String destFileDir = RepositoryUtil.getProjectTempRepo(projectName) + File.separator + fileName;
            String destFileName = chunk + SymbolConst.DELIMITER + fileName;
            String destFilePath = destFileDir + File.separator + destFileName;
            File destFile = new File(destFilePath);
            if (destFile.exists() && destFile.length() == chunkSize) {
                return ResultDO.buildError(ResultCode.FILE_CHUNK_ALREADY_EXISTED);
            } else {
                return result;
            }
        } else {
            return result;
        }
    }

    /**
     * 分片上传
     *
     * @param file
     * @param uploadVO
     * @return
     */
    public ResultDO chunkUpload(MultipartFile file, UploadVO uploadVO, String projectName) {
        String fileName = uploadVO.getName();
        Long chunk = uploadVO.getChunk();// 当前片
        Long chunks = uploadVO.getChunks();// 总共多少片

        // 分片目录创建
        String chunkDirPath = RepositoryUtil.getProjectTempRepo(projectName) + File.separator + fileName;
        File chunkDir = new File(chunkDirPath);
        if (!chunkDir.exists()) {
            chunkDir.mkdirs();
        }
        // 分片文件上传
        String chunkFileName = chunk + SymbolConst.DELIMITER + fileName;
        String chunkFilePath = chunkDir + File.separator + chunkFileName;
        File chunkFile = new File(chunkFilePath);
        try {
            if (chunkFile.length() != 0 && file.getSize() == chunkFile.length()) {

            } else {
                file.transferTo(chunkFile);
            }

        } catch (Exception e) {
            logger.error("分片上传出错", e);
            return ResultDO.buildError(ResultCode.FILE_CHUNK_UPLOAD_FAILED);
        }
        // 合并分片
        Long chunkSize = uploadVO.getChunkSize();
        long seek = 0;
        if (chunk != null) {
            seek = chunkSize * chunk;
        }
        String destFilePath = RepositoryUtil.getProjectRepo(projectName) + File.separator + fileName;
        File destFile = new File(destFilePath);
        if (chunkFile.length() > 0) {
            try {
                FileUtil.randomAccessFile(chunkFile, destFile, seek);
            } catch (IOException e) {
                logger.error("分片{}合并失败：{}", chunkFile.getName(), e.getMessage());
                return ResultDO.buildError(ResultCode.FILE_CHUNKS_MERGE_FAILED);
            }
        }
        if (chunk == null || chunk == chunks - 1) {
            FileUtil.deleteDirectory(chunkDirPath); // 删除分片文件夹
        }
        return new ResultDO<>(true);
    }
}
