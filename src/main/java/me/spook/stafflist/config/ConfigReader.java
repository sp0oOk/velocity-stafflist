package me.spook.stafflist.config;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.file.Path;

@SuppressWarnings("all")
public class ConfigReader<T> {

    // ------------------------------------------------- //
    // FIELDS
    // ------------------------------------------------- //

    private T instance;

    /**
     * Creates a new config reader
     *
     * @param clazz The class to instantiate the config with
     * @param path  The path to the config
     */

    public ConfigReader(@Nonnull Class<T> clazz, @Nonnull Path path) {
        URL configuration = clazz.getClassLoader().getResource("default-stafflist.toml"); // Could add constructor parameter if expanding to be configurable
        Preconditions.checkNotNull(configuration, "Configuration file not found: " + path);

        try (final CommentedFileConfig config = CommentedFileConfig
                .builder(path)
                .defaultData(configuration)
                .autosave()
                .preserveInsertionOrder()
                .sync()
                .build()) {
            config.load();
            final double _version = config.getOrElse("version", 1.0);

            try {
                final Object obj = clazz.getConstructors()[0].newInstance();

                for (Field field : clazz.getDeclaredFields()) {
                    final String name = field.getName().replace("_", "-");
                    if (config.contains(name)) {
                        field.setAccessible(true);
                        field.set(obj, config.get(name));
                    }
                }

                instance = (T) obj;
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Get optional instance
     *
     * @return The instance if it exists or an empty optional
     */

    public Optional<T> instance() {
        return Optional.fromNullable(instance);
    }
}
