package nebula.test;

import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;

class MyClass {
    public static String doSomething() {
        File classpath = getClasspathForClass(SomeClass.class);
        return classpath.getAbsolutePath();
    }

    public static File getClasspathForClass(Class<?> targetClass) {
        try {
            URI location = targetClass.getProtectionDomain().getCodeSource().getLocation().toURI();
            Preconditions.checkState(location.getScheme().equals("file"), "Cannot determine classpath for %s from codebase \'%s\'.", targetClass.getName(), location);
            return new File(location);
        } catch (URISyntaxException e) {
            throw Throwables.propagate(e);
        }
    }
}