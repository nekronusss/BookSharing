package org.example.booksharing.controller;

import org.example.booksharing.dto.CreateExchangeRequest;
import org.example.booksharing.dto.ExchangeDto;
import org.example.booksharing.service.ExchangeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/exchanges")
public class ExchangeController {
    
    private final ExchangeService exchangeService;
    
    public ExchangeController(ExchangeService exchangeService) {
        this.exchangeService = exchangeService;
    }
    
    @PostMapping
    public ResponseEntity<ExchangeDto> createExchange(@RequestBody CreateExchangeRequest request) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.createExchange(request, username);
        return ResponseEntity.status(HttpStatus.CREATED).body(exchange);
    }
    
    @GetMapping
    public ResponseEntity<List<ExchangeDto>> getUserExchanges() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        List<ExchangeDto> exchanges = exchangeService.getUserExchanges(username);
        return ResponseEntity.ok(exchanges);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<ExchangeDto> getExchangeById(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.getExchangeById(id, username);
        return ResponseEntity.ok(exchange);
    }
    
    @PostMapping("/{id}/accept")
    public ResponseEntity<ExchangeDto> acceptExchange(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.acceptExchange(id, username);
        return ResponseEntity.ok(exchange);
    }
    
    @PostMapping("/{id}/reject")
    public ResponseEntity<ExchangeDto> rejectExchange(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.rejectExchange(id, username);
        return ResponseEntity.ok(exchange);
    }
    
    @PostMapping("/{id}/mark-shipped")
    public ResponseEntity<ExchangeDto> markAsShipped(@PathVariable Long id, @RequestBody(required = false) java.util.Map<String, String> body) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        String trackingNumber = body != null ? body.get("trackingNumber") : null;
        ExchangeDto exchange = exchangeService.markAsShipped(id, trackingNumber, username);
        return ResponseEntity.ok(exchange);
    }
    
    @PostMapping("/{id}/mark-delivered")
    public ResponseEntity<ExchangeDto> markAsDelivered(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.markAsDelivered(id, username);
        return ResponseEntity.ok(exchange);
    }
    
    @PostMapping("/{id}/confirm-receipt")
    public ResponseEntity<ExchangeDto> confirmReceipt(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.confirmReceipt(id, username);
        return ResponseEntity.ok(exchange);
    }
    
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ExchangeDto> cancelExchange(@PathVariable Long id) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        ExchangeDto exchange = exchangeService.cancelExchange(id, username);
        return ResponseEntity.ok(exchange);
    }
}

