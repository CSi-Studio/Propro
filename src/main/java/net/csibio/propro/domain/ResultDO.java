package net.csibio.propro.domain;

import net.csibio.propro.constants.enums.ResultCode;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-06-06 09:36
 */
public class ResultDO<T> implements Serializable {
    private static final long serialVersionUID = 1738821497566027418L;

    /**
     * 是否执行成功
     */
    private boolean success = false;

    /**
     * 错误码
     */
    private String msgCode;

    /**
     * 错误提示信息
     */
    private String msgInfo;

    /**
     * Http 返回状态
     */
    private int status;

    /**
     * 错误信息列表
     */
    private List<String> errorList;

    /**
     * 单值返回,泛型
     */
    private T model;

    /**
     * 总计全部数目
     */
    private long totalNum;

    /**
     * 每页显示个数
     */
    private int pageSize = 10;

    /**
     * 最大页面数
     */
    private long totalPage;

    /**
     * 当前访问页面号
     */
    private long currentPageNo;

    public ResultDO() {
    }

    public ResultDO(boolean success) {
        this.success = success;
    }

    public boolean isSuccess() {
        return success;
    }

    public boolean isFailed() {
        return !success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMsgCode() {
        return msgCode;
    }

    public void setMsgCode(String msgCode) {
        this.msgCode = msgCode;
    }

    public T getModel() {
        return model;
    }

    public ResultDO<T> setModel(T value) {
        this.model = value;
        return this;
    }

    public String getMsgInfo() {
        return msgInfo;
    }

    public void setMsgInfo(String msgInfo) {
        this.msgInfo = msgInfo;
    }

    public ResultDO setResultCode(ResultCode code) {
        this.msgCode = code.getCode();
        this.msgInfo = code.getMessage();
        return this;
    }

    public ResultDO setErrorResult(ResultCode resultCode) {
        this.success = false;
        this.msgCode = resultCode.getCode();
        this.msgInfo = resultCode.getMessage();
        return this;
    }

    public ResultDO setErrorResult(String code, String msg) {
        this.success = false;
        this.msgCode = code;
        this.msgInfo = msg;
        return this;
    }

    public ResultDO setErrorResult(ResultCode resultCode, String msg) {
        return this.setErrorResult(resultCode.getCode(), msg);
    }

    public static ResultDO buildError(ResultCode resultCode) {
        ResultDO resultDO = new ResultDO();
        resultDO.setErrorResult(resultCode);
        return resultDO;
    }

    public static ResultDO buildError(ResultCode resultCode, int status) {
        ResultDO resultDO = new ResultDO();
        resultDO.setErrorResult(resultCode);
        resultDO.setStatus(status);
        return resultDO;
    }

    public static ResultDO build(Object object) {
        ResultDO resultDO = new ResultDO(true);
        resultDO.setModel(object);
        return resultDO;
    }

    public long getTotalNum() {
        return totalNum;
    }

    public void setTotalNum(long totalNum) {
        this.totalNum = totalNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getTotalPage() {
        if (this.pageSize > 0 && this.totalNum > 0) {
            return (this.totalNum % this.pageSize == 0L ? (this.totalNum / this.pageSize) : (this.totalNum / this.pageSize + 1));
        } else {
            return 0;
        }
    }

    public void setTotalPage(long totalPage) {
        this.totalPage = totalPage;
    }

    public long getCurrentPageNo() {
        return currentPageNo;
    }

    public void setCurrentPageNo(long currentPageNo) {
        this.currentPageNo = currentPageNo;
    }

    public List<String> getErrorList() {
        return errorList;
    }

    public ResultDO setErrorList(List<String> errorList) {
        this.errorList = errorList;
        return this;
    }

    public void addErrorMsg(String errorMsg) {
        if (errorList == null) {
            errorList = new ArrayList<>();
        }
        errorList.add(errorMsg);
    }

}
