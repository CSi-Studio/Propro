package net.csibio.propro.service;

import net.csibio.propro.domain.db.LibraryDO;
import net.csibio.propro.domain.db.TaskDO;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.query.LibraryQuery;

import java.io.InputStream;
import java.util.List;


/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 09:36
 */
public interface LibraryService {

    ResultDO<List<LibraryDO>> getList(LibraryQuery query);

    List<LibraryDO> getAll(LibraryQuery query);

    List<LibraryDO> getSimpleAll(String username, Integer type, Boolean doPublic);

    List<LibraryDO> getAllPublic(Integer type);

    long count(LibraryQuery query);

    ResultDO insert(LibraryDO libraryDO);

    ResultDO update(LibraryDO libraryDO);

    ResultDO delete(String id);

    LibraryDO getById(String id);

    LibraryDO getByName(String name);

    String getNameById(String id);

    ResultDO parseAndInsert(LibraryDO library, InputStream in, String fileName, InputStream prmFileStream, TaskDO taskDO);

    void countAndUpdateForLibrary(LibraryDO library);

    void uploadFile(LibraryDO library, InputStream libFileStream, String fileName, InputStream prmFileStream, TaskDO taskDO);
}
