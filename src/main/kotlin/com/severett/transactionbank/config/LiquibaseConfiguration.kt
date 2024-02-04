package com.severett.transactionbank.config

import liquibase.integration.spring.SpringLiquibase
import org.springframework.boot.autoconfigure.liquibase.LiquibaseProperties
import org.springframework.boot.context.properties.EnableConfigurationProperties
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.sql.DataSource

@Configuration
@EnableConfigurationProperties(LiquibaseProperties::class)
class LiquibaseConfiguration {
    @Bean
    fun liquibase(
        dataSource: DataSource,
        liquibaseProperties: LiquibaseProperties
    ) =
        SpringLiquibase().also { springLiquibase ->
            springLiquibase.dataSource = dataSource
            springLiquibase.changeLog = "classpath:liquibase/master.xml"
            springLiquibase.contexts = liquibaseProperties.contexts
            springLiquibase.defaultSchema = liquibaseProperties.defaultSchema
            springLiquibase.isDropFirst = liquibaseProperties.isDropFirst
            springLiquibase.setShouldRun(liquibaseProperties.isEnabled)
        }
}
