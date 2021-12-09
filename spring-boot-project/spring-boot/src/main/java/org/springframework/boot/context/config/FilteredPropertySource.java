/*
 * Copyright 2012-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.context.config;

import java.util.Set;
import java.util.function.Consumer;

import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertySource;

/**
 * Internal {@link PropertySource} implementation used by
 * {@link ConfigFileApplicationListener} to filter out properties for specific operations.
 *
 * @author Phillip Webb
 */
class FilteredPropertySource extends PropertySource<PropertySource<?>> {

	private final Set<String> filteredProperties;

	FilteredPropertySource(PropertySource<?> original, Set<String> filteredProperties) {
		super(original.getName(), original);
		this.filteredProperties = filteredProperties;
	}

	@Override
	public Object getProperty(String name) {
		if (this.filteredProperties.contains(name)) {
			return null;
		}
		return getSource().getProperty(name);
	}

	/**
	 *
	 * @param environment 环境上下文
	 * @param propertySourceName  defaultProperties
	 * @param filteredProperties filteredProperties.add("spring.profiles.active"); filteredProperties.add("spring.profiles.include");
	 * @param operation loadWithFilteredProperties
	 */
	static void apply(ConfigurableEnvironment environment, String propertySourceName, Set<String> filteredProperties,
			Consumer<PropertySource<?>> operation) {
		MutablePropertySources propertySources = environment.getPropertySources();
		//获取name=defaultProperties的source
		PropertySource<?> original = propertySources.get(propertySourceName);
		//为空则loadWithFilteredProperties传入null
		if (original == null) {
			operation.accept(null);
			return;
		}
		//不为空，创建一个FilteredPropertySource替换调name=defaultProperties中的值
		propertySources.replace(propertySourceName, new FilteredPropertySource(original, filteredProperties));
		try {
			//传入original
			operation.accept(original);
		}
		finally {
			//还原name=defaultProperties中的值
			propertySources.replace(propertySourceName, original);
		}
	}

}
