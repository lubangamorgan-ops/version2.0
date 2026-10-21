package ke.co.machakosfa.mims.config;

import jakarta.servlet.http.Cookie;
import ke.co.machakosfa.mims.model.Account;
import ke.co.machakosfa.mims.repository.FarmerRepository;
import ke.co.machakosfa.mims.service.RememberMeService;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RememberMeInterceptorTest {

    private final RememberMeService rememberMeService = mock(RememberMeService.class);
    private final FarmerRepository farmerRepository = mock(FarmerRepository.class);
    private final RememberMeInterceptor interceptor =
            new RememberMeInterceptor(rememberMeService, farmerRepository);

    @Test
    void restoresAdminSessionAndRotatesPersistentCookie() throws Exception {
        Account account = mock(Account.class);
        when(account.getRole()).thenReturn("ADMIN");
        when(account.getUsername()).thenReturn("admin");
        when(rememberMeService.rotateToken("original-token"))
                .thenReturn(Optional.of(new RememberMeService.RememberedLogin(account, "rotated-token")));
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie(RememberMeCookie.NAME, "original-token"));
        MockHttpServletResponse response = new MockHttpServletResponse();

        assertTrue(interceptor.preHandle(request, response, new Object()));
        assertEquals("admin", request.getSession().getAttribute("role"));
        assertEquals("admin", request.getSession().getAttribute("name"));
        assertEquals("Association Administrator", request.getSession().getAttribute("staffRole"));
        assertTrue(response.getHeader("Set-Cookie").contains("MIMS_REMEMBER=rotated-token"));
        assertTrue(response.getHeader("Set-Cookie").contains("HttpOnly"));
        verify(rememberMeService).rotateToken("original-token");
        verifyNoInteractions(farmerRepository);
    }
}
