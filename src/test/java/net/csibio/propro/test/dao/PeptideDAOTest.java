package net.csibio.propro.test.dao;

import net.csibio.propro.dao.PeptideDAO;
import net.csibio.propro.domain.db.PeptideDO;
import net.csibio.propro.domain.query.PeptideQuery;
import net.csibio.propro.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-08 19:07
 */
public class PeptideDAOTest extends BaseTest {

    @Autowired
    PeptideDAO peptideDAO;

    @Test
    public void getAllByLibraryIdTest() {
        List<PeptideDO> list = peptideDAO.getAllByLibraryId("5b6712012ada5f2dc8de57d7");
        assert true;
    }

    @Test
    public void getAllByLibraryIdAndIsDecoy() {
        List<PeptideDO> result = peptideDAO.getAllByLibraryId("5b67136d2ada5f15749a0140");
        assert true;
    }

    @Test
    public void getListTest() {
        PeptideDO peptideDO = getJsonFromFileTest();
        PeptideQuery peptideQuery = new PeptideQuery();
        List<PeptideDO> result = peptideDAO.getList(peptideQuery);
        assert true;
    }

    @Test
    public void getTTAllTest() {
        assert true;
    }

    @Test
    public void countTest() {
        assert true;
    }

    @Test
    public void getByIdTest() {
        assert true;
    }

    @Test
    public void insertTest_TransitionDO() {
        assert true;
    }

    @Test
    public void insertTest_ListTransitionDO() {
        assert true;
    }

    @Test
    public void updateTest() {
        assert true;
    }

    @Test
    public void deleteTest() {
        assert true;
    }

    @Test
    public void deleteAllByLibraryIdTest(){
        assert true;
    }

    @Test
    public void getProteinListTest(){
        assert true;
    }

    @Test
    public void getPeptideListTest(){
        assert true;
    }

}
