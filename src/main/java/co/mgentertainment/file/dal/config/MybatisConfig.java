package co.mgentertainment.file.dal.config;

import co.mgentertainment.common.uidgen.annonation.EnableUidGenerator;
import com.alibaba.druid.pool.DruidDataSource;
import com.alibaba.druid.spring.boot.autoconfigure.DruidDataSourceBuilder;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * @author larry
 * @createTime 2023/6/19
 * @description MybatisConfig
 */
@Configuration
@EnableUidGenerator
@MapperScan(basePackages = {"co.mgentertainment.file.dal.mapper"})
@EnableTransactionManagement
public class MybatisConfig {

    @Bean("druidDataSource")
    @ConfigurationProperties("spring.datasource")
    public DruidDataSource defaultDataSource() {
        return DruidDataSourceBuilder.create().build();
    }

    @Bean("defaultSqlSessionFactory")
    public SqlSessionFactory defaultSqlSessionFactory(@Qualifier("druidDataSource") DruidDataSource dataSource) throws Exception {
        SqlSessionFactoryBean bean = new SqlSessionFactoryBean();
        bean.setDataSource(dataSource);
        //数据源的mapper.xml所在位置(具体到xml文件)
        bean.setMapperLocations(new PathMatchingResourcePatternResolver().getResources("classpath*:**/mapper" +
                "/**/*Mapper*.xml"));
        bean.setDefaultEnumTypeHandler(org.apache.ibatis.type.EnumOrdinalTypeHandler.class);
//        bean.setTypeHandlersPackage("co.mgentertainment.player.dal.handler");
        return bean.getObject();
    }

    @Bean("defaultSqlSessionTemplate")
    public SqlSessionTemplate sqlSessionTemplate(@Qualifier("defaultSqlSessionFactory") SqlSessionFactory sqlSessionFactory) {
        return new SqlSessionTemplate(sqlSessionFactory);
    }

    /**
     * 创建事务管理器
     *
     * @param dataSource
     * @return
     */
    @Bean(name = "transactionManager")
    public PlatformTransactionManager transactionManager(@Qualifier("druidDataSource") DruidDataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

//    /**
//     * 拦截器配置
//     *
//     * @param sqlSessionFactory
//     * @return
//     */
//    @Bean
//    public String myInterceptor(SqlSessionFactory sqlSessionFactory, IdGeneratorInterceptor idGeneratorInterceptor) {
//        sqlSessionFactory.getConfiguration().addInterceptor(idGeneratorInterceptor);
//        return "interceptor";
//    }
}