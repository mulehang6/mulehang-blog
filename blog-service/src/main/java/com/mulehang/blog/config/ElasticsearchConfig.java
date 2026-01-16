package com.mulehang.blog.config;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Elasticsearch 配置类，手动配置 ElasticsearchClient Bean。
 *
 * <p>通过 {@code blog.elasticsearch.enabled=true} 开启（默认关闭），
 * 避免 ES 不可用时影响应用启动。</p>
 */
@Slf4j
@Configuration
@ConditionalOnProperty(name = "blog.elasticsearch.enabled", havingValue = "true")
@EnableConfigurationProperties(BlogElasticsearchProperties.class)
public class ElasticsearchConfig {

    @Value("${spring.elasticsearch.uris:http://localhost:9200}")
    private String uris;

    /**
     * 创建 RestClient Bean（带生命周期管理）。
     *
     * @return RestClient
     */
    @Bean(destroyMethod = "close")
    public RestClient elasticsearchRestClient() {
        String uri = uris.split(",")[0].trim();
        HttpHost host = HttpHost.create(uri);
        log.info("Elasticsearch RestClient 初始化，连接地址：{}", uri);
        return RestClient.builder(host).build();
    }

    /**
     * 创建 ElasticsearchClient Bean。
     *
     * @param restClient RestClient
     * @return ElasticsearchClient
     */
    @Bean
    public ElasticsearchClient elasticsearchClient(RestClient restClient) {
        RestClientTransport transport = new RestClientTransport(restClient, new JacksonJsonpMapper());
        log.info("ElasticsearchClient 初始化完成");
        return new ElasticsearchClient(transport);
    }
}
