package net.csibio.propro.exception;

public class UnauthorizedAccessException extends RuntimeException {

    private static final long serialVersionUID = 456412449131125748L;

    String redirectPage;

    public UnauthorizedAccessException() {
        super();
    }

    public UnauthorizedAccessException(String redirectPage) {
        super();
        this.redirectPage = redirectPage;
    }

    public void setRedirectPage(String redirectPage){
        this.redirectPage = redirectPage;
    }

    public String getRedirectPage(){
        return redirectPage;
    }
}
