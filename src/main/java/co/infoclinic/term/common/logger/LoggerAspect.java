package co.infoclinic.term.common.logger;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class LoggerAspect {
	
	protected Log log = LogFactory.getLog(LoggerAspect.class);
    
    @Around("execution(* co.infoclinic.term..controller.*Controller.*(..)) or execution(* co.infoclinic.term..service.impl.*Impl.*(..)) or execution(* co.infoclinic.term..repository.*Repository.*(..))")
    public Object logPrint(ProceedingJoinPoint joinPoint) throws Throwable {
    	
    	String jointName = joinPoint.getSignature().getName();
    	String type = joinPoint.getSignature().getDeclaringTypeName();
    	String name = "";
    	
    	long startTime = System.currentTimeMillis();
    	
        
         
        if (type.indexOf("Controller") > -1) {
            name = "Controller  \t:  ";
        }
        else if (type.indexOf("Service") > -1) {
            name = "ServiceImpl  \t:  ";
        }
        else if (type.indexOf("Repository") > -1) {
            name = "Repository  \t:  ";
        }
        log.debug(name + "[Start] " + type + "." + jointName + "()");
        
        Object retVal = joinPoint.proceed();
        
        long endTime = System.currentTimeMillis();
        
        long totalTime = endTime - startTime;
        
        log.debug(name + "[ End : " + totalTime + "ms ] " + type + "." + jointName + "()");
        
        return retVal;
    }
    
    @Before("execution(* co.infoclinic.term..controller.*Controller.*(..)) or execution(* co.infoclinic.term..service.impl.*Impl.*(..)) or execution(* co.infoclinic.term..repository.*Repository.*(..))")
    public void beforeTargetMethod(JoinPoint thisJoinPoint) {
        //System.out.println("AspectUsingAnnotation.afterTargetMethod start." + thisJoinPoint.getSignature().getDeclaringTypeName());
    }
    
    @After("execution(* co.infoclinic.term..controller.*Controller.*(..)) or execution(* co.infoclinic.term..service.impl.*Impl.*(..)) or execution(* co.infoclinic.term..repository.*Repository.*(..))")
    public void afterTargetMethod(JoinPoint thisJoinPoint) {
        //System.out.println("AspectUsingAnnotation.afterTargetMethod end." + thisJoinPoint.getSignature().getDeclaringTypeName());
    }
}
