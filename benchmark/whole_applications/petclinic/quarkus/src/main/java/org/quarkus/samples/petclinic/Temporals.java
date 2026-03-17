package org.quarkus.samples.petclinic;

import io.quarkus.qute.TemplateExtension;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * This is a temporary hack around
 * <a href="https://github.com/redhat-developer-demos/quarkus-petclinic/issues/7">this</a> issue.
 * <p>
 * The problem appears to be M1 specific and is being
 * <a href="https://quarkusio.zulipchat.com/#narrow/stream/187038-dev/topic/petclinic">looked</a> into by the Quarkus devs.
 */
@TemplateExtension
public class Temporals {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public static String format(LocalDate localDate) {
        return localDate.format(FORMATTER);
    }
}
