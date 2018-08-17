package com.westlake.air.pecs.service.impl;

import com.westlake.air.pecs.constants.ResultCode;
import com.westlake.air.pecs.dao.LibraryDAO;
import com.westlake.air.pecs.domain.ResultDO;
import com.westlake.air.pecs.domain.db.LibraryDO;
import com.westlake.air.pecs.domain.db.TaskDO;
import com.westlake.air.pecs.domain.query.LibraryQuery;
import com.westlake.air.pecs.domain.query.TransitionQuery;
import com.westlake.air.pecs.parser.TransitionTraMLParser;
import com.westlake.air.pecs.parser.TransitionTsvParser;
import com.westlake.air.pecs.service.LibraryService;
import com.westlake.air.pecs.service.TransitionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 09:45
 */
@Service("libraryService")
public class LibraryServiceImpl implements LibraryService {

    public final Logger logger = LoggerFactory.getLogger(LibraryServiceImpl.class);

    @Autowired
    LibraryDAO libraryDAO;
    @Autowired
    TransitionService transitionService;
    @Autowired
    TransitionTsvParser tsvParser;
    @Autowired
    TransitionTraMLParser traMLParser;

    @Override
    public List<LibraryDO> getAll() {
        return libraryDAO.getAll();
    }

    @Override
    public List<LibraryDO> getSimpleAll(Integer type) {
        return libraryDAO.getSimpleAll(type);
    }

    @Override
    public ResultDO<List<LibraryDO>> getList(LibraryQuery query) {
        List<LibraryDO> libraryDOS = libraryDAO.getList(query);
        long totalCount = libraryDAO.count(query);
        ResultDO<List<LibraryDO>> resultDO = new ResultDO<>(true);
        resultDO.setModel(libraryDOS);
        resultDO.setTotalNum(totalCount);
        resultDO.setPageSize(query.getPageSize());
        return resultDO;
    }

    @Override
    public ResultDO save(LibraryDO libraryDO) {
        if (libraryDO.getName() == null || libraryDO.getName().isEmpty()) {
            return ResultDO.buildError(ResultCode.LIBRARY_NAME_CANNOT_BE_EMPTY);
        }
        try {
            libraryDO.setCreateDate(new Date());
            libraryDO.setLastModifiedDate(new Date());
            libraryDAO.insert(libraryDO);
            return ResultDO.build(libraryDO);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            if (e.getMessage().contains("E11000")) {
                return ResultDO.buildError(ResultCode.DUPLICATE_KEY_ERROR);
            } else {
                return ResultDO.buildError(ResultCode.INSERT_ERROR);
            }
        }
    }

    @Override
    public ResultDO update(LibraryDO libraryDO) {
        if (libraryDO.getId() == null || libraryDO.getId().isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        if (libraryDO.getName() == null || libraryDO.getName().isEmpty()) {
            return ResultDO.buildError(ResultCode.LIBRARY_NAME_CANNOT_BE_EMPTY);
        }

        try {
            libraryDO.setLastModifiedDate(new Date());
            libraryDAO.update(libraryDO);
            return ResultDO.build(libraryDO);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.INSERT_ERROR);
        }
    }

    @Override
    public ResultDO delete(String id) {
        if (id == null || id.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }
        try {
            libraryDAO.delete(id);
            return new ResultDO(true);
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.DELETE_ERROR);
        }
    }

    @Override
    public ResultDO<LibraryDO> getById(String id) {
        if (id == null || id.isEmpty()) {
            return ResultDO.buildError(ResultCode.ID_CANNOT_BE_NULL_OR_ZERO);
        }

        try {
            LibraryDO libraryDO = libraryDAO.getById(id);
            if (libraryDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                return ResultDO.build(libraryDO);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public ResultDO<LibraryDO> getByName(String name) {
        if (name == null || name.isEmpty()) {
            return ResultDO.buildError(ResultCode.LIBRARY_NAME_CANNOT_BE_EMPTY);
        }

        try {
            LibraryDO libraryDO = libraryDAO.getByName(name);
            if (libraryDO == null) {
                return ResultDO.buildError(ResultCode.OBJECT_NOT_EXISTED);
            } else {
                return ResultDO.build(libraryDO);
            }
        } catch (Exception e) {
            logger.warn(e.getMessage());
            return ResultDO.buildError(ResultCode.QUERY_ERROR);
        }
    }

    @Override
    public ResultDO parseAndInsertTsv(LibraryDO library, InputStream in, String fileName, boolean justReal, TaskDO taskDO) {

        ResultDO resultDO;

        if (fileName.toLowerCase().endsWith("tsv") || fileName.toLowerCase().endsWith("csv")) {
            resultDO = tsvParser.parseAndInsert(in, library, justReal, taskDO);
        } else if (fileName.toLowerCase().endsWith("traml")) {
            resultDO = traMLParser.parseAndInsert(in, library, justReal, taskDO);
        } else {
            return ResultDO.buildError(ResultCode.INPUT_FILE_TYPE_MUST_BE_TSV_OR_TRAML);
        }

        return resultDO;
    }

    @Override
    public void countAndUpdateForLibrary(LibraryDO library) {
        try {
            library.setProteinCount(transitionService.countByProteinName(library.getId()));
            library.setPeptideCount(transitionService.countByPeptideRef(library.getId()));

            TransitionQuery query = new TransitionQuery();
            query.setLibraryId(library.getId());
            library.setTotalCount(transitionService.count(query));
            query.setIsDecoy(false);
            library.setTotalTargetCount(transitionService.count(query));
            query.setIsDecoy(true);
            library.setTotalDecoyCount(transitionService.count(query));

            update(library);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }
}
