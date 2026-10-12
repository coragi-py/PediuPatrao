package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.Cliente;
import com.umc.pediupatrao.repository.ClienteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ClienteService {

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AuditoriaService auditoriaService; // Injetamos a Auditoria aqui

    public Cliente novoCliente(Cliente cliente) {
        return salvar(cliente); // Redireciona para o salvar que já tem a auditoria embutida
    }

    public List<Cliente> listarClientes() {
        return clienteRepository.findAll();
    }

    public void excluir(String id) {
        clienteRepository.deleteById(id);
    }

    public Optional<Cliente> buscarPorId(String id) {
        return clienteRepository.findById(id);
    }

    // Método principal que fará a interceptação
    public Cliente salvar(Cliente cliente) {
        // 1. É uma INCLUSÃO (Novo Cliente)?
        if (cliente.getId() == null || cliente.getId().trim().isEmpty()) {
            Cliente salvo = clienteRepository.save(cliente);
            auditoriaService.registrar("INCLUSÃO DE CLIENTE", "Cliente", salvo.getId(), "Todos", "N/A", "Novo Cadastro", "Registro inicial de cliente");
            return salvo;
        } 
        // 2. É uma ALTERAÇÃO (Cliente já existe)?
        else {
            Optional<Cliente> existenteOpt = clienteRepository.findById(cliente.getId());
            
            if (existenteOpt.isPresent()) {
                Cliente existente = existenteOpt.get();
                
                // Compara os campos sensíveis exigidos e audita se houver mudança
                auditarAlteracao(existente.getId(), "telefone", existente.getTelefone(), cliente.getTelefone());
                auditarAlteracao(existente.getId(), "logradouro", existente.getLogradouro(), cliente.getLogradouro());
                auditarAlteracao(existente.getId(), "numero", existente.getNumero(), cliente.getNumero());
                auditarAlteracao(existente.getId(), "cep", existente.getCep(), cliente.getCep());
                auditarAlteracao(existente.getId(), "bairro", existente.getBairro(), cliente.getBairro());
                auditarAlteracao(existente.getId(), "cidade", existente.getCidade(), cliente.getCidade());
                
                return clienteRepository.save(cliente);
            } else {
                throw new IllegalArgumentException("Cliente não encontrado para atualização.");
            }
        }
    }

    // Método auxiliar para não repetir código
    private void auditarAlteracao(String idCliente, String campo, String valorAntigo, String valorNovo) {
        valorAntigo = (valorAntigo == null) ? "" : valorAntigo;
        valorNovo = (valorNovo == null) ? "" : valorNovo;

        if (!valorAntigo.equals(valorNovo)) {
            auditoriaService.registrar("ALTERAÇÃO DE CLIENTE", "Cliente", idCliente, campo, valorAntigo, valorNovo, "Atualização cadastral");
        }
    }
}