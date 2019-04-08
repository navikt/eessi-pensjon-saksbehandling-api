package no.nav.eessi.fagmodul.frontend.config

import com.google.common.base.Predicates
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import springfox.documentation.builders.ApiInfoBuilder
import springfox.documentation.builders.PathSelectors
import springfox.documentation.builders.RequestHandlerSelectors
import springfox.documentation.service.ApiInfo
import springfox.documentation.spi.DocumentationType
import springfox.documentation.spring.web.plugins.Docket
import springfox.documentation.swagger2.annotations.EnableSwagger2

@Configuration
@EnableSwagger2
class SwaggerConfig {

    @Bean
    fun api(): Docket {
        return Docket(DocumentationType.SWAGGER_2)
                .apiInfo(metaData())
                .select()
                .apis(Predicates.not(RequestHandlerSelectors.basePackage("org.springframework.boot")))
                .paths(PathSelectors.any())
                .build()
    }

    private fun metaData(): ApiInfo {
        return ApiInfoBuilder()
                .title("EESSI-Pensjon-Frontend-API - Spring Boot REST API")
                .description("Spring Boot REST API for EESSI-Pensjon.\n" +
                        "Vi finnes på slack https://nav-it.slack.com/messages/CAB4L39T6 eller https://nav-it.slack.com/messages/CADNRDN5T")
                .build()
    }


}