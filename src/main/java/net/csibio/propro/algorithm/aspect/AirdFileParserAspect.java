package net.csibio.propro.algorithm.aspect;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class AirdFileParserAspect {

    public final Logger logger = LoggerFactory.getLogger(AirdFileParserAspect.class);

    @Around("within(net.csibio.propro.algorithm.parser.AirdFileParser)")
    public Object countTime(ProceedingJoinPoint joinPoint) {
        Object obj = null;
        Object[] args = joinPoint.getArgs();
        long startTime = System.currentTimeMillis();

        try {
            obj = joinPoint.proceed(args);
        } catch (Throwable e) {
            logger.error("统计某方法执行耗时环绕通知出错", e);
        }

        logger.info("IO及解码耗时耗时：" + (System.currentTimeMillis() - startTime) + "毫秒");

        return obj;
    }

}
