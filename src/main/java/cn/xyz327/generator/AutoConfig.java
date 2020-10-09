package cn.xyz327.generator;

import cn.xyz327.generator.core.IdGenerator;
import cn.xyz327.generator.core.snowflake.SnowflakeIdGenerator;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

/**
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/16 2:26 下午
 */
@ApplicationScoped
public class AutoConfig {

    /**
     * workerId: 8位 最大 256
     * sequence: 14位 最大 16384
     * @return
     */
    @Produces
    public IdGenerator snowflakeIdGenerator(){
        return new SnowflakeIdGenerator(1, 8, 14);
    }
}
