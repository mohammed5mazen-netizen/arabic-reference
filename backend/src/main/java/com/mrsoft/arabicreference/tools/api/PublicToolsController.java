package com.mrsoft.arabicreference.tools.api;

import com.mrsoft.arabicreference.shared.web.ClientAddresses;
import com.mrsoft.arabicreference.shared.kernel.time.TimeProvider;
import com.mrsoft.arabicreference.shared.web.api.ApiResponse;
import com.mrsoft.arabicreference.shared.web.api.ApiResponses;
import com.mrsoft.arabicreference.tools.application.LinguisticToolsService;
import com.mrsoft.arabicreference.tools.application.ToolViews.ToolCatalogView;
import com.mrsoft.arabicreference.tools.application.ToolViews.ToolEnvelope;
import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class PublicToolsController {

    private final LinguisticToolsService tools;
    private final TimeProvider time;

    public PublicToolsController(LinguisticToolsService tools, TimeProvider time) {
        this.tools = tools;
        this.time = time;
    }

    @GetMapping("/api/v1/public/tools")
    public ApiResponse<List<ToolCatalogView>> catalog() {
        return ApiResponses.ok(tools.catalog(), time);
    }

    @GetMapping("/api/v1/public/tools/root")
    public ApiResponse<ToolEnvelope> root(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.root(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/derivations")
    public ApiResponse<ToolEnvelope> derivations(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.derivations(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/patterns")
    public ApiResponse<ToolEnvelope> patterns(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.patterns(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/word-analysis")
    public ApiResponse<ToolEnvelope> word(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.word(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/compare")
    public ApiResponse<ToolEnvelope> compare(
            @RequestParam(name = "a", required = false) String left,
            @RequestParam(name = "b", required = false) String right,
            HttpServletRequest request) {
        return ApiResponses.ok(tools.compare(left, right, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/relations")
    public ApiResponse<ToolEnvelope> relations(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.relations(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/spelling-check")
    public ApiResponse<ToolEnvelope> spelling(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.spelling(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/grammar")
    public ApiResponse<ToolEnvelope> grammar(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.grammar(query, ClientAddresses.read(request)), time);
    }

    @GetMapping("/api/v1/public/tools/explore")
    public ApiResponse<ToolEnvelope> explore(@RequestParam(name = "q", required = false) String query, HttpServletRequest request) {
        return ApiResponses.ok(tools.explore(query, ClientAddresses.read(request)), time);
    }
}
