package com.libertymutual.policy.controller;

import com.libertymutual.policy.model.ZipRiskData;
import com.libertymutual.policy.service.ZipRiskService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ZipRiskController.class)
class ZipRiskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ZipRiskService zipRiskService;

    @Test
    void getZipRisk_knownZip_returnsData() throws Exception {
        ZipRiskData data = new ZipRiskData("33101", true, 0.85, 0.1, 0.5, 1.75, "FL", true);
        when(zipRiskService.getZipRiskFactors("33101")).thenReturn(Optional.of(data));

        mockMvc.perform(get("/api/v1/zip-risk/33101"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("FL"))
                .andExpect(jsonPath("$.flood_zone").value(true))
                .andExpect(jsonPath("$.base_modifier").value(1.75));
    }

    @Test
    void getZipRisk_unknownZip_returns404() throws Exception {
        when(zipRiskService.getZipRiskFactors("99999")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/zip-risk/99999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.error").value("not_found"));
    }
}
