package spring.tutorial.mood.web;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TimeOfDayFilter extends OncePerRequestFilter {

    // Provide a default; can be overridden via FilterRegistrationBean if desired
    private final String mood;

    public TimeOfDayFilter() {
        this.mood = "awake";
    }

    public TimeOfDayFilter(String mood) {
        this.mood = mood;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {
        // Example: set an attribute based on time/mood (same behavior you had)
        request.setAttribute("mood", mood);
        // ... any time-of-day logic you previously had ...
        filterChain.doFilter(request, response);
    }
}
