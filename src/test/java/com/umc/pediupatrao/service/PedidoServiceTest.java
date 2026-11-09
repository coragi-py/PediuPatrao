package com.umc.pediupatrao.service;

import com.umc.pediupatrao.entity.ItemPedido;
import com.umc.pediupatrao.entity.Pedido;
import com.umc.pediupatrao.entity.Produto;
import com.umc.pediupatrao.repository.PedidoRepository;
import com.umc.pediupatrao.repository.ProdutoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PedidoServiceTest {

    @Mock
    private PedidoRepository pedidoRepository;

    @Mock
    private ProdutoRepository produtoRepository;

    @Mock
    private AuditoriaService auditoriaService;

    @InjectMocks
    private PedidoService pedidoService;

    private Produto pizzaCalabresa;

    @BeforeEach
    void setUp() {
        pizzaCalabresa = new Produto();
        pizzaCalabresa.setId("prod-1");
        pizzaCalabresa.setNome("Pizza Calabresa");
        pizzaCalabresa.setPreco(50.0);
        pizzaCalabresa.setAtivo(true);
    }

    @Test
    @DisplayName("Deve ignorar preço e desconto enviados pelo cliente e calcular valor pelo catálogo do banco")
    void deveIgnorarPrecoEDescontoDoClienteECalcularPeloBanco() {
        when(produtoRepository.findAll()).thenReturn(List.of(pizzaCalabresa));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        ItemPedido itemManipulado = new ItemPedido();
        itemManipulado.setSabor("Pizza Calabresa");
        itemManipulado.setQuantidade(2);
        itemManipulado.setPrecoUnitario(1.00); // Tentativa de fraude pelo client-side

        Pedido novoPedido = new Pedido();
        novoPedido.setClienteId("João");
        novoPedido.setDesconto(99.0); // Tentativa de injetar desconto na criação
        novoPedido.setItens(List.of(itemManipulado));

        Pedido salvo = pedidoService.criarPedido(novoPedido);

        assertEquals(0.0, salvo.getDesconto(), "O desconto inicial deve ser forçado para 0.0");
        assertEquals(50.0, salvo.getItens().get(0).getPrecoUnitario(), "Deve usar o preço do banco (50.0)");
        assertEquals(100.0, salvo.getValorTotal(), "2x R$ 50,00 deve resultar em R$ 100,00");
        assertEquals("RECEBIDO", salvo.getStatus());
        verify(auditoriaService, times(1)).registrar(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("Deve bloquear transição de status fora da sequência permitida")
    void deveBloquearTransicaoDeStatusInvalida() {
        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setStatus("RECEBIDO");

        when(pedidoRepository.findById("ped-1")).thenReturn(Optional.of(pedido));

        assertThrows(IllegalStateException.class, () -> {
            pedidoService.atualizarStatus("ped-1", "FINALIZADO");
        });
    }

    @Test
    @DisplayName("Deve aplicar desconto válido de até 20% e recalcular valor total")
    void deveAplicarDescontoValido() {
        ItemPedido item = new ItemPedido();
        item.setSabor("Pizza Calabresa");
        item.setQuantidade(2);
        item.setPrecoUnitario(50.0);

        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setStatus("RECEBIDO");
        pedido.setItens(List.of(item));
        pedido.setValorTotal(100.0);
        pedido.setDesconto(0.0);

        when(pedidoRepository.findById("ped-1")).thenReturn(Optional.of(pedido));
        when(pedidoRepository.save(any(Pedido.class))).thenAnswer(i -> i.getArguments()[0]);

        Pedido atualizado = pedidoService.aplicarDesconto("ped-1", 10.0, "gerente_carlos");

        assertEquals(10.0, atualizado.getDesconto());
        assertEquals(90.0, atualizado.getValorTotal());
    }

    @Test
    @DisplayName("Deve rejeitar desconto superior a 20%")
    void deveRejeitarDescontoAcimaDoLimite() {
        Pedido pedido = new Pedido();
        pedido.setId("ped-1");
        pedido.setStatus("RECEBIDO");

        when(pedidoRepository.findById("ped-1")).thenReturn(Optional.of(pedido));

        assertThrows(IllegalArgumentException.class, () -> {
            pedidoService.aplicarDesconto("ped-1", 25.0, "gerente_carlos");
        });
    }
}