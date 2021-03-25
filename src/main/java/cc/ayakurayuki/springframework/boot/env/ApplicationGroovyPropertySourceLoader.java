package cc.ayakurayuki.springframework.boot.env;

import groovy.lang.GString;
import java.io.IOException;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.springframework.boot.env.PropertySourceLoader;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.env.PropertySource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.scripting.groovy.GroovyScriptFactory;
import org.springframework.scripting.support.ResourceScriptSource;
import org.springframework.util.CollectionUtils;

/**
 * @author Ayakura Yuki
 */
public class ApplicationGroovyPropertySourceLoader implements PropertySourceLoader {

  private static final String[] EXTENSIONS = {"groovy"};

  private final Set<String> loaded = new HashSet<>();

  @Override
  public String[] getFileExtensions() {
    return EXTENSIONS;
  }

  @Override
  public PropertySource<?> load(String name, Resource resource, String profile) throws IOException {
    return load(name, (ClassPathResource) resource);
  }

  private PropertySource<?> load(final String name, final ClassPathResource resource) throws IOException {
    if (loaded.contains(resource.getPath())) {
      return null;
    }

    final Properties properties = new Properties();

    try {
      final Object scriptedObject = getGroovyConfigObject(resource);

      if (scriptedObject instanceof Map) {
        putIntoProperties(properties, (Map<?, ?>) scriptedObject);
      } else {
        final List<Field> fields = Reflections.getFields(scriptedObject.getClass());
        for (Field field : fields) {
          final Object value = Reflections.getField(field.getName(), scriptedObject);
          if (value instanceof Map) {
            putIntoProperties(properties, (Map<?, ?>) value);
          } else if (value instanceof String || value instanceof GString) {
            properties.put(field.getName(), String.valueOf(value));
          }
        }
      }

      return new PropertiesPropertySource(name, properties);
    } finally {
      loaded.add(resource.getPath());
    }
  }

  private void putIntoProperties(final Properties properties, final Map<?, ?> values) {
    if (CollectionUtils.isEmpty(values)) {
      return;
    }
    for (Map.Entry<?, ?> entry : values.entrySet()) {
      properties.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
    }
  }

  private Object getGroovyConfigObject(final ClassPathResource scriptedResource) throws IOException {
    final GroovyScriptFactory groovyScriptFactory = new GroovyScriptFactory(scriptedResource.getPath());
    groovyScriptFactory.setBeanClassLoader(getClass().getClassLoader());
    final ResourceScriptSource resourceScriptSource = new ResourceScriptSource(scriptedResource);
    return groovyScriptFactory.getScriptedObject(resourceScriptSource);
  }

}
