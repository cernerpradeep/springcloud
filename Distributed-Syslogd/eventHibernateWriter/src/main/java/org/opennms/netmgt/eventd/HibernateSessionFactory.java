package org.opennms.netmgt.eventd;

import java.util.Properties;

import javax.sql.DataSource;

import org.hibernate.SessionFactory;
import org.postgresql.ds.PGSimpleDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.annotation.PropertySources;
import org.springframework.core.env.Environment;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.hibernate5.HibernateTemplate;
import org.springframework.orm.hibernate5.HibernateTransactionManager;
import org.springframework.orm.hibernate5.LocalSessionFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

@SuppressWarnings("deprecation")
@Configuration
@PropertySources({
    @PropertySource(value = "classpath:application.properties")})
@ConfigurationProperties(prefix = "spring.datasource")
@EnableTransactionManagement
public class HibernateSessionFactory {
	
	  private PlatformTransactionManager platFormTransactionManager;


	  @Autowired
	   private Environment environment;
	  
	   @Autowired
	   private HibernateTransactionManager hibernateTransactionManager;
	
	   @Bean(name="sessionFactory")
	   public LocalSessionFactoryBean sessionFactory() {
		  LocalSessionFactoryBean sessionFactory = new LocalSessionFactoryBean();
	      sessionFactory.setDataSource(getDataSource());
	      sessionFactory.setPackagesToScan(new String[] { "org.opennms.model","org.opennms.netmgt.model" });
	      sessionFactory.setHibernateProperties(hibernateProperties());
	 
	      return sessionFactory;
	   }
	   
	   @Bean
	   @Autowired
	   public DataSource getDataSource() {
		   
			PGSimpleDataSource dataSource = new PGSimpleDataSource();
			dataSource.setPortNumber(5432);
			dataSource.setUser("opennms");
			dataSource.setPassword("opennms");
			dataSource.setServerName("localhost");
			dataSource.setDatabaseName("opennms");
			return dataSource;
	   }
	 
	   @Bean
	   public HibernateTransactionManager transactionManager(SessionFactory sessionFactory) {
	      HibernateTransactionManager txManager = new HibernateTransactionManager();
	      txManager.setSessionFactory(sessionFactory);
	      return txManager;
	   }
	   
	   @Bean
	   @Autowired
	   public TransactionTemplate transactionTemplate(SessionFactory s) {
		   TransactionTemplate hibernateTemplate = new TransactionTemplate();
		   hibernateTemplate.setTransactionManager(hibernateTransactionManager);
		   platFormTransactionManager = hibernateTransactionManager;
	       return new TransactionTemplate(platFormTransactionManager);
	   }
	 
	   @Bean
	   public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
	      return new PersistenceExceptionTranslationPostProcessor();
	   }
	 
	   private Properties hibernateProperties() {
		    Properties properties = new Properties();
		    properties.put("hibernate.dialect", environment.getRequiredProperty("hibernate.dialect"));
		    properties.put("show_sql", environment.getRequiredProperty("hibernate.show_sql"));
		    properties.put("org.hibernate.flushMode", "COMMIT");
		    properties.setProperty("hibernate.id.new_generator_mappings", Boolean.toString(true));

		    return properties;
		}
	   
	   @Bean
	   @Autowired
	   public HibernateTemplate hibernateTemplate(SessionFactory s) {
	       HibernateTemplate hibernateTemplate = new HibernateTemplate(s);
//	       hibernateTemplate.setCheckWriteOperations(true);
	       return hibernateTemplate;
	   }
}
