package com.csi.propro;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

/**
 * 自动化Open API文档生成模块
 */
@EnableSwagger2
@Configuration
public class Swagger2 {

    @Bean
    public Docket createRestApi(){
//        ParameterBuilder tokenPar = new ParameterBuilder();
//        List<Parameter> pars = new ArrayList<Parameter>();
//        tokenPar.name("token").description("token")
//                .modelRef(new ModelRef("string")).parameterType("query").required(false).build();
//        pars.add(tokenPar.build());
        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(buildApiInfo())
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.csi.propro.openapi"))
                .paths(PathSelectors.any())
//                .build().globalOperationParameters(pars)  ;
                .build();
    }

    private ApiInfo buildApiInfo() {
        return new ApiInfoBuilder()
                .title("Propro OpenAPI")
                .description("OpenAPI for Propro Platform")
                .contact(new Contact("陆妙善-James Lu","https://www.zhihu.com/people/lu-miao-shan-42/activities","lumiaoshan@westlake.edu.cn"))
                .version("1.0-Beta")
                .build();
    }

}
