package org.dreambot.api.script;
import java.lang.annotation.*;
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ScriptManifest {
  String author() default "";
  String name() default "";
  double version() default 1.0;
  Category category() default Category.MISC;
  String description() default "";
}
