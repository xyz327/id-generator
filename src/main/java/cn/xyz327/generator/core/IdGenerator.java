package cn.xyz327.generator.core;


import org.jetbrains.annotations.Nullable;

/**
 * @author <a href="mailto:xyz327@outlook.com">xizhou</a>
 * @since 2020/9/15 7:47 下午
 */
public interface IdGenerator {
    /**
     * 生成单个 id
     * @param key key 可以为{@code null}
     * @return 生成的 id
     */
    long generate(@Nullable String key);

    /**
     * 批量生成 id
     * @param key key 可以为{@code null}
     * @param batchSize 批量大小
     * @return 生成的 id 集合
     */
    long[] batchGenerate(@Nullable String key, long batchSize);
}
