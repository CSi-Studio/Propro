package net.csibio.propro.controller;

import net.csibio.propro.constants.Constants;
import net.csibio.propro.constants.PrivateUsernames;
import net.csibio.propro.constants.Roles;
import net.csibio.propro.constants.SuccessMsg;
import net.csibio.propro.constants.enums.ResultCode;
import net.csibio.propro.domain.ResultDO;
import net.csibio.propro.domain.db.*;
import net.csibio.propro.domain.query.LibraryQuery;
import net.csibio.propro.domain.query.ProjectQuery;
import net.csibio.propro.domain.query.UserQuery;
import net.csibio.propro.service.AnalyseOverviewService;
import net.csibio.propro.service.ExperimentService;
import net.csibio.propro.service.ProjectService;
import net.csibio.propro.service.UserService;
import net.csibio.propro.utils.PasswordUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.annotation.RequiresRoles;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Controller
@RequestMapping("admin")
public class AdminController extends BaseController {

    @Autowired
    UserService userService;
    @Autowired
    ProjectService projectService;
    @Autowired
    ExperimentService experimentService;
    @Autowired
    AnalyseOverviewService analyseOverviewService;

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/list")
    String list(Model model,
                @RequestParam(value = "currentPage", required = false, defaultValue = "1") Integer currentPage,
                @RequestParam(value = "pageSize", required = false, defaultValue = "50") Integer pageSize,
                @RequestParam(value = "username", required = false) String username,
                @RequestParam(value = "email", required = false) String email,
                @RequestParam(value = "telephone", required = false) String telephone,
                @RequestParam(value = "organization", required = false) String organization,
                RedirectAttributes redirectAttributes) {

        model.addAttribute("username", username);
        model.addAttribute("email", email);
        model.addAttribute("telephone", telephone);
        model.addAttribute("organization", organization);

        UserQuery query = new UserQuery();
        if (StringUtils.isNotEmpty(username)) {
            query.setUsername(username);
        }
        if (StringUtils.isNotEmpty(email)) {
            query.setEmail(email);
        }
        if (StringUtils.isNotEmpty(telephone)) {
            query.setTelephone(telephone);
        }
        if (StringUtils.isNotEmpty(organization)) {
            query.setOrganization(organization);
        }
        query.setPageNo(currentPage);
        query.setPageSize(pageSize);
        ResultDO<List<UserDO>> resultDO = userService.getList(query);

        model.addAttribute("users", resultDO.getModel());
        model.addAttribute("totalPage", resultDO.getTotalPage());
        model.addAttribute("currentPage", currentPage);
        model.addAttribute("users", resultDO.getModel());
        return "user/list";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/create")
    String create(Model model) {
        return "user/create";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/add")
    String add(Model model, @RequestParam(value = "nick", required = false) String nick,
               @RequestParam(value = "username", required = true) String username,
               @RequestParam(value = "password", required = true) String password,
               @RequestParam(value = "role", required = true) String role,
               @RequestParam(value = "email", required = false) String email,
               @RequestParam(value = "telephone", required = false) String telephone,
               @RequestParam(value = "organization", required = false) String organization,
               RedirectAttributes redirectAttributes) {
        model.addAttribute("username", username);
        model.addAttribute("nick", nick);
        model.addAttribute("password", password);
        model.addAttribute("role", role);
        model.addAttribute("email", email);
        model.addAttribute("telephone", telephone);
        model.addAttribute("organization", organization);

        UserDO user = new UserDO();
        if(PrivateUsernames.privates.contains(username)){
            model.addAttribute(ERROR_MSG, ResultCode.USERNAME_CANNOT_BE_USED.getMessage());
            return "user/create";
        }
        user.setUsername(username);
        user.setNick(nick);
        Set<String> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setOrganization(organization);
        String salt = PasswordUtil.getRandomSalt();
        String hashPassword = PasswordUtil.getHashPassword(password, salt);
        user.setPassword(hashPassword);
        user.setSalt(salt);

        ResultDO result = userService.register(user);
        if(result.isFailed()){
            model.addAttribute(ERROR_MSG, result.getMsgInfo());
            return "user/create";
        }else{
            redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.ADD_USER_SUCCESS);
            return "redirect:/admin/user/list";
        }

    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/edit/{id}")
    String edit(Model model,
                @PathVariable("id") String id,
                RedirectAttributes redirectAttributes) {

        UserDO user = userService.getById(id);
        model.addAttribute("user", user);
        return "user/edit";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/update",method = RequestMethod.POST)
    String update(Model model,
                  @RequestParam(value = "id", required = true) String id,
                  @RequestParam(value = "nick", required = false) String nick,
                  @RequestParam(value = "email", required = false) String email,
                  @RequestParam(value = "role", required = true) String role,
                  @RequestParam(value = "telephone", required = false) String telephone,
                  @RequestParam(value = "organization", required = false) String organization,
                  RedirectAttributes redirectAttributes) {
        UserDO user = userService.getById(id);
        if(user == null){
            redirectAttributes.addFlashAttribute(ERROR_MSG, ResultCode.USER_NOT_EXISTED.getMessage());
            return "redirect:/admin/user/list";
        }
        Set<String> roles = new HashSet<>();
        roles.add(role);
        user.setRoles(roles);
        user.setNick(nick);
        user.setEmail(email);
        user.setTelephone(telephone);
        user.setOrganization(organization);
        userService.update(user);
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.UPDATE_SUCCESS);
        return "redirect:/admin/user/list";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/delete/{id}")
    String delete(Model model,
                  @PathVariable("id") String id,
                  RedirectAttributes redirectAttributes) {

        userService.delete(id);
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.DELETE_SUCCESS);
        return "redirect:admin/user/list";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/setasadmin/{id}")
    String setAsAdmin(Model model,
                  @PathVariable("id") String id,
                  RedirectAttributes redirectAttributes) {

        UserDO user = userService.getById(id);
        if(user != null){
            Set<String> roles = new HashSet<>();
            roles.add(Roles.ROLE_ADMIN);
            user.setRoles(roles);
            userService.update(user);
        }
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.UPDATE_SUCCESS);
        return "redirect:/admin/user/list";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/user/resetpwd/{id}")
    String resetPwd(Model model,
                      @PathVariable("id") String id,
                      RedirectAttributes redirectAttributes) {

        UserDO user = userService.getById(id);
        if(user != null){
            user.setSalt(PasswordUtil.getRandomSalt());
            user.setPassword(PasswordUtil.getHashPassword(Constants.RESET_PASSWORD, user.getSalt()));
            userService.update(user);
        }
        redirectAttributes.addFlashAttribute(SUCCESS_MSG, SuccessMsg.UPDATE_SUCCESS);
        return "redirect:/admin/user/list";
    }

    @RequiresRoles("admin")
    @RequestMapping(value = "/repair")
    @ResponseBody
    public String repair(){
        //修复ProjectService的ProjectId字段
        List<ProjectDO> projects = projectService.getAll(new ProjectQuery());
        for(ProjectDO project : projects){
            List<ExperimentDO> exps = experimentService.getAllByProjectName(project.getName());
            for(ExperimentDO exp : exps){
                exp.setProjectId(project.getId());
                exp.setOwnerName(project.getOwnerName());
                experimentService.update(exp);

                List<AnalyseOverviewDO> overviews = analyseOverviewService.getAllByExpId(exp.getId());
                for(AnalyseOverviewDO overview : overviews){
                    overview.setOwnerName(project.getOwnerName());
                    analyseOverviewService.update(overview);
                }
            }
        }

        List<LibraryDO> libraries = libraryService.getAll(new LibraryQuery());
        for(LibraryDO library : libraries){
            if (!library.isDoPublic()){
                libraryService.update(library);
            }
        }
        return "Success";
    }
}
