package com.betvictor.repository.adapter.in.web;

import com.betvictor.repository.application.port.in.GetHistoryQuery;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class HistoryController {

    private final GetHistoryQuery getHistoryQuery;

    public HistoryController(GetHistoryQuery getHistoryQuery) {
        this.getHistoryQuery = getHistoryQuery;
    }

    @GetMapping("/betvictor/history")
    public List<HistoryEntryResponse> history() {
        return getHistoryQuery.lastResults().stream()
                .map(HistoryEntryResponse::from)
                .toList();
    }
}
