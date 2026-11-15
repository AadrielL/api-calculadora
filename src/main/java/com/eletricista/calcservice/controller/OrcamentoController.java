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
@CrossOrigin(originPatterns = "*", allowCredentials = "true", allowedHeaders = "*")
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

    // M2-02: Visualização pública da proposta pelo cliente do eletricista
    @GetMapping("/publico/{id}")
    public ResponseEntity<Orcamento> buscarPublicoPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(orcamentoService.buscarOrcamentoPublico(id));
    }

    // M2-02: Aceite com 1 clique pelo cliente (grava status Aceito/Recusado + dataHora no banco)
    @PostMapping("/publico/{id}/aceitar")
    public ResponseEntity<Orcamento> aceitarOuRecusarPublico(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "true") boolean aceitar) {
        return ResponseEntity.ok(orcamentoService.registrarAceiteCliente(id, aceitar));
    }
}
