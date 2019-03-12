package com.opuscapita.peppol.processor.rest;

import com.opuscapita.peppol.commons.template.ApiListRestResponse;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
public class CommonRestController {

    @RequestMapping("/api/list/apis")
    public List<ApiListRestResponse> getApis() {
        List<ApiListRestResponse> apiList = new ArrayList<>();
        apiList.add(new ApiListRestResponse("/api/list/apis", "/api/list/apis"));
        apiList.add(new ApiListRestResponse("/api/health/check", "/api/health/check"));
        return apiList;
    }
}
