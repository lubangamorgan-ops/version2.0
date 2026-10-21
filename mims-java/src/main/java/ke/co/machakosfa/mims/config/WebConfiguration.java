package ke.co.machakosfa.mims.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfiguration implements WebMvcConfigurer {

    private final RememberMeInterceptor rememberMeInterceptor;

    public WebConfiguration(RememberMeInterceptor rememberMeInterceptor) {
        this.rememberMeInterceptor = rememberMeInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(rememberMeInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns(
                        "/h2-console/**",
                        "/css/**",
                        "/js/**",
                        "/images/**",
                        "/favicon.ico");
    }
}
