/**
 * 
 */
package com.haoyong.salel.common.form;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sf.mily.bus.annotation.ExecutionPriority;

/**<pre>
 * Title: 匹配Action集合
 * </pre>
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
public @interface MatchActions {
	
	/**
	 * ActionEnum 实例或数组
	 * @return
	 */
	ActionEnum[] value();
	
	/**
	 * <pre>
	 * 执行的优先级
	 * 如果配置了 LATER， 那么 Service 里面的 Action 监听方法会比所有 非LATER的执行得要晚。
	 * 默认值是 {@link ExecutionPriority#NORMAL} 
	 * </pre>
	 * @return
	 */
	ExecutionPriority priority() default ExecutionPriority.NORMAL;
}
