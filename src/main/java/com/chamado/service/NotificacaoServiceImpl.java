package com.chamado.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.chamado.dto.NotificacaoDTO;
import com.chamado.model.Chamado;
import com.chamado.model.Notificacao;
import com.chamado.model.Usuario;
import com.chamado.model.enums.CanalNotificacao;
import com.chamado.model.enums.StatusNotificacao;
import com.chamado.repository.NotificacaoRepository;

@Service
public class NotificacaoServiceImpl implements NotificacaoService {

    private final NotificacaoRepository notificacaoRepository;
    private final WhatsAppService whatsAppService;

    public NotificacaoServiceImpl(NotificacaoRepository notificacaoRepository, WhatsAppService whatsAppService) {
        this.notificacaoRepository = notificacaoRepository;
        this.whatsAppService = whatsAppService;
    }

    @Override
    public void notificar(Usuario destinatario, Chamado chamado, String mensagem) {
        if (destinatario == null) {
            return;
        }

        Notificacao notificacao = new Notificacao();
        notificacao.setDestinatario(destinatario);
        notificacao.setChamado(chamado);
        notificacao.setMensagem(mensagem);
        notificacao.setLida(false);

        // WhatsApp envolve um terceiro (CallMeBot): só é usado com consentimento explícito do titular
        boolean possuiWhatsapp = destinatario.getTelefone() != null && !destinatario.getTelefone().isBlank()
                && destinatario.getWhatsappApiKey() != null && !destinatario.getWhatsappApiKey().isBlank()
                && Boolean.TRUE.equals(destinatario.getConsentimentoWhatsapp());

        if (possuiWhatsapp) {
            notificacao.setCanal(CanalNotificacao.WHATSAPP);
            boolean enviado = whatsAppService.enviarMensagem(
                    destinatario.getTelefone(), destinatario.getWhatsappApiKey(), mensagemMinima(chamado));
            notificacao.setStatus(enviado ? StatusNotificacao.ENVIADA : StatusNotificacao.FALHA);
        } else {
            notificacao.setCanal(CanalNotificacao.SISTEMA);
            notificacao.setStatus(StatusNotificacao.ENVIADA);
        }

        notificacaoRepository.save(notificacao);
    }

    /**
     * Texto enviado ao terceiro: não inclui título, descrição ou comentários do
     * chamado (que podem conter dados pessoais/sensíveis). O conteúdo completo
     * fica disponível apenas dentro do sistema.
     */
    private String mensagemMinima(Chamado chamado) {
        if (chamado == null || chamado.getId() == null) {
            return "ChamadoPro: você tem uma nova notificação. Acesse o sistema para ver os detalhes.";
        }
        return "ChamadoPro: há uma atualização no chamado #" + chamado.getId()
                + ". Acesse o sistema para ver os detalhes.";
    }

    @Override
    public List<NotificacaoDTO> listarPorUsuario(Usuario usuario) {
        if (usuario == null) {
            return List.of();
        }
        return notificacaoRepository.findByDestinatarioOrderByDataEnvioDesc(usuario)
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public long contarNaoLidas(Usuario usuario) {
        if (usuario == null) {
            return 0;
        }
        return notificacaoRepository.countByDestinatarioAndLida(usuario, false);
    }

    @Override
    public void marcarComoLida(Long id, Usuario usuario) {
        notificacaoRepository.findById(id).ifPresent(notificacao -> {
            if (usuario != null && notificacao.getDestinatario() != null
                    && notificacao.getDestinatario().getId().equals(usuario.getId())) {
                notificacao.setLida(true);
                notificacaoRepository.save(notificacao);
            }
        });
    }

    private NotificacaoDTO toDTO(Notificacao notificacao) {
        NotificacaoDTO dto = new NotificacaoDTO();
        dto.setId(notificacao.getId());
        dto.setMensagem(notificacao.getMensagem());
        dto.setCanal(notificacao.getCanal());
        dto.setStatus(notificacao.getStatus());
        dto.setLida(notificacao.isLida());
        dto.setDataEnvio(notificacao.getDataEnvio());
        if (notificacao.getChamado() != null) {
            dto.setChamadoId(notificacao.getChamado().getId());
            dto.setChamadoTitulo(notificacao.getChamado().getTitulo());
        }
        return dto;
    }
}
