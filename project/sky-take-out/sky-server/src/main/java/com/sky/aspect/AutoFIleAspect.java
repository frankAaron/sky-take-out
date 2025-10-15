package com.sky.aspect;

import com.sky.annotation.AutoFile;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

@Aspect
@Component
@Slf4j
public class AutoFIleAspect {
    @Pointcut("execution(* com.sky.mapper.*.*(..))&& @annotation(com.sky.annotation.AutoFile)")
    public void autoFilePointCut() {
    }

    @Before("autoFilePointCut()")
    public void autoFile(JoinPoint joinPoint) {
        log.info("开始进行公共字段的自动填充");
        //获取拦截方法上数据库操作的类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        AutoFile autoFile = signature.getMethod().getAnnotation(AutoFile.class);
        OperationType operationType = autoFile.value();

        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0];
        LocalDateTime now = LocalDateTime.now();
        Long currentID = BaseContext.getCurrentId();
        if (operationType == OperationType.INSERT) {
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);


                setCreateTime.invoke(entity, now);
                setUpdateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentID);
                setUpdateUser.invoke(entity, currentID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }else if (operationType == OperationType.UPDATE) {
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentID);
            } catch (Exception e) {

                throw new RuntimeException(e);
            }


        }
    }
}
