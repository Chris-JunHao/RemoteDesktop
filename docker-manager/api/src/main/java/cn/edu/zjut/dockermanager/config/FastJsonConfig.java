package cn.edu.zjut.dockermanager.config;

import com.alibaba.fastjson.serializer.PropertyFilter;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.alibaba.fastjson.support.spring.FastJsonHttpMessageConverter;
import org.springframework.boot.autoconfigure.http.HttpMessageConverters;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;

@Configuration  // 声明该类为Spring配置类
public class FastJsonConfig {

    @Bean  // 将此方法作为Spring Bean进行管理
    public HttpMessageConverters fastJsonHttpMessageConverters() {
        // 1. 创建FastJsonHttpMessageConverter对象，用于消息转换
        FastJsonHttpMessageConverter fastJsonHttpMessageConverter = new FastJsonHttpMessageConverter();

        // 2. 创建FastJson的配置信息对象
        com.alibaba.fastjson.support.config.FastJsonConfig fastJsonConfig = new com.alibaba.fastjson.support.config.FastJsonConfig();

        // 设置FastJson序列化特性
        fastJsonConfig.setSerializerFeatures(
                //SerializerFeature.PrettyFormat,  // 格式化输出
                SerializerFeature.WriteMapNullValue,  // 输出null值
                SerializerFeature.WriteNullNumberAsZero,  // 将null数值输出为0
                SerializerFeature.WriteNullStringAsEmpty  // 将null字符串输出为空字符串
        );

        // 过滤掉空字段，不返回给客户端
        fastJsonConfig.setSerializeFilters((PropertyFilter) (object, name, value) -> !StringUtils.isEmpty(value));

        // 3. 处理中文乱码问题
        List<MediaType> fastMediaTypes = new ArrayList<>();
        fastMediaTypes.add(MediaType.APPLICATION_JSON_UTF8);  // 设置支持的媒体类型为UTF-8编码的JSON

        // 4. 设置日期格式
        fastJsonConfig.setDateFormat("yyyy-MM-dd HH:mm:ss");

        // 5. 将配置信息添加到FastJsonHttpMessageConverter中
        fastJsonHttpMessageConverter.setSupportedMediaTypes(fastMediaTypes);
        fastJsonHttpMessageConverter.setFastJsonConfig(fastJsonConfig);

        // 6. 返回HttpMessageConverters对象，注册FastJsonHttpMessageConverter
        return new HttpMessageConverters(fastJsonHttpMessageConverter);
    }
}
