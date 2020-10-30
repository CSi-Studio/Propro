package net.csibio.propro.exception;

import net.csibio.propro.constants.enums.ResultCode;
import org.apache.shiro.authz.UnauthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@ControllerAdvice
public class GlobalExceptionHandler {

    public final Logger logger = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(UserNotLoginException.class)
    public String userNotLoginException(UserNotLoginException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error_msg", ResultCode.USER_NOT_EXISTED.getMessage());
        return "redirect:/login/login";
    }

    @ExceptionHandler(UnauthorizedAccessException.class)
    public String unauthorizedAccessException(UnauthorizedAccessException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error_msg", ResultCode.UNAUTHORIZED_ACCESS.getMessage());
        return e.getRedirectPage();
    }

    @ExceptionHandler(UnauthorizedException.class)
    public String unauthorizedException(UnauthorizedException e, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("error_msg", ResultCode.UNAUTHORIZED_ACCESS.getMessage());
        logger.warn("非法访问尝试,非管理员权限账号正在企图访问管理员专有页面");
        return "/error";
    }
}
