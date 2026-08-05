package com.iisi.bookmanager;

import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * WAR 部署進入點（design.md / 3人團隊SSDLC協作與Git分支實例.md：後端包版為 WAR，
 * 部署至外部 Servlet 容器，如 Tomcat）。
 *
 * <p>本機開發仍可用 {@code mvn spring-boot:run} 內嵌 Tomcat 啟動，
 * 不需要外部容器；此類別僅在以 WAR 部署至外部容器時才會被呼叫。
 */
public class ServletInitializer extends SpringBootServletInitializer {

    @Override
    protected SpringApplicationBuilder configure(SpringApplicationBuilder application) {
        return application.sources(PersonalBookManagerApplication.class);
    }
}
