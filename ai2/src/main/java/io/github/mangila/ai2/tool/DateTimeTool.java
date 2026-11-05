package io.github.mangila.ai2.tool;

import org.springframework.ai.tool.annotation.Tool;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.TimeZone;

@Service
public class DateTimeTool {

    @Tool(description = "Call this immediately whenever the user asks for the current time, date, or day of the week.")
    public String getCurrentDateTime() {
        final TimeZone timeZone = LocaleContextHolder.getTimeZone();
        final LocalDateTime now = LocalDateTime.now();
        return now.atZone(timeZone.toZoneId()).toString();
    }
}
