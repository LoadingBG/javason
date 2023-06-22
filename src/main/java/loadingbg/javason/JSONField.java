package loadingbg.javason;

import java.lang.annotation.*;

@Target({ ElementType.METHOD, ElementType.RECORD_COMPONENT })
@Retention(RetentionPolicy.RUNTIME)
public @interface JSONField {
    String value();
}
