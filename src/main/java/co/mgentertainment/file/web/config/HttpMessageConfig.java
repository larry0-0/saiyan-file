package co.mgentertainment.file.web.config;

import co.mgentertainment.common.doc.annonation.EnableCommonDoc;
import co.mgentertainment.common.utils.GsonFactory;
import com.google.gson.Gson;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.converter.json.GsonHttpMessageConverter;

/**
 * @author larry
 */
@Configuration
@EnableCommonDoc
public class HttpMessageConfig {

    @Bean
    public HttpMessageConverters httpMessageConverters() {
        GsonHttpMessageConverter gsonConverter = new GsonHttpMessageConverter();
        Gson gson = GsonFactory.getGson();
        gsonConverter.setGson(gson);
        return new HttpMessageConverters(gsonConverter);
    }
}
