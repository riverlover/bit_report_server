/**   
* 文件名：CustomizedPropertyPlaceholderConfigurer.java   
*   
* 版本信息：   
* 日期：2014-1-23   
* Copyright 梅泰诺移动信息 2014    
* 版权所有   
*   
*/
package zhenhe.li.util.spring;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.util.PropertyPlaceholderHelper;


/**   
*    
* 项目名称：bms   
* 类名称：CustomizedPropertyPlaceholderConfigurer   
* 类描述：   继承spring中的占位符配置类，增加快速取得其中属性的方法
* 创建人：lzh   
* 创建时间：2014-1-23 下午3:20:50   
* 修改人：lzh   
* 修改时间：2014-1-23 下午3:20:50   
* 修改备注：   
* @version    
*    
*/
public class ConfigUtil extends PropertyPlaceholderConfigurer{
	private static Map<String, Object> ctxPropertiesMap;

	@Override
	protected void processProperties(
			ConfigurableListableBeanFactory beanFactoryToProcess,
			Properties props) throws BeansException {
		super.processProperties(beanFactoryToProcess, props);
		ctxPropertiesMap = new HashMap<String, Object>();
		for (Object key : props.keySet()) {
			String keyStr = key.toString();
			String value = props.getProperty(keyStr);
			//对value中还有占位符的情况，做二次解析 add by lizhenhe@ 2014-01-23 15:27
			PropertyPlaceholderHelper helper = new PropertyPlaceholderHelper(
					placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
			value = helper.replacePlaceholders(value, props);
			
			ctxPropertiesMap.put(keyStr, value);
		}
	}

	public static Object getContextProperty(String name) {
		return ctxPropertiesMap.get(name);
	}
}
