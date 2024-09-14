/**
 * 
 */
package com.haoyong.sales.common.form;

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
	 */
	ActionEnum[] value();
	
	/**
	 * 执行的优先级
	 */
	ExecutionPriority priority() default ExecutionPriority.NORMAL;
}
