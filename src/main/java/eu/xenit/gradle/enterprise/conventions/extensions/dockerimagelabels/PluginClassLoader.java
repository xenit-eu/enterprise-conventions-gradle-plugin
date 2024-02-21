package eu.xenit.gradle.enterprise.conventions.extensions.dockerimagelabels;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.gradle.api.Plugin;

@RequiredArgsConstructor
class PluginClassLoader {

    private final Plugin<?> plugin;

    private ClassLoader getClassLoader() {
        return plugin.getClass().getClassLoader();
    }

    @SneakyThrows
    public <T> Class<T> loadClass(String clazz) {
        return (Class<T>) getClassLoader().loadClass(clazz);
    }

}
