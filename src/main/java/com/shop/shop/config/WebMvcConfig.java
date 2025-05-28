package com.shop.shop.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${uploadPath}") // "uploadPath" 프로퍼티 값 읽어옴
    String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        // url 에 images 로 시작하는 경우 uploadPath에 설정한 폴더를 기준으로 파일을 읽어오도록 설정
        registry.addResourceHandler("/images/**")
                .addResourceLocations(uploadPath); // 로컬 컴퓨터에 저장된 파일을 읽어올 root 경로 설정
    }
}
