package com.eletricista.calcservice.controller;

import com.eletricista.calcservice.dto.OrcamentoResponse;
import com.eletricista.calcservice.dto.QuizRequest;
import com.eletricista.calcservice.model.Orcamento;
import com.eletricista.calcservice.service.OrcamentoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/v1/orcamentos")
@CrossOrigin(origins = "http://localhost:4200")
public class OrcamentoController {

    @Autowired
    private OrcamentoService orcamentoService;

    @PostMapping("/gerar-quiz")
    public ResponseEntity<OrcamentoResponse> gerar(@RequestBody QuizRequest quiz,
                                                   @RequestHeader(value = "X-User-Role", defaultValue = "VISITANTE") String role) {

        OrcamentoResponse response = orcamentoService.processarNovoOrcamento(quiz, role);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Orcamento> buscarPorId(@PathVariable UUID id) {
        Orcamento orc = orcamentoService.buscarOrcamentoSeguro(id);
        return ResponseEntity.ok(orc);
    }
}