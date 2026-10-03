package com.freetowear.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

class GlobalExceptionHandlerTests {

    private MockMvc mockMvc;

    @RestController
    static class TestController {
        @GetMapping("/test/not-found")
        public void throwNotFound() {
            throw new ResourceNotFoundException("Resource test not found");
        }

        @GetMapping("/test/access-denied")
        public void throwAccessDenied() {
            throw new AccessDeniedException("Access test denied");
        }

        @GetMapping("/test/business-error")
        public void throwBusinessError() {
            throw new BusinessException("Business validation error");
        }

        @PostMapping("/test/post-only")
        public void postOnly() {
        }

        @GetMapping("/test/unexpected-error")
        public void throwUnexpectedError() {
            throw new RuntimeException("Unexpected internal failure");
        }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders
                .standaloneSetup(new TestController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void whenResourceNotFound_thenReturns404View() throws Exception {
        mockMvc.perform(get("/test/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("errors/404"));
    }

    @Test
    void whenAccessDenied_thenReturns403View() throws Exception {
        mockMvc.perform(get("/test/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("errors/403"));
    }

    @Test
    void whenBusinessException_thenReturns400ViewWithBadRequestStatus() throws Exception {
        mockMvc.perform(get("/test/business-error"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("errors/400"));
    }

    @Test
    void whenMethodNotAllowed_thenReturns405ViewWithMethodNotAllowedStatus() throws Exception {
        mockMvc.perform(get("/test/post-only"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(view().name("errors/405"));
    }

    @Test
    void whenUnexpectedException_thenReturns500ViewWithInternalServerErrorStatus() throws Exception {
        mockMvc.perform(get("/test/unexpected-error"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("errors/500"));
    }
}
