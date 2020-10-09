package cn.xyz327.generator.api;

import cn.xyz327.generator.core.IdGenerator;
import cn.xyz327.generator.vo.Result;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

/**
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/16 11:21 上午
 */
@Path("generate")
public class GeneratorResource {
    @Inject
    IdGenerator idGenerator;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Result hello() {
        Result<Long> longResult = new Result<>();
        longResult.setData(idGenerator.generate(null));
        return longResult;
    }
}
