package net.csibio.propro.controller;

import net.csibio.propro.constants.SuccessMsg;
import net.csibio.propro.dao.ConfigDAO;
import net.csibio.propro.domain.db.ConfigDO;
import org.apache.tomcat.util.buf.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.support.RequestContextUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by James Lu MiaoShan
 * Time: 2018-08-14 16:02
 */
@Controller
@RequestMapping("/config")
public class ConfigController extends BaseController {

    @Autowired
    ConfigDAO configDAO;

    @RequestMapping(value = "/changeLang")
    String changeLang(Model model,
                      HttpServletRequest request,
                      HttpServletResponse response,
                      @RequestParam(value = "lang", required = false) String lang) {

        LocaleResolver localeResolver = RequestContextUtils.getLocaleResolver(request);
        if ("zh".equals(lang)) {
            localeResolver.setLocale(request, response, new Locale("zh", "CN"));
        } else if ("en".equals(lang)) {
            localeResolver.setLocale(request, response, new Locale("en", "US"));
        }
        return "redirect:/";
    }

    @RequestMapping(value = "/config")
    String config(Model model) {
        ConfigDO configDO = configDAO.getConfig();
        model.addAttribute("config", configDO);
        model.addAttribute("repoUrls", StringUtils.join(configDO.getRepoUrls(), ','));
        return "config";
    }

    @RequestMapping(value = "/update")
    String update(Model model,
                  @RequestParam(value = "repoUrls", required = false) String repoUrls
    ) {
        ConfigDO configDO = configDAO.getConfig();
        List<String> repoUrlList = new ArrayList<>();
        if (repoUrls != null && !repoUrls.isEmpty()) {
            String[] urls = repoUrls.split(",");
            for (String url : urls) {
                repoUrlList.add(url);
            }
            configDO.setRepoUrls(repoUrlList);
        }

        configDAO.updateConfig(configDO);
        model.addAttribute(SUCCESS_MSG, SuccessMsg.UPDATE_SUCCESS);
        model.addAttribute("config", configDAO.getConfig());
        model.addAttribute("repoUrls", StringUtils.join(configDO.getRepoUrls(), ','));
        return "config";
    }
}
