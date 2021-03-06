package cn.itrip.trade.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

//@EnableWebMvc
/**
 * Swagger API文档生成工具初始化配置信息
 * <br/>
 * 要求jdk8
 * @author hduser
 *
 */
@EnableSwagger2
@ComponentScan(basePackages = {"cn.itrip.trade.controller"})
@Configuration
public class SwaggerConfig extends WebMvcConfigurationSupport {

	/**
	 * 通过createRestApi函数创建Docket的Bean之后，
	 * apiInfo()用来创建该Api的基本信息（这些基本信息会展现在文档页面中）
	 * select()函数返回一个ApiSelectorBuilder实例用来控制哪些接口暴露给Swagger来展现，
	 * apis()函数扫描所有Controller中定义的API， 并产生文档内容（除了被@ApiIgnore指定的请求）
	 * @return
	 */
	@Bean
	public Docket createRestApi() {
		return new Docket(DocumentationType.SWAGGER_2)
				.apiInfo(apiInfo())
				.select()
				.apis(RequestHandlerSelectors.any())
				.paths(PathSelectors.any())
				.build();
		
	}

	/**
	 * 创建该Api的基本信息（这些基本信息会展现在文档页面中）
	 * @return
	 */
	private ApiInfo apiInfo() {
		return new ApiInfoBuilder()
				.title("爱旅行-支付模块API")
				.termsOfServiceUrl("http://www.itrip.com/itriptrade")
				.contact("yao.liu2015@bdqn.cn")
				.version("1.0")
				.build();
	}
}