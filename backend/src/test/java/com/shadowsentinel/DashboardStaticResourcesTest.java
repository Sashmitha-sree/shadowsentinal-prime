package com.shadowsentinel;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DashboardStaticResourcesTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("GET / forwards to index.html without authentication")
    void getRoot_ServesIndexHtml() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.forwardedUrl("index.html"));
    }

    @Test
    @DisplayName("GET /index.html serves HTML dashboard")
    void getIndexHtml_ServesIndex() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Shadow Sentinel")));
    }

    @Test
    @DisplayName("GET /style.css serves dark restrained CSS")
    void getStyleCss_ServesStylesheet() throws Exception {
        mockMvc.perform(get("/style.css"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("--bg-app")));
    }

    @Test
    @DisplayName("GET /app.js serves dashboard JavaScript application")
    void getAppJs_ServesScript() throws Exception {
        mockMvc.perform(get("/app.js"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("Shadow Sentinel Dashboard")));
    }
}
