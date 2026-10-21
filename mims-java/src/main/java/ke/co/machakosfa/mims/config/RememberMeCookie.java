package ke.co.machakosfa.mims.config;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.ResponseCookie;
import ke.co.machakosfa.mims.service.RememberMeService;

public final class RememberMeCookie {

    public static final String NAME = "MIMS_REMEMBER";

    private RememberMeCookie() {}

    public static String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (NAME.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    public static void set(
            HttpServletResponse response, HttpServletRequest request, String token) {
        response.addHeader("Set-Cookie", ResponseCookie.from(NAME, token)
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(java.time.Duration.ofDays(RememberMeService.TOKEN_LIFETIME_DAYS))
                .build()
                .toString());
    }

    public static void clear(HttpServletResponse response, HttpServletRequest request) {
        response.addHeader("Set-Cookie", ResponseCookie.from(NAME, "")
                .httpOnly(true)
                .secure(request.isSecure())
                .sameSite("Lax")
                .path("/")
                .maxAge(0)
                .build()
                .toString());
    }
}
